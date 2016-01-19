/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.completions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.springframework.ide.eclipse.editor.support.EditorSupportActivator;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.ColorManager;
import org.springframework.ide.eclipse.editor.support.yaml.completions.AbstractPropertyProposal;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypedProperty;

/**
 * @author Kris De Volder
 */
public class CompletionFactory {

	public static final Styler UNDERLINE = new Styler() {
		public void applyStyles(TextStyle textStyle) {
			textStyle.underline = true;
		};
	};

	public static final Styler GREY_UNDERLINE = new Styler() {
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = ColorManager.getInstance().getColor(ColorManager.GREY);
			textStyle.underline = true;
		};
	};

	public static final Styler GREY = new Styler() {
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = ColorManager.getInstance().getColor(ColorManager.GREY);
		};
	};

	public static final Styler NULL_STYLER = new Styler() {
		public void applyStyles(TextStyle textStyle) {
		};
	};


	public static final CompletionFactory DEFAULT = new CompletionFactory();

	public ScoreableProposal simpleProposal(String name, int sortingOrder, ProposalApplier applier) {
		return simpleProposal(name, -(1.0+sortingOrder), applier);
	}

	public ScoreableProposal simpleProposal(String name, double score, ProposalApplier applier) {
		return new SimpleProposal(name, score, applier);
	}

	public static abstract class ScoreableProposal implements ICompletionProposal, ICompletionProposalExtension4 {
		private static final double DEEMP_VALUE = 100000; // should be large enough to move deemphasized stuff to bottom of list.

		private double deemphasizedBy = 0.0;
		public abstract double getBaseScore();
		public final double getScore() {
			return getBaseScore() - deemphasizedBy;
		}
		public ScoreableProposal deemphasize() {
			deemphasizedBy+= DEEMP_VALUE;
			return this;
		}
		public boolean isDeemphasized() {
			return deemphasizedBy > 0;
		}

		@Override
		public boolean isAutoInsertable() {
			return !isDeemphasized();
		}
	}

	private static class SimpleProposal extends ScoreableProposal {

		private String value;
		private ProposalApplier applier;
		private double score;

		public SimpleProposal(String value, double score, ProposalApplier applier) {
			this.score = score;
			this.value = value;
			this.applier = applier;
		}

		@Override
		public void apply(IDocument doc) {
			try {
				applier.apply(doc);
			} catch (Exception e) {
				EditorSupportActivator.log(e);
			}
		}

		@Override
		public Point getSelection(IDocument doc) {
			try {
				return applier.getSelection(doc);
			} catch (Exception e) {
				EditorSupportActivator.log(e);
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
		public double getBaseScore() {
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

	public ScoreableProposal beanProperty(IDocument doc, final String contextProperty, final YType contextType, final String pattern, final YTypedProperty p, final double score, ProposalApplier applier, final YTypeUtil typeUtil) {
		return new AbstractPropertyProposal(doc, applier) {

			//TODO: pull up the implementaton of getAdditionalProposalInfo from
			// org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory.beanProperty(IDocument, String, Type, String, TypedProperty, double, ProposalApplier, TypeUtil)
			// Then delete the overridden method in subclass.

			@Override
			public double getBaseScore() {
				return score;
			}

			@Override
			protected String niceTypeName(YType type) {
				return typeUtil.niceTypeName(type);
			}

			@Override
			protected YType getType() {
				return p.getType();
			}

			@Override
			protected String getHighlightPattern() {
				return pattern;
			}

			@Override
			protected String getBaseDisplayString() {
				return p.getName();
			}

			@Override
			public HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor) {
				return null;
			}
		};
	}

	public ICompletionProposal valueProposal(String value, YType yType, double score, ProposalApplier applier) {
		//TODO; sort out the alternate implementation in super class. Should not be needed.
		return simpleProposal(value, score, applier);
	}

}
