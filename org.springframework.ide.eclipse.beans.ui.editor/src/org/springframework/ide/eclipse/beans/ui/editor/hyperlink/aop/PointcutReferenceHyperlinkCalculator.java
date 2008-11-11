/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.aop;

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
 * {@link IHyperlinkCalculator} for the pointcut reference attribute.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
public class PointcutReferenceHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode,
			IDocument document, ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink hyperlink = searchPointcutElements(target, parentNode, textViewer,
				hyperlinkRegion);
		if (hyperlink == null && parentNode.getParentNode() != null) {
			hyperlink = searchPointcutElements(target, parentNode.getParentNode(), textViewer,
					hyperlinkRegion);
		}
		return hyperlink;
	}

	private IHyperlink searchPointcutElements(String name, Node node, ITextViewer textViewer,
			IRegion hyperlinkRegion) {
		NodeList beanNodes = node.getChildNodes();
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			if ("pointcut".equals(beanNode.getLocalName())) {
				if (name.equals(BeansEditorUtils.getAttribute(beanNode, "id"))) {
					IRegion region = HyperlinkUtils.getHyperlinkRegion(beanNode);
					return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
				}
			}
		}
		return null;
	}

}
