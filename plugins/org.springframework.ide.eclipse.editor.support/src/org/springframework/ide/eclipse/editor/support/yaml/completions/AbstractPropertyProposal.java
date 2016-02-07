/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.completions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.editor.support.EditorSupportActivator;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory.ScoreableProposal;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInformationControlCreator;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;

public abstract class AbstractPropertyProposal extends ScoreableProposal implements ICompletionProposalExtension3,
ICompletionProposalExtension4, ICompletionProposalExtension5, ICompletionProposalExtension6
{

	protected final IDocument fDoc;
	private final ProposalApplier proposalApplier;
	private boolean isDeprecated = false;

	public AbstractPropertyProposal(IDocument doc, ProposalApplier applier) {
		this.proposalApplier = applier;
		this.fDoc = doc;
	}

	public Point getSelection(IDocument document) {
		try {
			return proposalApplier.getSelection(document);
		} catch (Exception e) {
			EditorSupportActivator.log(e);
			return null;
		}
	}


	@Override
	public String getAdditionalProposalInfo() {
		HoverInfo hoverInfo = getAdditionalProposalInfo(new NullProgressMonitor());
		if (hoverInfo!=null) {
			return hoverInfo.getHtml();
		}
		return null;
	}

	@Override
	public abstract HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor);

	public String getDisplayString() {
		StyledString styledText = getStyledDisplayString();
		return styledText.getString();
	}

	public Image getImage() {
		return null;
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public StyledString getStyledDisplayString() {
		StyledString result = new StyledString();
		highlightPattern(getHighlightPattern(), getBaseDisplayString(), result);
		YType type = getType();
		if (type!=null) {
			String typeStr = niceTypeName(type);
			result.append(" : "+typeStr, StyledString.DECORATIONS_STYLER);
		}
		return result;
	}

	protected boolean isDeprecated() {
		return isDeprecated;
	}
	public void deprecate() {
		if (!isDeprecated()) {
			deemphasize();
			deemphasize();
			isDeprecated = true;
		}
	}
	protected abstract YType getType();
	protected abstract String getHighlightPattern();
	protected abstract String getBaseDisplayString();
	protected abstract String niceTypeName(YType type);

	private void highlightPattern(String pattern, String data, StyledString result) {
		Styler highlightStyle = CompletionFactory.HIGHLIGHT;
		Styler plainStyle = isDeemphasized()?CompletionFactory.DEEMPHASIZE:CompletionFactory.NULL_STYLER;
		if (isDeprecated()) {
			highlightStyle = CompletionFactory.compose(highlightStyle, CompletionFactory.DEPRECATE);
			plainStyle = CompletionFactory.compose(plainStyle, CompletionFactory.DEPRECATE);
		}
		if (StringUtils.hasText(pattern)) {
			int dataPos = 0;	int dataLen = data.length();
			int patternPos = 0; int patternLen = pattern.length();

			while (dataPos<dataLen && patternPos<patternLen) {
				int pChar = pattern.charAt(patternPos++);
				int highlightPos = data.indexOf(pChar, dataPos);
				if (dataPos<highlightPos) {
					result.append(data.substring(dataPos, highlightPos), plainStyle);
				}
				result.append(data.charAt(highlightPos), highlightStyle);
				dataPos = highlightPos+1;
			}
			if (dataPos<dataLen) {
				result.append(data.substring(dataPos), plainStyle);
			}
		} else { //no pattern to highlight
			result.append(data, plainStyle);
		}
	}

	@Override
	public String toString() {
		return getBaseDisplayString();
	}

	@Override
	public void apply(IDocument document) {
		try {
			proposalApplier.apply(document);
		} catch (Exception e) {
			EditorSupportActivator.log(e);
		}
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return new HoverInformationControlCreator("F2 for focus");
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return completionOffset;
	}
}