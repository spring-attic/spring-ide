package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.INamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.IReferenceableElementsLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class DefaultReferenceableElementsLocator implements
		IReferenceableElementsLocator {

	private INamespaceAwareEditorContribution contribution;

	DefaultReferenceableElementsLocator(INamespaceAwareEditorContribution contribution) {
		this.contribution = contribution;
	}

	public Map<String, Node> getReferenceableElements(Document document) {
		Map<String, Node> nodes = new HashMap<String, Node>();
		NodeList childNodes = document.getDocumentElement().getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (this.contribution.getNamespaceURI().equals(node.getNamespaceURI())
					&& BeansEditorUtils.hasAttribute(node, "id")) {
				nodes.put(BeansEditorUtils.getAttribute(node, "id"), node);
			}
		}
		return nodes;
	}
}