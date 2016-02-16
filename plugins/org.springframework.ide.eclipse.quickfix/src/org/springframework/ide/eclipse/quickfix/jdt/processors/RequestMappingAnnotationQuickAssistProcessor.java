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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddRequestMappingCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 */
public class RequestMappingAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	@Override
	public String getAnnotationName() {
		return "RequestMapping";
	}

	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		if (super.isQuickfixAvailable(methodDecl, context)) {
			ASTNode node = methodDecl;
			TypeDeclaration typeDecl = null;
			while (node != null && typeDecl == null) {
				if (node instanceof TypeDeclaration) {
					typeDecl = (TypeDeclaration) node;
				}
				node = node.getParent();
			}

			if (typeDecl != null) {
				return ProposalCalculatorUtil.hasAnnotation("Controller", typeDecl);
			}
		}
		// TODO: check for @Controller on type declaration
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		return super.isQuickfixAvailable(typeDecl, context)
				&& ProposalCalculatorUtil.hasAnnotation("Controller", typeDecl);
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssistsForDecl(methodDecl, name, cu);
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForType(TypeDeclaration typeDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssistsForDecl(typeDecl, name, cu);
	}

	private List<IJavaCompletionProposal> getAssistsForDecl(BodyDeclaration decl, SimpleName name, ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		proposals.add(new AddRequestMappingCompletionProposal(name.getFullyQualifiedName(), decl, cu, name
				.getStartPosition(), name.getLength(), 1));
		return proposals;

	}

}
