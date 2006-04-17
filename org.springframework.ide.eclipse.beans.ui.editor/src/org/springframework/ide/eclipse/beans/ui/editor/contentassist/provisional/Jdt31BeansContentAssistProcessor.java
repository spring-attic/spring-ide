package org.springframework.ide.eclipse.beans.ui.editor.contentassist.provisional;

import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;

/**
 * Provisional content assist processor to support Eclipse 3.1.x JDT API 
 * @author Christian Dupuis
 */
public class Jdt31BeansContentAssistProcessor extends
		BeansContentAssistProcessor {

	protected void processJavaCompletionProposal(ContentAssistRequest request,
			ICompletionProposal comProposal) {
		if (comProposal instanceof JavaCompletionProposal) {
			JavaCompletionProposal prop = (JavaCompletionProposal) comProposal;
			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
					prop.getReplacementString(), request
							.getReplacementBeginPosition(), request
							.getReplacementLength(), prop
							.getReplacementString().length(), prop.getImage(),
					prop.getDisplayString(), null, prop
							.getAdditionalProposalInfo(), prop.getRelevance());

			request.addProposal(proposal);
		} else if (comProposal instanceof LazyJavaTypeCompletionProposal) {
			{
				LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) comProposal;
				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
						prop.getReplacementString(), request
								.getReplacementBeginPosition(), request
								.getReplacementLength(), prop
								.getReplacementString().length(), prop
								.getImage(), prop.getDisplayString(), null,
						prop.getAdditionalProposalInfo(), prop.getRelevance());

				request.addProposal(proposal);
			}
		}
	}
}
