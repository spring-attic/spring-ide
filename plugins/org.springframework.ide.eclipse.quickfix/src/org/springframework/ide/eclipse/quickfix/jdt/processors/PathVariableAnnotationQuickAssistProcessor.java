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
package org.springframework.ide.eclipse.quickfix.jdt.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddPathVariableCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddPathVariableParameterCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.ide.eclipse.quickfix.jdt.util.UriTemplateVariable;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class PathVariableAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private Map<UriTemplateVariable, List<SingleVariableDeclaration>> variableToMissingAnnotationParams;

	private Map<UriTemplateVariable, List<SingleVariableDeclaration>> variableToParams;

	@Override
	public String getAnnotationName() {
		return "PathVariable";
	}

	private MethodDeclaration getMethodDeclaration(ASTNode node) {
		if (node == null || node instanceof MethodDeclaration) {
			return (MethodDeclaration) node;
		}
		return getMethodDeclaration(node.getParent());
	}

	private void findParametersWithMissingAnnotation(List<UriTemplateVariable> variables,
			List<SingleVariableDeclaration> params, IProject project) {
		for (SingleVariableDeclaration param : params) {
			String variableName = ProposalCalculatorUtil.getPathVariableName(param);
			// String paramName = param.getName().getFullyQualifiedName();
			for (UriTemplateVariable variable : variables) {
				if (!ProposalCalculatorUtil.isKnownRequestMappingParamType(project, param.getType().resolveBinding())) {
					// if (variable.getVariableName().equals(paramName)) {
					if (variableName == null) {
						variableToMissingAnnotationParams.get(variable).add(param);
					}
					else if (variableName.equals(variable.getVariableName())) {
						variableToParams.get(variable).add(param);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("RequestMapping", methodDecl);
		List<UriTemplateVariable> variables = new ArrayList<UriTemplateVariable>();
		for (Annotation annotation : annotations) {
			variables.addAll(ProposalCalculatorUtil.getUriTemplatVariables(annotation));
		}

		if (variables.size() > 0) {
			for (UriTemplateVariable variable : variables) {
				variableToMissingAnnotationParams.put(variable, new ArrayList<SingleVariableDeclaration>());
				variableToParams.put(variable, new ArrayList<SingleVariableDeclaration>());
			}

			List<SingleVariableDeclaration> params = methodDecl.parameters();
			findParametersWithMissingAnnotation(variables, params, context.getCompilationUnit().getResource()
					.getProject());
		}

		for (UriTemplateVariable variable : variableToParams.keySet()) {
			if (variableToParams.get(variable).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("RequestMapping", typeDecl);
		List<UriTemplateVariable> variables = new ArrayList<UriTemplateVariable>();
		for (Annotation annotation : annotations) {
			variables.addAll(ProposalCalculatorUtil.getUriTemplatVariables(annotation));
		}

		for (UriTemplateVariable variable : variables) {
			variableToMissingAnnotationParams.put(variable, new ArrayList<SingleVariableDeclaration>());
			variableToParams.put(variable, new ArrayList<SingleVariableDeclaration>());
		}

		if (variables.size() > 0) {
			MethodDeclaration[] methodDecls = typeDecl.getMethods();
			for (MethodDeclaration methodDecl : methodDecls) {
				findParametersWithMissingAnnotation(variables, methodDecl.parameters(), context.getCompilationUnit()
						.getResource().getProject());
			}
		}

		return variableToMissingAnnotationParams.keySet().size() > 0;
	}

	@Override
	protected void setUpFields() {
		variableToMissingAnnotationParams = new HashMap<UriTemplateVariable, List<SingleVariableDeclaration>>();
		variableToParams = new HashMap<UriTemplateVariable, List<SingleVariableDeclaration>>();
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssists(methodDecl, cu);
	}

	private List<IJavaCompletionProposal> getAssists(BodyDeclaration decl, ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		List<SingleVariableDeclaration> includedParams = new ArrayList<SingleVariableDeclaration>();
		for (UriTemplateVariable variable : variableToMissingAnnotationParams.keySet()) {
			List<SingleVariableDeclaration> missingAnnotationsParams = variableToMissingAnnotationParams.get(variable);
			for (SingleVariableDeclaration param : missingAnnotationsParams) {
				proposals.add(new AddPathVariableCompletionProposal(variable, param, getMethodDeclaration(param), cu,
						decl instanceof TypeDeclaration));
				includedParams.add(param);
			}

			if (missingAnnotationsParams.isEmpty() && decl instanceof MethodDeclaration
					&& variableToParams.get(variable).isEmpty()) {
				proposals.add(new AddPathVariableParameterCompletionProposal(variable, ((MethodDeclaration) decl), cu));
			}

			// if (decl instanceof MethodDeclaration) {
			// MethodDeclaration methodDecl = (MethodDeclaration) decl;
			// List<SingleVariableDeclaration> parameters =
			// methodDecl.parameters();
			// for (SingleVariableDeclaration param : parameters) {
			// if (!includedParams.contains(param)) {
			// if (!ProposalCalculatorUtil.hasAnnotation("PathVariable", param))
			// {
			// //
			// ProposalCalculatorUtil.isKnownRequestMappingParamType(decl.get,
			// // typeBinding)param.getType().resolveBinding()
			// proposals
			// .add(new AddPathVariableCompletionProposal(variable, param,
			// methodDecl, cu, false));
			// }
			// }
			// }
			// }
		}

		return proposals;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForType(TypeDeclaration typeDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssists(typeDecl, cu);
	}
}
