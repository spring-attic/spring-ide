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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.editor.support.EditorSupportActivator;

/**
 * Provides methods for creating completion proposals.
 *
 * @author Kris De Volder
 */
public class CompletionFactory {

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

}
