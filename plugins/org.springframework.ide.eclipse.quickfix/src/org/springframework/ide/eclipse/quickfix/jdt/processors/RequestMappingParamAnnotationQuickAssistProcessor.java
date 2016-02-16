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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddRequestMappingParamCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class RequestMappingParamAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private List<SingleVariableDeclaration> params;

	private static final Class<?>[] PARAM_ANNOTATION_CLASSES = new Class<?>[] { PathVariable.class, RequestBody.class,
			RequestParam.class, ModelAttribute.class };

	@Override
	public String getAnnotationName() {
		return "RequestParam";
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		return false;
	}

	// @SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		// if (ProposalCalculatorUtil.hasAnnotation("RequestMapping",
		// methodDecl)) {
		// List<SingleVariableDeclaration> parameters = methodDecl.parameters();
		// for (SingleVariableDeclaration parameter : parameters) {
		// if (isQuickfixAvailable(parameter, context)) {
		// params.add(parameter);
		// }
		// }
		// }
		//
		// return params.size() > 0;
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(SingleVariableDeclaration param, IInvocationContext context) {
		if (ProposalCalculatorUtil.hasAnnotation("RequestMapping", param.getParent())) {

			for (Class<?> annotationClass : PARAM_ANNOTATION_CLASSES) {
				if (ProposalCalculatorUtil.hasAnnotation(annotationClass.getSimpleName(), param)) {
					return false;
				}
			}

			ITypeBinding typeBinding = param.getType().resolveBinding();
			return !ProposalCalculatorUtil.isKnownRequestMappingParamType(context.getCompilationUnit().getJavaProject()
					.getProject(), typeBinding);
		}

		return false;
	}

	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		return false;
	}

	@Override
	protected void setUpFields() {
		this.params = new ArrayList<SingleVariableDeclaration>();
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		for (SingleVariableDeclaration param : params) {
			proposals.addAll(getAssistsForMethodParam(param, name, cu));
		}

		return proposals;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethodParam(SingleVariableDeclaration param, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		for (Class<?> annotationClass : PARAM_ANNOTATION_CLASSES) {
			proposals.add(new AddRequestMappingParamCompletionProposal(param, annotationClass, cu));
		}

		return proposals;
	}
}
