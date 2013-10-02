/*******************************************************************************
 * Copyright (c) 2012 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.beans.ui.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.DefaultReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.data.SpringDataUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IReferenceableElementsLocator} to point to Spring Data repositories by their interface instead of the actual
 * bean class (the factory).
 * 
 * @author Oliver Gierke
 */
public class RepositoriesReferenceableElementLocator extends DefaultReferenceableElementsLocator {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.editor.namespaces.DefaultReferenceableElementsLocator#getReferenceableElements(org.w3c.dom.Document, org.eclipse.core.resources.IFile)
	 */
	@Override
	public Map<String, Set<Node>> getReferenceableElements(Document document, IFile file) {

		Map<String, Set<Node>> result = super.getReferenceableElements(document, file);

		NodeList childNodes = document.getDocumentElement().getChildNodes();
		Node springDataElement = null;

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);

			if (SpringDataUtils.isSpringDataElement(node)) {
				springDataElement = node;
				break;
			}
		}

		if (file != null && file.exists()) {
			for (String name : SpringDataUtils.getRepositoryBeanIds(file.getProject())) {
				Set<Node> nodes = result.get(name);
				if (nodes == null) {
					nodes = new HashSet<Node>();
					result.put(name, nodes);
				}
				nodes.add(springDataElement);
			}
		}

		return result;
	}
}
