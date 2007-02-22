package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import org.w3c.dom.Node;

public class WebflowNamespaceUtils {
	
	public static Node locateFlowRootNode(Node node) {
		if (!"flow".equals(node.getLocalName()) && node.getParentNode() != null) {
			return locateFlowRootNode(node.getParentNode());
		}
		else {
			return node;
		}
	}
}
