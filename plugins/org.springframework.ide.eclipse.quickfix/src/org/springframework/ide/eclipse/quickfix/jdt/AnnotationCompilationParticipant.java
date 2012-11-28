/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.ide.eclipse.quickfix.jdt.util.UriTemplateVariable;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * Compilation participant to display warning in Spring annotation definitions
 * 
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class AnnotationCompilationParticipant extends CompilationParticipant {

	@SuppressWarnings("unchecked")
	private List<String> findPathVariables(AbstractTypeDeclaration typeDecl) {
		List<String> pathVariables = new ArrayList<String>();
		List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyDecls) {
			if (bodyDecl instanceof MethodDeclaration) {
				MethodDeclaration methodDecl = (MethodDeclaration) bodyDecl;
				pathVariables.addAll(findPathVariables(methodDecl));
			}
		}

		return pathVariables;
	}

	private List<String> findPathVariables(ITypeBinding typeBinding) {
		List<String> pathVariables = new ArrayList<String>();

		if (typeBinding == null) {
			return pathVariables;
		}

		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		for (IMethodBinding method : methods) {
			for (int i = 0; i < method.getParameterTypes().length; i++) {
				IAnnotationBinding[] annotations = method.getParameterAnnotations(i);
				for (IAnnotationBinding annotation : annotations) {
					if ("PathVariable".equals(annotation.getName())) {
						IMemberValuePairBinding[] memberValuePairs = annotation.getAllMemberValuePairs();
						String valueStr = null;
						for (IMemberValuePairBinding memberValuePair : memberValuePairs) {
							if ("value".equals(memberValuePair.getName())) {
								Object value = memberValuePair.getValue();
								if (value instanceof String) {
									valueStr = (String) value;
								}
							}
						}

						// "value" attribute not explicitly defined, use
						// parameter name instead
						if (valueStr == null || valueStr.length() == 0) {
							IJavaElement javaMethod = method.getJavaElement();
							if (javaMethod instanceof IMethod) {
								try {
									valueStr = ((IMethod) javaMethod).getParameterNames()[i];
								}
								catch (JavaModelException e) {
								}
							}
						}

						if (valueStr != null) {
							pathVariables.add(valueStr);
						}
					}
				}
			}
		}

		pathVariables.addAll(findPathVariables(typeBinding.getSuperclass()));

		return pathVariables;

	}

	@SuppressWarnings("unchecked")
	private List<String> findPathVariables(MethodDeclaration methodDecl) {
		List<String> pathVariables = new ArrayList<String>();
		List<SingleVariableDeclaration> params = methodDecl.parameters();
		for (SingleVariableDeclaration param : params) {
			String variableName = ProposalCalculatorUtil.getPathVariableName(param);
			if (variableName != null) {
				pathVariables.add(variableName);
			}
		}
		return pathVariables;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void reconcile(ReconcileContext context) {
		try {
			CompilationUnit cuAST = context.getAST3();
			ICompilationUnit cu = context.getWorkingCopy();
			IFile file = (IFile) cu.getResource();

			List<MissingPathVariableWarning> problems = new ArrayList<MissingPathVariableWarning>();

			if (cuAST != null) {
				List<AbstractTypeDeclaration> typeDecls = cuAST.types();
				for (AbstractTypeDeclaration typeDecl : typeDecls) {
					List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations();
					for (BodyDeclaration bodyDecl : bodyDecls) {
						if (bodyDecl instanceof MethodDeclaration) {
							MethodDeclaration methodDecl = (MethodDeclaration) bodyDecl;
							List<String> currentPathVariables = findPathVariables(methodDecl);

							Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("RequestMapping",
									methodDecl);
							for (Annotation annotation : annotations) {
								List<UriTemplateVariable> variables = ProposalCalculatorUtil
										.getUriTemplatVariables(annotation);
								for (UriTemplateVariable variable : variables) {
									boolean found = false;
									for (String currentPathVariable : currentPathVariables) {
										if (Pattern.matches(variable.getVariableName(), currentPathVariable)) {
											found = true;
											break;
										}
									}

									if (!found) {
										problems.add(new MissingPathVariableWarning(annotation, variable, file, cuAST
												.getLineNumber(variable.getOffset())));
									}
								}
							}
						}
					}

					List<String> pathVariables;
					if (context.isResolvingBindings()) {
						pathVariables = new ArrayList<String>();

						ITypeBinding typeBinding = typeDecl.resolveBinding();
						while (typeBinding != null
								&& !typeDecl.getAST().resolveWellKnownType("java.lang.Object").equals(typeBinding)) {
							pathVariables.addAll(findPathVariables(typeBinding));
							typeBinding = typeBinding.getSuperclass();
						}
					}
					else {
						pathVariables = findPathVariables(typeDecl);
					}

					Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("RequestMapping", typeDecl);
					for (Annotation annotation : annotations) {
						List<UriTemplateVariable> variables = ProposalCalculatorUtil.getUriTemplatVariables(annotation);
						for (UriTemplateVariable variable : variables) {
							if (!pathVariables.contains(variable.getVariableName())) {
								problems.add(new MissingPathVariableWarning(annotation, variable, file, cuAST
										.getLineNumber(variable.getOffset())));
							}
						}
					}
				}
			}

			context.putProblems(MissingPathVariableWarning.MARKER_TYPE,
					problems.toArray(new CategorizedProblem[problems.size()]));
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
		}
	}

	@Override
	public boolean isActive(IJavaProject project) {
		return SpringCoreUtils.isSpringProject(project.getProject());
	}
}
