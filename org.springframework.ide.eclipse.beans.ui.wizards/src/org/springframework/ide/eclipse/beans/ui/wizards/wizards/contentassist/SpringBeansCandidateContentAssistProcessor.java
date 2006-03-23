package org.springframework.ide.eclipse.beans.ui.wizards.wizards.contentassist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Point;

public class SpringBeansCandidateContentAssistProcessor implements ISubjectControlContentAssistProcessor {
	private String errorMessage;

	private Set proposals;

	public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		IDocument doc = contentAssistSubjectControl.getDocument();
		Point selectedRange = contentAssistSubjectControl.getSelectedRange();
		List propList = new ArrayList();

		// Retrieve selected text
		String text = doc.get();
		// Compute completion proposals
		computeSpringBeansCandidateProposals(text, selectedRange, propList);
		// Create completion proposal array
		ICompletionProposal[] proposals = new ICompletionProposal[propList.size()];

		// and fill with list elements
		propList.toArray(proposals);

		// Return the proposals
		return proposals;
	}

	private void computeSpringBeansCandidateProposals(String text, Point selectedRange, List propList) {
		for (Iterator it = this.proposals.iterator(); it.hasNext();) {
			String next = (String) it.next();
			if (next.toUpperCase().startsWith(text.toUpperCase())) {
				CompletionProposal proposal = new CompletionProposal(next, 0, text.length(), next.length());
				propList.add(proposal);
			}
		}
	}

	public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		// ITextViewer not supported yet
		return null;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// ITextViewer not supported yet
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public IContextInformationValidator getContextInformationValidator() {
		// no context
		return null;
	}

	public void setProposals(Set proposals) {
		this.proposals = proposals;
	}

}
