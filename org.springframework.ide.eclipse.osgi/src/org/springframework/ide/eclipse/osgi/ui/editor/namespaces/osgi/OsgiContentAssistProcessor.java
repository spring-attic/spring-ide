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
package org.springframework.ide.eclipse.osgi.ui.editor.namespaces.osgi;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class OsgiContentAssistProcessor extends
		AbstractContentAssistProcessor {

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Node node) {
		BeansCompletionUtils.addBeanReferenceProposals(request, prefix, node
				.getOwnerDocument(), true);
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix, true);
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {
		String nodeName = node.getLocalName();
		if ("service".equals(nodeName)) {
			// bean
			if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node);
			}
			else if ("depends-on".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node);
			}
			else if ("interface".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("reference".equals(nodeName)) {
			// bean
			if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node);
			}
			else if ("depends-on".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node);
			}
			else if ("interface".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}
}
