/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.processors.imports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AddImportCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Factory that creates various types of relevance resolvers for "add imports"
 * proposals based on the type of selected AST node where "add imports" quick
 * fix should be applied to
 *
 */
public class AddImportRelevanceResolverFactory {

	public List<AddImportRelevanceResolver> getResolvers(ICompilationUnit cu, List<IJavaCompletionProposal> proposals) {
		List<AddImportRelevanceResolver> computations = new ArrayList<AddImportRelevanceResolver>();

		int boost = 100;
		computations.add(variableDeclarationRHSReturnType(cu, proposals, boost));

		return computations;
	}

	/**
	 * Computes relevance for proposals that match the return type on the RHS of
	 * a statement where LHS has unresolved type
	 * @param kind
	 * @param cu
	 * @param proposals
	 * @param boost
	 * @return
	 */
	protected AddImportRelevanceResolver variableDeclarationRHSReturnType(ICompilationUnit cu,
			List<IJavaCompletionProposal> proposals, int boost) {

		return new AddImportRelevanceResolver(cu, proposals, boost) {

			protected VariableDeclarationStatement getVariableDeclarationStatement(ASTNode selectedNode) {
				// Go two levels up:
				if (selectedNode != null) {
					ASTNode parent = selectedNode.getParent();
					if (parent instanceof VariableDeclarationStatement) {
						return (VariableDeclarationStatement) parent;
					}
					else if (parent.getParent() instanceof VariableDeclarationStatement) {
						return (VariableDeclarationStatement) parent.getParent();
					}
				}
				return null;
			}

			@Override
			protected AddImportCorrectionProposal findCorrectionProposal(int kind, ASTNode statementNode)
					throws Exception {
				VariableDeclarationStatement statement = getVariableDeclarationStatement(statementNode);
				if (statement != null) {

					List<?> frags = statement.fragments();
					if (frags != null) {
						for (Object content : frags) {
							if (content instanceof VariableDeclarationFragment) {
								VariableDeclarationFragment frag = (VariableDeclarationFragment) content;
								Expression exp = frag.getInitializer();
								if (exp != null) {
									ITypeBinding binding = exp.resolveTypeBinding();

									if (binding != null) {
										String qualifiedName = binding.getQualifiedName();

										if (qualifiedName != null) {
											for (IJavaCompletionProposal prop : proposals) {
												if (prop instanceof AddImportCorrectionProposal) {
													AddImportCorrectionProposal corrProposal = (AddImportCorrectionProposal) prop;
													String propQuali = corrProposal.getQualifiedTypeName();
													if (qualifiedName.contains(propQuali)) {
														return corrProposal;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				return null;
			}
		};
	}
}
