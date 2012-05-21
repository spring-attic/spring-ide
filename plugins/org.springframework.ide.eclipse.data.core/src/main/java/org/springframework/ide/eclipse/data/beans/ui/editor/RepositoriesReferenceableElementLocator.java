/*
 * Copyright 2011 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.data.beans.ui.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.DefaultReferenceableElementsLocator;
import org.springframework.ide.eclipse.data.SpringDataUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
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

		for (String name : SpringDataUtils.getRepositoryBeanIds(file.getProject())) {
			Set<Node> nodes = result.get(name);
			if (nodes == null) {
				nodes = new HashSet<Node>();
				result.put(name, nodes);
			}
			nodes.add(springDataElement);
		}

		return result;
	}
}
