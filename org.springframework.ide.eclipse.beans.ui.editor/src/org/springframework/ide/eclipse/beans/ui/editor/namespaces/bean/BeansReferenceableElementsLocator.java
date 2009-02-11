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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BeansReferenceableElementsLocator implements
		IReferenceableElementsLocator {

	public static final String BEAN_NAME_DELIMITERS = ",; ";

	public Map<String, Node> getReferenceableElements(Document document, IFile file) {
		Map<String, Node> nodes = new HashMap<String, Node>();
		NodeList childNodes = document.getDocumentElement().getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if ("bean".equals(node.getNodeName())) {
				if (BeansEditorUtils.hasAttribute(node, "id")) {
					nodes.put(BeansEditorUtils.getAttribute(node, "id"), node);
				}
				if (BeansEditorUtils.hasAttribute(node, "name")) {
					String aliasesString = BeansEditorUtils.getAttribute(node,
							"name");
					String[] nameArr = StringUtils.tokenizeToStringArray(
							aliasesString, BEAN_NAME_DELIMITERS);
					for (String name : nameArr) {
						nodes.put(name, node);
					}
				}
			}
		}
		return nodes;
	}
}
