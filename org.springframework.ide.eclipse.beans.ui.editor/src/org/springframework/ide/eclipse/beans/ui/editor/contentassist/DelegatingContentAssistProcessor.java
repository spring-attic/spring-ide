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

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IAnnotationBasedContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistProcessor} that delegates to {@link INamespaceContentAssistProcessor}s
 * contribute via the <code>org.springframework.ide.eclipse.beans.ui.editor</code> extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {

		int proposalCount = 0;
		if (contentAssistRequest.getCompletionProposals() != null) {
			proposalCount = contentAssistRequest.getCompletionProposals().length;
		}

		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils
				.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeValueProposals(this, contentAssistRequest);
		}

		// only calculate content assists based on annotations if no other processor
		// kicked in already.
		if (contentAssistRequest.getCompletionProposals() == null
				|| contentAssistRequest.getCompletionProposals().length == proposalCount) {
			addAnnotationBasedAttributeValueProposals(contentAssistRequest, node);
		}

		super.addAttributeValueProposals(contentAssistRequest);
	}

	private void addAnnotationBasedAttributeValueProposals(
			ContentAssistRequest contentAssistRequest, IDOMNode node) {

		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(contentAssistRequest.getRegion());
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

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			List<Element> appInfo = ToolAnnotationUtils.getApplicationInformationElements(node,
					attributeName);
			for (Element elem : appInfo) {
				NodeList children = elem.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						invokeAnnotationBasedContentAssistProcessor(contentAssistRequest, child);
					}
				}
			}
		}
	}

	private void invokeAnnotationBasedContentAssistProcessor(
			ContentAssistRequest contentAssistRequest, Node child) {

		IAnnotationBasedContentAssistProcessor[] annotationProcessors = NamespaceUtils
				.getAnnotationBasedContentAssistProcessor(child.getNamespaceURI());
		for (IAnnotationBasedContentAssistProcessor annotationProcessor : annotationProcessors) {
			annotationProcessor.addAttributeValueProposals(this, contentAssistRequest, child);
		}
	}

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils
				.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeNameProposals(this, request);
		}
		super.addAttributeNameProposals(request);
	}

	@Override
	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils
				.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils
				.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

}
