/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.HyperlinkUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.ui.editor.WebflowNamespaceUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IHyperlinkCalculator} for state references.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.3.4
 */
public class StateReferenceHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		Node flowNode = WebflowNamespaceUtils.locateFlowRootNode(node);
		NodeList nodes = flowNode.getChildNodes();
		if (nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				String id = BeansEditorUtils.getAttribute(nodes.item(i), "id");
				if (target.equals(id)) {
					IRegion region = HyperlinkUtils.getHyperlinkRegion(nodes.item(i));
					return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
				}
			}
		}
		return null;
	}

}
