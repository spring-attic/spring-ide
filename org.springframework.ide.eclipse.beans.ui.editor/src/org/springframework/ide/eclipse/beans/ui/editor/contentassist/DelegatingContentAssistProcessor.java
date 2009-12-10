/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * {@link IContentAssistProcessor} that delegates to {@link INamespaceContentAssistProcessor}s contribute via the
 * <code>org.springframework.ide.eclipse.beans.ui.editor</code> extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {

		int proposalCount = 0;
		if (contentAssistRequest.getCompletionProposals() != null) {
			proposalCount = contentAssistRequest.getCompletionProposals().length;
		}

		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeValueProposals(this, contentAssistRequest);
		}

		// only calculate content assists based on annotations if no other processor kicked in already.
		if (contentAssistRequest.getCompletionProposals() == null
				|| contentAssistRequest.getCompletionProposals().length == proposalCount) {
			new ToolAnnotationContentAssistProcessor().addAttributeValueProposals(this, contentAssistRequest);
		}

		super.addAttributeValueProposals(contentAssistRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeNameProposals(this, request);
		}
		super.addAttributeNameProposals(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	/**
	 * {@link INamespaceContentAssistProcessor} implementation that wraps a {@link ToolAnnotationContentAssistCalulator}
	 * to create content assist proposals basd on tool annotations.
	 * @since 2.3.0
	 */
	private static class ToolAnnotationContentAssistProcessor extends AbstractContentAssistProcessor {

		private final ToolAnnotationContentAssistCalulator calculator = new ToolAnnotationContentAssistCalulator();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace,
				String namespacePrefix, Node attributeNode) {
			// nothing do on annotations
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
				String attributeName, String namespace, String prefix) {
			IContentAssistContext context = new DefaultContentAssistContext(request, attributeName, BeansEditorUtils
					.prepareMatchString(matchString));
			IContentAssistProposalRecorder recorder = new DefaultContentAssistProposalRecorder(request);

			calculator.computeProposals(context, recorder);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
			// nothing do on annotations
		}

	}

}
