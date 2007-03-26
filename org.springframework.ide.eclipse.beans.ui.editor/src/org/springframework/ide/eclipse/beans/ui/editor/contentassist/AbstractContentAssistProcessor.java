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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateCompletionProcessor;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 */
@SuppressWarnings("restriction")
public abstract class AbstractContentAssistProcessor implements
		INamespaceContentAssistProcessor {

	protected IContentAssistProcessor delegatingContextAssistProcessor;

	private BeansTemplateCompletionProcessor templateProcessor = null;

	public void addAttributeNameProposals(
			IContentAssistProcessor delegatingContextAssistProcessor,
			ContentAssistRequest request) {
		this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		// have a value proposed
		IStructuredDocumentRegion open = node
				.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(request.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		String matchString = request.getMatchString();
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0
				&& (matchString.startsWith("\"") || matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			Node attribute = node.getAttributes().getNamedItem(attributeName);
			if (attribute != null) {
				String prefix = attribute.getPrefix();
				String namespace = attribute.getNamespaceURI();
				if (prefix != null) {
					attributeName = attributeName
							.substring(prefix.length() + 1);
				}
				computeAttributeNameProposals(request, attributeName,
						namespace, prefix, node);
			}
		}
		// super.addAttributeNameProposals(request);
	}

	public void addAttributeValueProposals(
			IContentAssistProcessor delegatingContextAssistProcessor,
			ContentAssistRequest request) {
		this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		// have a value proposed
		IStructuredDocumentRegion open = node
				.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(request.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		String matchString = request.getMatchString();
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0
				&& (matchString.startsWith("\"") || matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			computeAttributeValueProposals(request, node, matchString,
					attributeName);
		}
	}

	public void addTagCloseProposals(
			IContentAssistProcessor delegatingContextAssistProcessor,
			ContentAssistRequest request) {
		this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
		// add content assist proposals for incomplete tags
		this.addAttributeValueProposals(delegatingContextAssistProcessor,
				request);
	}

	public void addTagInsertionProposals(
			IContentAssistProcessor delegatingContextAssistProcessor,
			ContentAssistRequest request, int childPosition) {
		this.delegatingContextAssistProcessor = delegatingContextAssistProcessor;
		IDOMNode node = (IDOMNode) request.getNode();
		computeTagInsertionProposals(request, node);
	}

	/**
	 * Adds templates to the list of proposals
	 * 
	 * @param contentAssistRequest
	 * @param context
	 */
	protected void addTemplates(ContentAssistRequest contentAssistRequest,
			String context) {
		if (contentAssistRequest == null)
			return;

		// if already adding template proposals for a certain context type, do
		// not add again
		// if (!fTemplateContexts.contains(context)) {
		// fTemplateContexts.add(context);
		boolean useProposalList = !contentAssistRequest.shouldSeparate();

		if (getTemplateCompletionProcessor() != null) {
			getTemplateCompletionProcessor().setContextType(context);
			ICompletionProposal[] proposals = getTemplateCompletionProcessor()
					.computeCompletionProposals(
							((DelegatingContentAssistProcessor) this.delegatingContextAssistProcessor)
									.getTextViewer(),
							contentAssistRequest.getReplacementBeginPosition());
			for (ICompletionProposal element : proposals) {
				if (useProposalList)
					contentAssistRequest.addProposal(element);
				else
					contentAssistRequest.addMacro(element);
			}
		}
	}

	protected abstract void computeAttributeNameProposals(
			ContentAssistRequest request, String prefix, String namespace,
			String namespacePrefix, Node attributeNode);

	protected abstract void computeAttributeValueProposals(
			ContentAssistRequest request, IDOMNode node, String matchString,
			String attributeName);

	protected abstract void computeTagInsertionProposals(
			ContentAssistRequest request, IDOMNode node);

	/**
	 * Returns project request is in
	 * 
	 * @param request
	 * @return
	 */

	protected BeansTemplateCompletionProcessor getTemplateCompletionProcessor() {
		if (templateProcessor == null) {
			templateProcessor = new BeansTemplateCompletionProcessor();
		}
		return templateProcessor;
	}

}
