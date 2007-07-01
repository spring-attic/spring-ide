/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.mylyn.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.context.ui.ContextUiImages;
import org.eclipse.mylyn.internal.java.ui.editor.FocusedJavaProposalProcessor;
import org.eclipse.wst.sse.ui.internal.contentassist.IRelevanceCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ProposalComparator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;

/**
 * This {@link IContentAssistProcessor} reorders the calculated
 * {@link IContentProposal} based on the current interest level of the proposed
 * elements.
 * <p>
 * This implementation is greatly influenced by Mylyn's
 * {@link FocusedJavaProposalProcessor}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class FocusedStructuredTextViewerContentAssistProcessor implements
		IContentAssistProcessor {

	static class FocusedProposalSeparator extends JavaCompletionProposal
			implements IRelevanceCompletionProposal {
		public FocusedProposalSeparator(int baseRelevance) {
			super("", 0, 0, ContextUiImages
					.getImage(ContextUiImages.CONTENT_ASSIST_SEPARATOR),
					LABEL_SEPARATOR, THRESHOLD_INTEREST + baseRelevance);
		}
	}

	private static final int THRESHOLD_INTEREST = 10000;

	private static final int THRESHOLD_IMPLICIT_INTEREST = THRESHOLD_INTEREST * 2;

	private static final int RELEVANCE_IMPLICIT_INTEREST = 300;

	private static final String IDENTIFIER_THIS = "this";

	public static final String LABEL_SEPARATOR = " -------------------------------------------- ";

	private final IContentAssistProcessor processor;

	public FocusedStructuredTextViewerContentAssistProcessor(
			IContentAssistProcessor processor) {
		this.processor = processor;
	}

	private boolean boostRelevanceWithInterest(
			BeansJavaCompletionProposal proposal, int baseRelevance) {
		boolean hasInteresting = false;
		Object element = proposal.getProposedObject();
		if (element != null) {
			String handle = determineHandleForProposedElement(element);
			if (handle != null) {
				hasInteresting = determineRelevanceForProposedElements(
						proposal, baseRelevance, handle);
			}
		}
		else if (isImplicitlyInteresting(proposal)) {
			proposal.setRelevance(THRESHOLD_IMPLICIT_INTEREST
					+ proposal.getRelevance());
			hasInteresting = true;
		}
		return hasInteresting;
	}

	@SuppressWarnings("unchecked")
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		ICompletionProposal[] rawProposals = processor
				.computeCompletionProposals(viewer, offset);
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		proposals.addAll(Arrays.asList(rawProposals));

		if (!ContextCorePlugin.getContextManager().isContextActive()) {
			return rawProposals;
		}
		else {
			int baseRelevance = 0;
			for (Object object : proposals) {
				if (object instanceof IRelevanceCompletionProposal
						&& ((IRelevanceCompletionProposal) object)
								.getRelevance() > baseRelevance) {
					baseRelevance = ((IRelevanceCompletionProposal) object)
							.getRelevance();
				}
			}

			int foundInterestingCount = 0;
			boolean hasInterestingProposals = false;
			for (Object object : proposals) {
				if (object instanceof BeansJavaCompletionProposal) {
					boolean foundInteresting = boostRelevanceWithInterest(
							(BeansJavaCompletionProposal) object, baseRelevance);
					if (!hasInterestingProposals && foundInteresting) {
						hasInterestingProposals = true;
					}
					if (foundInteresting) {
						foundInterestingCount++;
					}
				}
			}

			if (hasInterestingProposals
					&& proposals.size() > foundInterestingCount) {
				proposals.add(new FocusedProposalSeparator(baseRelevance));
			}
		}

		ICompletionProposal[] newProposals = proposals
				.toArray(new ICompletionProposal[proposals.size()]);
		Arrays.sort(newProposals, new ProposalComparator());
		return newProposals;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return processor.computeContextInformation(viewer, offset);
	}

	protected String determineHandleForProposedElement(Object element) {
		if (element != null && element instanceof IJavaElement) {
			return ((IJavaElement) element).getHandleIdentifier();
		}
		return null;
	}

	private boolean determineRelevanceForProposedElements(
			BeansJavaCompletionProposal proposal, int baseRelevance,
			String handle) {
		IInteractionElement mylarElement = ContextCorePlugin
				.getContextManager().getElement(handle);
		float interest = mylarElement.getInterest().getValue();
		if (interest > InteractionContextManager.getScalingFactors()
				.getInteresting()) {
			proposal.setRelevance(THRESHOLD_INTEREST + baseRelevance
					+ (int) (interest * 10));
			return true;
		}
		return false;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return processor.getCompletionProposalAutoActivationCharacters();
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return processor.getContextInformationAutoActivationCharacters();
	}

	public IContextInformationValidator getContextInformationValidator() {
		return processor.getContextInformationValidator();
	}

	public String getErrorMessage() {
		return processor.getErrorMessage();
	}

	public boolean isImplicitlyInteresting(BeansJavaCompletionProposal proposal) {
		return proposal.getRelevance() > RELEVANCE_IMPLICIT_INTEREST
				&& !IDENTIFIER_THIS.equals(proposal.getDisplayString());
	}
}