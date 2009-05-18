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
package org.springframework.ide.eclipse.batch.ui.editor.hyperlink.batch;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.HyperlinkUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IHyperlinkCalculator} implementation that can be used to locate references to
 * <code>step<step> elements.
 * @author Leo Dos Santos
 * @since 2.2.5
 */
public class StepReferenceHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode,
			IDocument document, ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		if (node.getOwnerDocument() != null && node.getOwnerDocument().getDocumentElement() != null) {
			return searchStepElements(target, node.getOwnerDocument().getDocumentElement(),
					textViewer, hyperlinkRegion);
		}
		return null;
	}

	private IHyperlink searchStepElements(String name, Node node, ITextViewer textViewer,
			IRegion hyperlinkRegion) {
		NodeList children = node.getChildNodes();
		IHyperlink link = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("step".equals(child.getLocalName())) {
				if (name.equals(BeansEditorUtils.getAttribute(child, "id"))) {
					IRegion region = HyperlinkUtils.getHyperlinkRegion(child);
					link = new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
				}
			}
			if (link == null && child.hasChildNodes()) {
				link = searchStepElements(name, child, textViewer, hyperlinkRegion);
			}
		}
		return link;
	}

}
