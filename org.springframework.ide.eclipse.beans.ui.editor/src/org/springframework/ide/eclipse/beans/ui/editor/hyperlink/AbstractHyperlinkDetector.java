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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class AbstractHyperlinkDetector implements IHyperlinkDetector,
		INamespaceHyperlinkDetector {
	
	public void init() {
	}
	
	public final IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();
		Node currentNode = BeansEditorUtils.getNodeByOffset(document, region
				.getOffset());
		if (currentNode != null) {
			switch (currentNode.getNodeType()) {
			case Node.ELEMENT_NODE:
				// at first try to handle selected attribute value
				Attr currentAttr = BeansEditorUtils.getAttrByOffset(
						currentNode, region.getOffset());
				IDOMAttr attr = (IDOMAttr) currentAttr;
				if (currentAttr != null
						&& region.getOffset() >= attr
								.getValueRegionStartOffset()) {
					if (isLinkableAttr(currentAttr)) {
						IRegion hyperlinkRegion = HyperlinkUtils.getHyperlinkRegion(currentAttr);
						IHyperlink hyperLink = createHyperlink(currentAttr
								.getName(), currentAttr.getNodeValue(),
								currentNode, currentNode.getParentNode(),
								document, textViewer, hyperlinkRegion, region);
						if (hyperLink != null) {
							return new IHyperlink[] { hyperLink };
						}
					}
				}
				break;

			case Node.TEXT_NODE:
				IRegion hyperlinkRegion = HyperlinkUtils.getHyperlinkRegion(currentNode);
				Node parentNode = currentNode.getParentNode();
				if (parentNode != null) {
					IHyperlink hyperLink = createHyperlink(parentNode
							.getNodeName(), currentNode.getNodeValue(),
							currentNode, parentNode, document, textViewer,
							hyperlinkRegion, region);
					if (hyperLink != null) {
						return new IHyperlink[] { hyperLink };
					}
				}
				break;
			}
		}
		return null;
	}

}
