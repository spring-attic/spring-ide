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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.db;

import java.sql.Driver;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class DbContentAssistProcessor extends AbstractContentAssistProcessor {

	/**
	 * Adds content assist proposals for attribute value requests
	 * @param request the content assist request
	 * @param node the node
	 * @param matchString the prefix the user has entered
	 * @param attributeName the attributeName
	 */
	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {

		String nodeName = node.getLocalName();

		if ("driver-manager-data-source".equals(nodeName)) {
			if ("driver-class-name".equals(attributeName)) {
				BeansJavaCompletionUtils
						.addTypeHierachyAttributeValueProposals(request,
								matchString, Driver.class.getName());
			}
			else if ("connection-properties-ref".equals(attributeName)) {
				BeansCompletionUtils.addBeanReferenceProposals(request,
						matchString, node.getOwnerDocument(), true);
			}
		}
	}

	/**
	 * Adds content assist proposals for adding tags
	 */
	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}

	/**
	 * Adds content assist proposals for attribute name requests
	 * @param request the content assist request
	 * @param prefix the prefix the user has entered
	 * @param namespace the namespace of the current XML element
	 * @param namespacePrefix the namespacePrefix
	 * @param attributeNode the node
	 */
	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}
}
