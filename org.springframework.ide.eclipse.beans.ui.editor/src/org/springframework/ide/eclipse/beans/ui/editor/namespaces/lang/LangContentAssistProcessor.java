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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.lang;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class LangContentAssistProcessor extends AbstractContentAssistProcessor {

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

		if ("jruby".equals(node.getLocalName())
				|| "bsh".equals(node.getLocalName())) {
			if ("script-interfaces".equals(attributeName)) {
				addInterfaceAttributeValueProposals(request, matchString);
			}
		}
		else if ("groovy".equals(node.getLocalName())) {
			if ("customizer-ref".equals(attributeName)) {
				BeansCompletionUtils.addBeanReferenceProposals(request,
						matchString, node.getOwnerDocument(), true);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}
}
