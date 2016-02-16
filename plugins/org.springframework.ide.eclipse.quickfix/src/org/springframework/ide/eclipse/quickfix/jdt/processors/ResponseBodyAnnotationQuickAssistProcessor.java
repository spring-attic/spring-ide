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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddResponseBodyCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class ResponseBodyAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	@Override
	public String getAnnotationName() {
		return "ResponseBody";
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		if (ProposalCalculatorUtil.hasAnnotation("RequestMapping", methodDecl)
				&& !ProposalCalculatorUtil.hasAnnotation("ResponseBody", methodDecl)) {
			Type returnType = methodDecl.getReturnType2();
			if (returnType.isPrimitiveType()
					&& ((PrimitiveType) returnType).getPrimitiveTypeCode().equals(PrimitiveType.VOID)) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(SingleVariableDeclaration param, IInvocationContext context) {
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		return false;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		proposals.add(new AddResponseBodyCompletionProposal(methodDecl, cu));
		return proposals;
	}
}
