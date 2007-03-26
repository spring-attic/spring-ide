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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;

@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeValueProposals(this, request);
		}
		super.addAttributeValueProposals(request);
	}

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeNameProposals(this, request);
		}
		super.addAttributeNameProposals(request);
	}

	@Override
	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request,
			int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '=', '\"', '<' };
	}
}
