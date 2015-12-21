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
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.RemoveQualifierCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.SetAutowireRequiredFalseCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class AutowireRequiredNotFoundAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private List<MemberValuePair> valuePairs;

	private List<Annotation> autowiredAnnotations;

	private List<Annotation> qualifierAnnotations;

	@Override
	public String getAnnotationName() {
		return "Autowired";
	}

	@Override
	protected void setUpFields() {
		valuePairs = new ArrayList<MemberValuePair>();
		autowiredAnnotations = new ArrayList<Annotation>();
		qualifierAnnotations = new ArrayList<Annotation>();
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		if (isRequired(fieldDecl)) {
			findAnnotationRequiredToRemove(fieldDecl, context);
			findQualifierToRemove(fieldDecl, fieldDecl.getType(), context);
			return autowiredAnnotations.size() > 0 || qualifierAnnotations.size() > 0;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		if (isRequired(methodDecl)) {
			findAnnotationRequiredToRemove(methodDecl, context);
			List<SingleVariableDeclaration> params = methodDecl.parameters();
			for (SingleVariableDeclaration param : params) {
				findQualifierToRemove(param, param.getType(), context);
			}
			return autowiredAnnotations.size() > 0 || qualifierAnnotations.size() > 0;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean isRequired(BodyDeclaration decl) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Autowired", decl);
		for (Annotation annotation : annotations) {
			if (annotation instanceof NormalAnnotation) {
				List<MemberValuePair> values = ((NormalAnnotation) annotation).values();
				for (MemberValuePair pair : values) {
					if ("required".equals(pair.getName().getFullyQualifiedName())) {
						Expression expression = pair.getValue();
						if (expression instanceof BooleanLiteral && ((BooleanLiteral) expression).booleanValue()) {
							return true;
						}
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	private void findAnnotationRequiredToRemove(BodyDeclaration decl, IInvocationContext context) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Autowired", decl);
		MemberValuePair valuePair = ProposalCalculatorUtil.getRequiredMemberValuePair(decl);
		if (valuePair != null) {
			if (valuePair.getValue() instanceof BooleanLiteral
					&& ((BooleanLiteral) valuePair.getValue()).booleanValue()) {
				valuePairs.add(valuePair);
			}
		}
		else if (annotations.size() > 0) {
			annotations.addAll(annotations);
		}
	}

	private void findQualifierToRemove(ASTNode node, Type type, IInvocationContext context) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Qualifier", node);
		for (Annotation annotation : annotations) {
			if (annotation instanceof SingleMemberAnnotation) {
				Expression expression = ((SingleMemberAnnotation) annotation).getValue();
				if (expression instanceof StringLiteral) {
					String qualifier = ((StringLiteral) expression).getLiteralValue();
					ITypeBinding typeBinding = type.resolveBinding();
					Set<String> beans = ProposalCalculatorUtil.getMatchingBeans(context, typeBinding, qualifier);
					if (beans.isEmpty()) {
						qualifierAnnotations.add(annotation);
					}
				}
			}
		}
	}

	private List<IJavaCompletionProposal> getAssists(ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		for (Annotation annotation : autowiredAnnotations) {
			proposals.add(new SetAutowireRequiredFalseCompletionProposal(annotation, cu));
		}

		for (MemberValuePair valuePair : valuePairs) {
			proposals.add(new SetAutowireRequiredFalseCompletionProposal(valuePair, cu));
		}

		for (Annotation annotation : qualifierAnnotations) {
			proposals.add(new RemoveQualifierCompletionProposal(annotation, cu));
		}

		return proposals;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForField(FieldDeclaration fieldDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssists(cu);
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssists(cu);
	}

}
