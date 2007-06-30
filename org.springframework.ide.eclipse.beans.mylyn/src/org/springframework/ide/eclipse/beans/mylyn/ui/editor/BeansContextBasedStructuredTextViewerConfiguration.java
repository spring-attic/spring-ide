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
package org.springframework.ide.eclipse.beans.mylyn.ui.editor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.beans.ui.editor.BeansStructuredTextViewerConfiguration;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class BeansContextBasedStructuredTextViewerConfiguration extends
		BeansStructuredTextViewerConfiguration {

	@Override
	public IContentAssistProcessor[] getContentAssistProcessors(
			ISourceViewer sourceViewer, String partitionType) {

		IContentAssistProcessor[] processors = super
				.getContentAssistProcessors(sourceViewer, partitionType);
		if (processors != null) {
			IContentAssistProcessor[] wrappedProcessors = new IContentAssistProcessor[processors.length];
			for (int i = 0; i < processors.length; i++) {
				wrappedProcessors[i] = new BeansContextBasedContentAssistProcessor(
						processors[i]);
			}
			return wrappedProcessors;
		}
		return processors;
	}

	private static class BeansContextBasedContentAssistProcessor implements
			IContentAssistProcessor {

		private final IContentAssistProcessor processor;

		public BeansContextBasedContentAssistProcessor(
				IContentAssistProcessor processor) {
			this.processor = processor;
		}

		public ICompletionProposal[] computeCompletionProposals(
				ITextViewer viewer, int offset) {
			return processor.computeCompletionProposals(viewer, offset);
		}

		public IContextInformation[] computeContextInformation(
				ITextViewer viewer, int offset) {
			return processor.computeContextInformation(viewer, offset);
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
	}
}
