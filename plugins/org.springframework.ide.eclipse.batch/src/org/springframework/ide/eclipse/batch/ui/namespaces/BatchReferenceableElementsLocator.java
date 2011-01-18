/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.namespaces;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IReferenceableElementsLocator} for steps and other Spring Batch related artifacts.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.3
 */
public class BatchReferenceableElementsLocator implements IReferenceableElementsLocator {

	public Map<String, Node> getReferenceableElements(Document document,
			IFile file) {
		return getReferenceableElementsHelper(document.getDocumentElement());
	}
	
	private Map<String, Node> getReferenceableElementsHelper(Node parent) {
		Map<String, Node> nodes = new HashMap<String, Node>();
		NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (BeansEditorUtils.hasAttribute(node, "id")) {
				nodes.put(BeansEditorUtils.getAttribute(node, "id"), node);
			}
			if (node.hasChildNodes()) {
				nodes.putAll(getReferenceableElementsHelper(node));
			}
		}
		return nodes;
	}

}
