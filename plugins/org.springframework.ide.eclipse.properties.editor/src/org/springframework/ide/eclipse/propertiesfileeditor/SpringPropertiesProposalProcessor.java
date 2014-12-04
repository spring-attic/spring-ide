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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
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
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap.Match;

public class SpringPropertiesProposalProcessor implements IContentAssistProcessor {
	
	private static final Set<String> ASSIGNABLE_TYPES = new HashSet<String>(Arrays.asList(
			"java.lang.Boolean",
			"java.lang.String",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lan.Double",
			"java.lang.Float",
			"java.lang.Character",
			"java.util.List"
	));

	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];
	private static final char[] AUTO_ACTIVATION_CHARS = {'.'};

	public Styler JAVA_STRING_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_STRING);
		}
	};
	public Styler JAVA_KEYWORD_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_KEYWORD);
		}
	};
	public Styler JAVA_OPERATOR_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_OPERATOR);
		}
	};
	

	private final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4,
	 ICompletionProposalExtension6
	{

		private final String fPrefix;
		private final int fOffset;
		private Match<ConfigurationMetadataProperty> match;

		public Proposal(String prefix, int offset, Match<ConfigurationMetadataProperty> match) {
			fPrefix= prefix;
			fOffset= offset;
			this.match = match;
		}

		public void apply(IDocument document) {
			apply(null, '\0', 0, fOffset);
		}

		public Point getSelection(IDocument document) {
			return new Point(fOffset - fPrefix.length() + getCompletion().length(), 0);
		}

		private String getCompletion() {
			StringBuilder completion = new StringBuilder(match.data.getId());
			String defaultValue = formatDefaultValue(match.data.getDefaultValue());
			if (defaultValue!=null) {
				completion.append("=");
				completion.append(defaultValue);
			} else {
				String type = match.data.getType();
				if (ASSIGNABLE_TYPES.contains(type)) {
					completion.append("=");
				} else {
					//assume some kind of 'Object' type
					completion.append(".");
				}
			}
			return completion.toString();
		}

		private String formatDefaultValue(Object defaultValue) {
			if (defaultValue!=null) {
				if (defaultValue instanceof String) {
					return (String) defaultValue;
				} else if (defaultValue instanceof Number) {
					return ((Number)defaultValue).toString();
				} else if (defaultValue instanceof Boolean) {
					return Boolean.toString((Boolean) defaultValue);
				} else {
					//no idea what it is so ignore
				}
			}
			return null;
		}

		public String getAdditionalProposalInfo() {
			return "Some more info?";
		}

		public String getDisplayString() {
			return this.match.data.getId();
		}

		public Image getImage() {
			return null;
		}

		public IContextInformation getContextInformation() {
			return null;
		}

		public void apply(IDocument document, char trigger, int offset) {
			try {
				String replacement= getCompletion();
				document.replace(offset-fPrefix.length(), fPrefix.length(), replacement);
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
				String newPrefix = document.get(prefixStart, offset-prefixStart);
				double newScore = FuzzyMap.match(newPrefix, match.data.getId());
				if (newScore!=0.0) {
					match.score = newScore; //Score might change, but I don't think Eclipse CA will re-sort results after incremental.
					return true;
				}
			} catch (BadLocationException x) {
			}
			return false;
		}

		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return match.data.getId();
		}

		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fOffset - fPrefix.length();
		}

		public boolean isAutoInsertable() {
			return true;
		}

		@Override
		public StyledString getStyledDisplayString() {
			Styler.
			StyledString result = new StyledString();
			result.append(fPrefix, BOLD);
			result.append(fString.substring(fPrefix.length()));
			return result;
		}
		
		@Override
		public String toString() {
			return "<"+fPrefix+">"+fString.substring(fPrefix.length());
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
			if (prefix == null/* || prefix.length() == 0*/)
				return NO_PROPOSALS;

			Collection<Match<ConfigurationMetadataProperty>> suggestions = fEngine.getCompletions(viewer.getDocument(), prefix, offset);

			List<ICompletionProposal> result= new ArrayList<ICompletionProposal>();
			for (Match<ConfigurationMetadataProperty> match : suggestions) {
				result.add(new Proposal(prefix, offset, match));
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
