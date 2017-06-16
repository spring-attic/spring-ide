/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.operations.completion.LSCompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LSBasedCompletionEngine implements ICompletionEngine {

	private ISourceViewer viewer;
	private LSContentAssistProcessor lsContentAssistProcessor;

	public LSBasedCompletionEngine(ISourceViewer viewer) {
		this.viewer = viewer;
		this.lsContentAssistProcessor = new LSContentAssistProcessor();
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception {
		ICompletionProposal[] proposals = lsContentAssistProcessor.computeCompletionProposals(viewer, offset);
		for (int i = 0; i < proposals.length; i++) {
			LSCompletionProposal proposal = (LSCompletionProposal) proposals[i];
			proposals[i] = new LSBasedSmartCompletionProposal(proposal);
		}
		return Arrays.asList(proposals);
	}

}
