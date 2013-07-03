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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * @author Terry Denney
 */
public abstract class AbstractAnnotationQuickAssistProcessor {

	public abstract String getAnnotationName();

	protected TypeDeclaration getSurroundingTypeDecl(ASTNode node) {
		if (node == null || node instanceof TypeDeclaration) {
			return (TypeDeclaration) node;
		}
		return getSurroundingTypeDecl(node.getParent());
	}

	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		return !ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), fieldDecl);
	}

	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		return !ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), methodDecl);
	}

	public boolean isQuickfixAvailable(SingleVariableDeclaration param, IInvocationContext context) {
		return !ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), param);
	}

	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		return !ProposalCalculatorUtil.hasAnnotation(getAnnotationName(), typeDecl);
	}

	/**
	 * @param fieldDecl
	 * @param name
	 * @param cu
	 * @return quick assists that apply to this fieldDecl - called after
	 * isQuickfixAvailable
	 */
	public List<IJavaCompletionProposal> getAssistsForField(FieldDeclaration fieldDecl, SimpleName name,
			ICompilationUnit cu) {
		return new ArrayList<IJavaCompletionProposal>();
	}

	/**
	 * @param methodDecl
	 * @param name
	 * @param cu
	 * @return quick assists that apply to this methodDecl - called after
	 * isQuickfixAvailable
	 */
	@SuppressWarnings("unchecked")
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		List<SingleVariableDeclaration> params = methodDecl.parameters();
		for (SingleVariableDeclaration param : params) {
			proposals.addAll(getAssistsForMethodParam(param, param.getName(), cu));
		}
		return proposals;
	}

	/**
	 * @param param
	 * @param name
	 * @param cu
	 * @return quick assists that apply to this method param - called after
	 * isQuickfixAvailable
	 */
	public List<IJavaCompletionProposal> getAssistsForMethodParam(SingleVariableDeclaration param, SimpleName name,
			ICompilationUnit cu) {
		return new ArrayList<IJavaCompletionProposal>();
	}

	/**
	 * @param typeDecl
	 * @param name
	 * @param cu
	 * @return quick assists that apply to this typeDecl - called after
	 * isQuickfixAvailable
	 */
	public List<IJavaCompletionProposal> getAssistsForType(TypeDeclaration typeDecl, SimpleName name,
			ICompilationUnit cu) {
		return new ArrayList<IJavaCompletionProposal>();
	}

	/**
	 * @param node
	 * @return closest surrounding BodyDeclaration or SingleVariableDeclaration
	 * that is a method param
	 */
	private ASTNode getSurroundingDecl(ASTNode node) {
		if (node == null || node instanceof BodyDeclaration) {
			return node;
		}

		if (node instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration) node;
			if (decl.getParent() instanceof MethodDeclaration) {
				return decl;
			}
		}

		return getSurroundingDecl(node.getParent());
	}

	private Block getSurroundingBlock(ASTNode node) {
		if (node == null || node instanceof Block) {
			return (Block) node;
		}

		return getSurroundingBlock(node.getParent());
	}

	private VariableDeclarationFragment getSurroundingFragment(ASTNode node) {
		if (node == null || node instanceof VariableDeclarationFragment) {
			return (VariableDeclarationFragment) node;
		}
		return getSurroundingFragment(node.getParent());
	}

	@SuppressWarnings("unchecked")
	public final List<IJavaCompletionProposal> getAssists(ASTNode coveringNode, IInvocationContext context) {
		if (!isSpringProject(context)) {
			return new ArrayList<IJavaCompletionProposal>();
		}

		ASTNode decl = getSurroundingDecl(coveringNode);
		if (decl != null) {
			if (decl instanceof FieldDeclaration) {
				FieldDeclaration fieldDecl = (FieldDeclaration) decl;

				// if cursor is inside a specific variable declaration fragment,
				// only get assists for that fragment
				VariableDeclarationFragment coveringFragment = getSurroundingFragment(coveringNode);
				if (coveringFragment != null) {
					return checkAndReturnAssists(fieldDecl, coveringFragment.getName(), context);
				}

				// cannot tell which fragment cursor is on, so get assists for
				// all fragments
				List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
				List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

				for (VariableDeclarationFragment fragment : fragments) {
					SimpleName name = fragment.getName();
					proposals.addAll(checkAndReturnAssists(fieldDecl, name, context));
				}

				return proposals;
			}
			else if (decl instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration varDecl = (SingleVariableDeclaration) decl;

				return checkAndReturnAssists(varDecl, varDecl.getName(), context);
			}
			else if (decl instanceof MethodDeclaration) {
				MethodDeclaration methodDecl = (MethodDeclaration) decl;
				Block body = methodDecl.getBody();

				// if coveringNode is not in the method body, show assists
				if (getSurroundingBlock(coveringNode) != body) {
					return checkAndReturnAssists(methodDecl, methodDecl.getName(), context);
				}
			}
			else if (decl instanceof TypeDeclaration) {
				if (getSurroundingDecl(coveringNode) instanceof TypeDeclaration) {
					TypeDeclaration typeDecl = (TypeDeclaration) decl;
					return checkAndReturnAssists(typeDecl, typeDecl.getName(), context);
				}
			}
		}

		return new ArrayList<IJavaCompletionProposal>();
	}

	private boolean isSpringProject(IInvocationContext context) {
		IJavaProject javaProject = context.getCompilationUnit().getJavaProject();
		return SpringCoreUtils.isSpringProject(javaProject.getProject());
	}

	private List<IJavaCompletionProposal> checkAndReturnAssists(FieldDeclaration fieldDecl, SimpleName name,
			IInvocationContext context) {
		setUpFields();
		if (isQuickfixAvailable(fieldDecl, context)) {
			return getAssistsForField(fieldDecl, name, context.getCompilationUnit());
		}
		return new ArrayList<IJavaCompletionProposal>();
	}

	private List<IJavaCompletionProposal> checkAndReturnAssists(MethodDeclaration methodDecl, SimpleName name,
			IInvocationContext context) {
		setUpFields();
		if (isQuickfixAvailable(methodDecl, context)) {
			return getAssistsForMethod(methodDecl, name, context.getCompilationUnit());
		}
		return new ArrayList<IJavaCompletionProposal>();
	}

	private List<IJavaCompletionProposal> checkAndReturnAssists(SingleVariableDeclaration param, SimpleName name,
			IInvocationContext context) {
		setUpFields();
		if (isQuickfixAvailable(param, context)) {
			return getAssistsForMethodParam(param, name, context.getCompilationUnit());
		}
		return new ArrayList<IJavaCompletionProposal>();
	}

	private List<IJavaCompletionProposal> checkAndReturnAssists(TypeDeclaration typeDecl, SimpleName name,
			IInvocationContext context) {
		setUpFields();
		if (isQuickfixAvailable(typeDecl, context)) {
			return getAssistsForType(typeDecl, name, context.getCompilationUnit());
		}
		return new ArrayList<IJavaCompletionProposal>();
	}

	/**
	 * Subclass should override this method if it uses fields to store result
	 * from isQuickfixAvailble(...)
	 */
	protected void setUpFields() {
		// default: do nothing
	}
}
