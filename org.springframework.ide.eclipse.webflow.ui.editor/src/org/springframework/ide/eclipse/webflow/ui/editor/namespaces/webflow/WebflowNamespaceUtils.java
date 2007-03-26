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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import org.w3c.dom.Node;

/**
 * 
 */
public class WebflowNamespaceUtils {
	
	/**
	 * 
	 * 
	 * @param node 
	 * 
	 * @return 
	 */
	public static Node locateFlowRootNode(Node node) {
		if (!"flow".equals(node.getLocalName()) && node.getParentNode() != null) {
			return locateFlowRootNode(node.getParentNode());
		}
		else {
			return node;
		}
	}
}
