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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.jee;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class JeeContentAssistProcessor extends AbstractContentAssistProcessor {

	private void addInterfaceAttributeValueProposals(
			ContentAssistRequest request, String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix, true);
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {
		String nodeName = node.getNodeName();
		String prefix = node.getPrefix();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("jndi-lookup".equals(nodeName)) {
			if ("expected-type".equals(attributeName)) {
				addInterfaceAttributeValueProposals(request, matchString);
			}
			else if ("proxy-interface".equals(attributeName)) {
				addInterfaceAttributeValueProposals(request, matchString);
			}
		}
		else if ("remote-slsb".equals(nodeName)
				|| "local-slsb".equals(nodeName)) {
			if ("business-interface".equals(attributeName)) {
				addInterfaceAttributeValueProposals(request, matchString);
			}
			else if ("home-interface".equals(attributeName)) {
				addInterfaceAttributeValueProposals(request, matchString);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}
}
