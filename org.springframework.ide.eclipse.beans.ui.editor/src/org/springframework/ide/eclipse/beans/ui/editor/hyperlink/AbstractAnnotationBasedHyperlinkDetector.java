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
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Abstract {@link IAnnotationBasedHyperlinkDetector} super class that handles
 * resolving the {@link ToolAnnotationData} and then delegates to
 * {@link #createHyperlink} for producing the {@link IHyperlink} instances.
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
public abstract class AbstractAnnotationBasedHyperlinkDetector implements
		IAnnotationBasedHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks, Node annotation) {
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
					if (isLinkableAttr(currentAttr, annotation)) {
						IRegion hyperlinkRegion = getHyperlinkRegion(currentAttr);
						IHyperlink hyperLink = createHyperlink(currentAttr
								.getName(), currentAttr.getNodeValue(),
								currentNode, currentNode.getParentNode(),
								document, textViewer, hyperlinkRegion, region,
								annotation);
						if (hyperLink != null) {
							return new IHyperlink[] { hyperLink };
						}
					}
				}
				break;

			case Node.TEXT_NODE:
				IRegion hyperlinkRegion = getHyperlinkRegion(currentNode);
				Node parentNode = currentNode.getParentNode();
				if (parentNode != null) {
					IHyperlink hyperLink = createHyperlink(parentNode
							.getNodeName(), currentNode.getNodeValue(),
							currentNode, parentNode, document, textViewer,
							hyperlinkRegion, region, annotation);
					if (hyperLink != null) {
						return new IHyperlink[] { hyperLink };
					}
				}
				break;
			}
		}
		return null;
	}

	/**
	 * Returns the text region of given node.
	 */
	protected final IRegion getHyperlinkRegion(Node node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case Node.DOCUMENT_TYPE_NODE:
			case Node.TEXT_NODE:
				IDOMNode docNode = (IDOMNode) node;
				return new Region(docNode.getStartOffset(), docNode
						.getEndOffset()
						- docNode.getStartOffset());

			case Node.ELEMENT_NODE:
				IDOMElement element = (IDOMElement) node;
				int endOffset;
				if (element.hasEndTag() && element.isClosed()) {
					endOffset = element.getStartEndOffset();
				}
				else {
					endOffset = element.getEndOffset();
				}
				return new Region(element.getStartOffset(), endOffset
						- element.getStartOffset());

			case Node.ATTRIBUTE_NODE:
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				int regLength = att.getValueRegionText().length();
				String attValue = att.getValueRegionText();
				if (StringUtils.isQuoted(attValue)) {
					regOffset += 1;
					regLength = regLength - 2;
				}
				return new Region(regOffset, regLength);
			}
		}
		return null;
	}

	public abstract IHyperlink createHyperlink(String name, String target,
			Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor,
			Node annotation);

	public abstract boolean isLinkableAttr(Attr attr, Node annotation);

}
