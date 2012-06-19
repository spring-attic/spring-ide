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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddAutowireCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 */
public class AutowiredAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		int modifiers = fieldDecl.getModifiers();
		if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
			return super.isQuickfixAvailable(fieldDecl, context);
		}
		return false;
	}

	private TypeDeclaration getEnclosingTypeDeclaration(ASTNode node) {
		if (node == null || node instanceof TypeDeclaration) {
			return (TypeDeclaration) node;
		}

		return getEnclosingTypeDeclaration(node.getParent());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		if (methodDecl.isConstructor()) {
			// @Autowired can only be applied to one constructor per class
			// make sure no other constructors are already annotated
			TypeDeclaration typeDecl = getEnclosingTypeDeclaration(methodDecl);
			if (typeDecl != null) {
				List<BodyDeclaration> decls = typeDecl.bodyDeclarations();
				for (BodyDeclaration decl : decls) {
					if (decl instanceof MethodDeclaration) {
						MethodDeclaration currentMethodDecl = (MethodDeclaration) decl;
						if (currentMethodDecl.isConstructor() && currentMethodDecl != methodDecl) {
							if (ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), currentMethodDecl)) {
								return false;
							}
						}
					}
				}
			}
		}

		Type returnType = methodDecl.getReturnType2();

		// check if methodDecl is a void method
		if (methodDecl.isConstructor()
				|| (returnType != null && returnType.isPrimitiveType() && PrimitiveType.VOID
						.equals(((PrimitiveType) returnType).getPrimitiveTypeCode()))) {

			// check if there is at least one parameter
			List params = methodDecl.parameters();
			if (params != null && !params.isEmpty()) {
				return super.isQuickfixAvailable(methodDecl, context);
			}
		}

		return false;
	}

	@Override
	public String getAnnotationName() {
		return "Autowired";
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForField(FieldDeclaration fieldDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssistsForDecl(fieldDecl, name, cu);
	}

	private List<IJavaCompletionProposal> getAssistsForDecl(BodyDeclaration decl, SimpleName name, ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		proposals.add(new AddAutowireCompletionProposal(decl, cu));
		proposals.add(new AddAutowireCompletionProposal(decl, cu, AddAutowireCompletionProposal.Param.REQUIRED));

		return proposals;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		return getAssistsForDecl(methodDecl, name, cu);
	}

}
