/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.HoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesInformationControlCreator;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.util.StringUtil;

public class PropertyCompletionFactory {

	public ICompletionProposal property(IDocument doc, ProposalApplier applier, Match<PropertyInfo> prop, TypeUtil typeUtil) {
		return new PropertyProposal(doc, applier, prop, typeUtil);
	}

	public ICompletionProposal valueProposal(String value, Type type, int order, ProposalApplier applier) {
		return valueProposal(value, type, -(1.0+order), applier);
	}

	public ICompletionProposal valueProposal(String value, Type type, double score, ProposalApplier applier) {
		return simpleProposal(value, score, applier);
	}

	public ICompletionProposal beanProperty(IDocument doc, final String contextProperty, final Type contextType, final String pattern, final TypedProperty p, final double score, ProposalApplier applier, final TypeUtil typeUtil) {
		return new AbstractPropertyProposal(doc, applier, typeUtil) {

			private HoverInfo hoverInfo;

			@Override
			public HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor) {
				if (hoverInfo==null) {
					hoverInfo = new TypeNavigationHoverInfo(contextProperty+"."+p.getName(), p.getName(), contextType, p.getType(), this.typeUtil);
				}
				return hoverInfo;
			}

			@Override
			protected String getBaseDisplayString() {
				return p.getName();
			}

			@Override
			protected String getHighlightPattern() {
				return pattern;
			}

			@Override
			public double getScore() {
				return score;
			}

			@Override
			protected Type getType() {
				return p.getType();
			}
		};
	}

	public ICompletionProposal simpleProposal(String name, int sortingOrder, ProposalApplier applier) {
		return simpleProposal(name, -(1.0+sortingOrder), applier);
	}

	public ICompletionProposal simpleProposal(String name, double score, ProposalApplier applier) {
		return new SimpleProposal(name, score, applier);
	}

	private static abstract class ScoreableProposal implements ICompletionProposal {
		public abstract double getScore();
	}

	private static class SimpleProposal extends ScoreableProposal {

		private String value;
		private double score;
		private ProposalApplier applier;

		public SimpleProposal(String value, double score, ProposalApplier applier) {
			this.value = value;
			this.score = score;
			this.applier = applier;
		}

		@Override
		public void apply(IDocument doc) {
			try {
				applier.apply(doc);
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}

		@Override
		public Point getSelection(IDocument doc) {
			try {
				return applier.getSelection(doc);
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return null;
		}

		@Override
		public String getAdditionalProposalInfo() {
			return null;
		}

		@Override
		public String getDisplayString() {
			return value;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public double getScore() {
			return score;
		}

	}

	/**
	 * A sorter suitable for sorting proposals created by this factory
	 */
	public static final ICompletionProposalSorter SORTER = new ICompletionProposalSorter() {
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			if (p1 instanceof ScoreableProposal && p2 instanceof ScoreableProposal) {
				double s1 = ((ScoreableProposal)p1).getScore();
				double s2 = ((ScoreableProposal)p2).getScore();
				if (s1==s2) {
					String name1 = ((ScoreableProposal)p1).getDisplayString();
					String name2 = ((ScoreableProposal)p2).getDisplayString();
					return name1.compareTo(name2);
				} else {
					return Double.compare(s2, s1);
				}
			}
			return 0;
		}
	};

	private DocumentContextFinder documentContextFinder;

	private static final Styler UNDERLINE = new Styler() {
		public void applyStyles(TextStyle textStyle) {
			textStyle.underline = true;
		};
	};

	public PropertyCompletionFactory(DocumentContextFinder documentContextFinder) {
		this.documentContextFinder = documentContextFinder;
	}

	private abstract class AbstractPropertyProposal extends ScoreableProposal implements ICompletionProposalExtension3,
	ICompletionProposalExtension4, ICompletionProposalExtension5, ICompletionProposalExtension6
	{

		protected final IDocument fDoc;
		private final ProposalApplier proposalApplier;
		protected final TypeUtil typeUtil;

		public AbstractPropertyProposal(IDocument doc, ProposalApplier applier, TypeUtil typeUtil) {
			this.proposalApplier = applier;
			this.fDoc = doc;
			this.typeUtil = typeUtil;
		}

		public Point getSelection(IDocument document) {
			try {
				return proposalApplier.getSelection(document);
			} catch (Exception e) {
				BootActivator.log(e);
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
		public boolean isAutoInsertable() {
			return true;
		}

		@Override
		public StyledString getStyledDisplayString() {
			StyledString result = new StyledString();
			highlightPattern(getHighlightPattern(), getBaseDisplayString(), result);
			Type type = getType();
			if (type!=null) {
				String typeStr = typeUtil.niceTypeName(type);
				result.append(" : "+typeStr, StyledString.DECORATIONS_STYLER);
			}
			return result;
		}

		protected abstract Type getType();
		protected abstract String getHighlightPattern();
		protected abstract String getBaseDisplayString();

		private void highlightPattern(String pattern, String data, StyledString result) {
			if (StringUtil.hasText(pattern)) {
				int dataPos = 0;	int dataLen = data.length();
				int patternPos = 0; int patternLen = pattern.length();

				while (dataPos<dataLen && patternPos<patternLen) {
					int pChar = pattern.charAt(patternPos++);
					int highlightPos = data.indexOf(pChar, dataPos);
					if (dataPos<highlightPos) {
						result.append(data.substring(dataPos, highlightPos));
					}
					result.append(data.charAt(highlightPos), UNDERLINE);
					dataPos = highlightPos+1;
				}
				if (dataPos<dataLen) {
					result.append(data.substring(dataPos));
				}
			} else { //no pattern to highlight
				result.append(data);
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
				BootActivator.log(e);
			}
		}

		@Override
		public IInformationControlCreator getInformationControlCreator() {
			return new SpringPropertiesInformationControlCreator("F2 for focus");
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

	private class PropertyProposal extends AbstractPropertyProposal {
		private Match<PropertyInfo> match;
		private Type type;

		public PropertyProposal(IDocument doc, ProposalApplier applier, Match<PropertyInfo> match,
				TypeUtil typeUtil) {
			super(doc, applier, typeUtil);
			this.match = match;
		}

		@Override
		public HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor) {
			return new SpringPropertyHoverInfo(documentContextFinder.getJavaProject(fDoc), match.data);
		}

		@Override
		protected String getBaseDisplayString() {
			return match.data.getId();
		}

		@Override
		public double getScore() {
			return match.score;
		}

		@Override
		protected Type getType() {
			if (type==null) {
				type = TypeParser.parse(match.data.getType());
			}
			return type;
		}

		@Override
		protected String getHighlightPattern() {
			return match.getPattern();
		}
	}

}
