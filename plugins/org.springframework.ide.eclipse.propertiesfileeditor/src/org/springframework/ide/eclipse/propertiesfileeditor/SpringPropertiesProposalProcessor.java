/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beryozkin, me@genady.org - #getSuggestions implementation copied from HippieCompleteAction
 *     Kris De Volder - Copied and modified HippieCompletionProcessor to become 'SpringPropertiesCompletionProcessor'.
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A completion proposal computer for hippie word completions.
 * <p>
 * Clients may instantiate=
 * </p>
 *
 * @since 3.2
 */
public class SpringPropertiesProposalProcessor implements IContentAssistProcessor {

	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];
	private static final char[] AUTO_ACTIVATION_CHARS = {'.'};

	private static final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

		private final String fString;
		private final String fPrefix;
		private final int fOffset;

		public Proposal(String string, String prefix, int offset) {
			fString= string;
			fPrefix= prefix;
			fOffset= offset;
		}

		public void apply(IDocument document) {
			apply(null, '\0', 0, fOffset);
		}

		public Point getSelection(IDocument document) {
			return new Point(fOffset + fString.length(), 0);
		}

		public String getAdditionalProposalInfo() {
			return "Some more info?";
		}

		public String getDisplayString() {
			return fPrefix + fString;
		}

		public Image getImage() {
			return null;
		}

		public IContextInformation getContextInformation() {
			return null;
		}

		public void apply(IDocument document, char trigger, int offset) {
			try {
				String replacement= fString.substring(offset - fOffset);
				document.replace(offset, 0, replacement);
			} catch (BadLocationException x) {
				// TODO Auto-generated catch block
				x.printStackTrace();
			}
		}

		public boolean isValidFor(IDocument document, int offset) {
			return validate(document, offset, null);
		}

		public char[] getTriggerCharacters() {
			return null;
		}

		public int getContextInformationPosition() {
			return 0;
		}

		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument(), trigger, offset);
		}

		public void selected(ITextViewer viewer, boolean smartToggle) {
		}

		public void unselected(ITextViewer viewer) {
		}

		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			try {
				int prefixStart= fOffset - fPrefix.length();
				return offset >= fOffset && offset < fOffset + fString.length() && document.get(prefixStart, offset - (prefixStart)).equals((fPrefix + fString).substring(0, offset - prefixStart));
			} catch (BadLocationException x) {
				return false;
			}
		}

		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return fPrefix + fString;
		}

		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fOffset - fPrefix.length();
		}

		public boolean isAutoInsertable() {
			return true;
		}

	}

	private final SpringPropertiesCompletionEngine fEngine;

	public SpringPropertiesProposalProcessor(IJavaProject jp) throws Exception {
		this.fEngine = new SpringPropertiesCompletionEngine(jp);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			String prefix= getPrefix(viewer, offset);
			if (prefix == null || prefix.length() == 0)
				return NO_PROPOSALS;

			Collection<String> suggestions = fEngine.getCompletions(viewer.getDocument(), prefix, offset);

			List<ICompletionProposal> result= new ArrayList<>();
			for (Iterator<String> it= suggestions.iterator(); it.hasNext();) {
				String string= (String) it.next();
				if (string.length() > 0)
					result.add(createProposal(string, prefix, offset));
			}

			return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);

		} catch (BadLocationException x) {
			// ignore and return no proposals
			return NO_PROPOSALS;
		}
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
		IDocument doc= viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return null;

		int length= 0;
		while (--offset >= 0 && isPrefixChar(doc.getChar(offset)))
			length++;

		return doc.get(offset + 1, length);
	}

	private boolean isPrefixChar(char c) {
		return c=='.' || Character.isJavaIdentifierPart(c);
	}

	private ICompletionProposal createProposal(String string, String prefix, int offset) {
		return new Proposal(string, prefix, offset);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// no context informations for hippie completions
		return NO_CONTEXTS;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return AUTO_ACTIVATION_CHARS;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null; // no custom error message
	}
}
