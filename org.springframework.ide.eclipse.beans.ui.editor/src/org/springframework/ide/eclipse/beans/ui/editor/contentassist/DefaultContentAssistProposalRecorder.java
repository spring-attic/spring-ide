/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * Default implementation of {@link IContentAssistProposalRecorder} to record calculated content
 * assist proposals.
 * <p>
 * This implementation wraps the WTP internal class {@link ContentAssistRequest} and forwards the
 * recording to
 * {@link ContentAssistRequest#addProposal(org.eclipse.jface.text.contentassist.ICompletionProposal)}.
 * @author Christian Dupuis
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class DefaultContentAssistProposalRecorder implements IContentAssistProposalRecorder {

	private final ContentAssistRequest request;

	/**
	 * Creates a new {@link DefaultContentAssistProposalRecorder}.
	 */
	public DefaultContentAssistProposalRecorder(ContentAssistRequest request) {
		this.request = request;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void recordProposal(Image image, int relevance, String displayText, String replaceText) {
		recordProposal(image, relevance, displayText, replaceText, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void recordProposal(Image image, int relevance, String displayText, String replaceText,
			Object proposedObject) {
		request.addProposal(new BeansJavaCompletionProposal(replaceText, request
				.getReplacementBeginPosition(), request.getReplacementLength(), replaceText
				.length(), image, displayText, null, relevance, proposedObject));
	}

}
