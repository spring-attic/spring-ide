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

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.operations.completion.LSCompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.DocumentRegion;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LSPBasedYAssistContext implements YamlAssistContext {
	
	private LSContentAssistProcessor lsContentAssistProcessor;
	private ISourceViewer viewer;

	public LSPBasedYAssistContext(ISourceViewer viewer) {
		this.viewer = viewer;
		this.lsContentAssistProcessor = new LSContentAssistProcessor();
	}

	@Override
	public YamlAssistContext traverse(YamlPathSegment arg0) throws Exception {
		return this;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(YamlDocument arg0, SNode arg1, int arg2) throws Exception {
		ICompletionProposal[] proposals = lsContentAssistProcessor.computeCompletionProposals(viewer, arg2);
		for (int i = 0; i < proposals.length; i++) {
			LSCompletionProposal proposal = (LSCompletionProposal) proposals[i];
			proposals[i] = new SmartLSCompletionProposal(proposal);
		}
		
		return Arrays.asList(proposals);
	}

	@Override
	public HoverInfo getHoverInfo() {
		return null;
	}

	@Override
	public HoverInfo getHoverInfo(YamlPathSegment arg0) {
		return null;
	}

	@Override
	public HoverInfo getValueHoverInfo(YamlDocument arg0, DocumentRegion arg1) {
		return null;
	}

}
