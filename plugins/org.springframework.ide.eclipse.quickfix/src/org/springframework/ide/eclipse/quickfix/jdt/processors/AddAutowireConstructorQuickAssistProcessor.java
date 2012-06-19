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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddAutowiredConstructorCompletionProposal;


/**
 * @author Kaitlin Duck Sherwood
 * @since 2.9
 */
public class AddAutowireConstructorQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private ITypeBinding typeBinding;

	@Override
	public String getAnnotationName() {
		return "Autowired";
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(SingleVariableDeclaration param, IInvocationContext context) {
		return false;
	}

	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		CompilationUnit astRoot = context.getASTRoot();

		typeBinding = ASTNodes.getTypeBinding(typeDecl.getName());

		if (typeBinding == null) {
			return false;
		}

		if (typeBinding.isAnnotation()) {
			return false;
		}

		if (typeBinding.isEnum()) {
			return false;
		}

		List<IVariableBinding> finalVariablesList = computeFinalVariables(astRoot);

		if (finalVariablesList.size() == 0) {
			return false;
		}

		return !hasNonDefaultConstructor(typeDecl);
	}

	private boolean hasNonDefaultConstructor(TypeDeclaration typeDecl) {
		for (MethodDeclaration methodDecl : typeDecl.getMethods()) {
			if (methodDecl.isConstructor()) {
				if (!(methodDecl.parameters().size() == 0)) {
					return true;
				}
			}
		}

		return false;
	}

	private List<IVariableBinding> computeFinalVariables(CompilationUnit astRoot) {
		List<IVariableBinding> variables = new ArrayList<IVariableBinding>();
		for (IVariableBinding varBinding : typeBinding.getDeclaredFields()) {
			if (varBinding.isSynthetic()) {
				continue;
			}
			if (Modifier.isStatic(varBinding.getModifiers())) {
				continue;
			}
			if (Modifier.isFinal(varBinding.getModifiers())) {
				ASTNode declaringNode = astRoot.findDeclaringNode(varBinding);
				if (declaringNode instanceof VariableDeclarationFragment
						&& ((VariableDeclarationFragment) declaringNode).getInitializer() != null) {
					continue; // Do not add final fields which have been set in
								// the <clinit>
				}
				else {
					variables.add(varBinding);
					continue;
				}
			}
		}
		return variables;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForType(TypeDeclaration typeDecl, SimpleName name,
			ICompilationUnit icu) {

		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		CompilationUnit cu = ASTResolving.findParentCompilationUnit(typeDecl);

		List<IVariableBinding> variables = computeFinalVariables(cu);

		IVariableBinding[] finalVariables = new IVariableBinding[variables.size()];

		finalVariables = variables.toArray(finalVariables);
		AddAutowiredConstructorCompletionProposal proposal = new AddAutowiredConstructorCompletionProposal(typeDecl,
				icu, finalVariables);
		proposals.add(proposal);

		return proposals;
	}

}
