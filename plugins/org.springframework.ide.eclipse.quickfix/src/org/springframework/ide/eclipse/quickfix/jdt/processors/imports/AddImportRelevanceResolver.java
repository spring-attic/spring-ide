/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.processors.imports;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AddImportCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 *
 * Relevance resolver for "add import" proposals. It recomputes the relevance of
 * "add import" proposals. The purpose of this is to place proposals "higher" in
 * the list of proposals shown to a user
 *
 */
abstract class AddImportRelevanceResolver {

	protected final List<IJavaCompletionProposal> proposals;

	protected final ICompilationUnit cu;

	private final int relevanceBoost;

	public AddImportRelevanceResolver(ICompilationUnit cu, List<IJavaCompletionProposal> proposals,
			int relevanceBoost) {
		this.proposals = proposals;
		this.cu = cu;
		this.relevanceBoost = relevanceBoost;
	}

	/**
	 * @return true if relevance was recomputed and set in the proposal. False
	 * if no relevance change occurred.
	 */
	public boolean recomputeRelevance(int kind, ASTNode selectedNode) throws Exception {
		AddImportCorrectionProposal proposal = findCorrectionProposal(kind, selectedNode);
		if (proposal != null) {
			proposal.setRelevance(proposal.getRelevance() + relevanceBoost);
			return true;
		}
		return false;
	}

	protected abstract AddImportCorrectionProposal findCorrectionProposal(int kind, ASTNode selectedNode)
			throws Exception;

}