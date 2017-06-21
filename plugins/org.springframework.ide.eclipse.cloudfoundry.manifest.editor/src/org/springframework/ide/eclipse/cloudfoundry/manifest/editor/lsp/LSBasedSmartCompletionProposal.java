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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.lsp4e.operations.completion.LSCompletionProposal;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LSBasedSmartCompletionProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2,
		ICompletionProposalExtension3, ICompletionProposalExtension4, ICompletionProposalExtension5,
		ICompletionProposalExtension6, ICompletionProposalExtension7, IContextInformation {

	private final LSCompletionProposal proposal;

	public LSBasedSmartCompletionProposal(LSCompletionProposal lsProposal) {
		this.proposal = lsProposal;
	}
	
	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		fixMagicIndentation(viewer, offset, proposal.getItem());
		proposal.apply(viewer, trigger, stateMask, offset);
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		proposal.apply(document, trigger, offset);
	}

	@Override
	public void apply(IDocument document) {
		proposal.apply(document);
	}

	private void fixMagicIndentation(ITextViewer viewer, int offset, CompletionItem item) {
		TextEdit edit = item.getTextEdit();

		if (edit != null) {
			String newText = edit.getNewText();
			if (newText.contains("\n")) {
				
				IDocument document = viewer.getDocument();
				try {
					int line = document.getLineOfOffset(offset);
					IRegion lineRegion = document.getLineInformation(line);
					String lineContent = document.get(lineRegion.getOffset(), lineRegion.getLength());
					int indentation = 0;
					while (indentation < (offset - lineRegion.getOffset())
							&& (lineContent.charAt(indentation) == ' ' || lineContent.charAt(indentation) == '\t')) {
						indentation++;
					}

					newText = newText.replace("\n", "\n" + lineContent.substring(0, indentation));
					edit.setNewText(newText);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	public int getBestOffset() {
		return proposal.getBestOffset();
	}

	public CompletionItem getItem() {
		return proposal.getItem();
	}

	@Override
	public StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider) {
		return proposal.getStyledDisplayString(document, offset, boldStylerProvider);
	}

	@Override
	public String getDisplayString() {
		return proposal.getDisplayString();
	}

	@Override
	public StyledString getStyledDisplayString() {
		return proposal.getStyledDisplayString();
	}

	@Override
	public boolean isAutoInsertable() {
		return proposal.isAutoInsertable();
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return proposal.getInformationControlCreator();
	}

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		return proposal.getAdditionalProposalInfo(monitor);
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		return proposal.isValidFor(document, offset);
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return proposal.getPrefixCompletionText(document, completionOffset);
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return proposal.getPrefixCompletionStart(document, completionOffset);
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {
		proposal.selected(viewer, smartToggle);
	}

	@Override
	public void unselected(ITextViewer viewer) {
		proposal.unselected(viewer);
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		return proposal.validate(document, offset, event);
	}

	@Override
	public char[] getTriggerCharacters() {
		return proposal.getTriggerCharacters();
	}

	@Override
	public int getContextInformationPosition() {
		return proposal.getContextInformationPosition();
	}

	@Override
	public Point getSelection(IDocument document) {
		return proposal.getSelection(document);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return proposal.getAdditionalProposalInfo();
	}

	@Override
	public Image getImage() {
		return proposal.getImage();
	}

	@Override
	public IContextInformation getContextInformation() {
		return proposal.getContextInformation();
	}

	@Override
	public String getContextDisplayString() {
		return proposal.getContextDisplayString();
	}

	@Override
	public String getInformationDisplayString() {
		return proposal.getInformationDisplayString();
	}

	public String getSortText() {
		return proposal.getSortText();
	}

	public int getNumberOfModifsBeforeOffset() {
		return proposal.getNumberOfModifsBeforeOffset();
	}
}
