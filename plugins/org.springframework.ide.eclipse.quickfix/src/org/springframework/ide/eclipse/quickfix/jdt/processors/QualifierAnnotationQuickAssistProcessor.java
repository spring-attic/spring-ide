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
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddQualiferCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddQualiferToMethodParamCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class QualifierAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private List<SingleVariableDeclaration> params;

	@Override
	protected void setUpFields() {
		params = new ArrayList<SingleVariableDeclaration>();
	}

	@Override
	public String getAnnotationName() {
		return "Qualifier";
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		if (ProposalCalculatorUtil.hasAnnotation("Autowired", fieldDecl)
				&& !ProposalCalculatorUtil.hasAnnotation("Qualifier", fieldDecl)) {
			ITypeBinding typeBinding = fieldDecl.getType().resolveBinding();
			if (typeBinding != null) {
				return ProposalCalculatorUtil.getMatchingBeans(context, typeBinding).size() > 0;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		if (ProposalCalculatorUtil.hasAnnotation("Autowired", methodDecl)) {
			List<SingleVariableDeclaration> params = methodDecl.parameters();
			for (SingleVariableDeclaration param : params) {
				if (!ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), param)) {
					ITypeBinding typeBinding = param.getType().resolveBinding();
					if (typeBinding != null) {
						if (ProposalCalculatorUtil.getMatchingBeans(context, typeBinding).size() > 0) {
							this.params.add(param);
						}
					}
				}
			}
		}

		return this.params.size() > 0;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForField(FieldDeclaration fieldDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		proposals.add(new AddQualiferCompletionProposal(fieldDecl, cu));
		return proposals;
		// if (ProposalCalculatorUtil.hasAnnotation("Autowired", fieldDecl)) {
		// ITypeBinding typeBinding = fieldDecl.getType().resolveBinding();
		// if (typeBinding != null) {
		// Set<String> matchingBeans =
		// ProposalCalculatorUtil.getMatchingBeans(context, typeBinding);
		// if (matchingBeans.size() > 0) {
		// List<IJavaCompletionProposal> proposals = new
		// ArrayList<IJavaCompletionProposal>();
		// // for (String bean : matchingBeans) {
		// // proposals.add(new AddQualiferCompletionProposal(bean,
		// // fieldDecl, context.getCompilationUnit(),
		// // startPos, length, 1));
		// // }
		// proposals.add(new AddQualiferCompletionProposal(fieldDecl, cu));
		// return proposals;
		// }
		// }
		// }
		// return super.getAssistsForField(fieldDecl, name, cu);
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		proposals.add(new AddQualiferToMethodParamCompletionProposal(params, methodDecl, cu));

		// if (ProposalCalculatorUtil.hasAnnotation("Autowired", methodDecl)) {
		// List<SingleVariableDeclaration> parameters = methodDecl.parameters();
		// List<SingleVariableDeclaration> parametersToAdd = new
		// ArrayList<SingleVariableDeclaration>();
		// for (SingleVariableDeclaration parameter : parameters) {
		// if (!ProposalCalculatorUtil.hasAnnotation("Qualifier", parameter)) {
		// ITypeBinding typeBinding = parameter.getType().resolveBinding();
		// Set<String> matchingBeans =
		// ProposalCalculatorUtil.getMatchingBeans(context, typeBinding);
		// if (matchingBeans.size() > 0) {
		// parametersToAdd.add(parameter);
		// }
		// }
		// }
		//
		// if (parametersToAdd.size() > 0) {
		// proposals.add(new
		// AddQualiferToMethodParamCompletionProposal(parametersToAdd,
		// methodDecl, cu));
		// }
		// }

		return proposals;
	}

}
