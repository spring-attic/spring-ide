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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.3.0
 */
@SuppressWarnings("restriction")
public class ToolAnnotationBasedHyperlinkCalculator implements IMultiHyperlinkCalculator {

	public IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
		detectAnnotationBasedHyperlinks(textViewer, hyperlinkRegion, true, hyperlinks, node);
		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink[] hyperlinks = createHyperlinks(name, target, node, parentNode, document, textViewer,
				hyperlinkRegion, cursor);
		if (hyperlinks.length > 0) {
			return hyperlinks[0];
		}
		return null;
	}

	private void detectAnnotationBasedHyperlinks(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks, List<IHyperlink> hyperlinks, Node currentNode) {
		if (currentNode != null) {
			Attr currentAttr = BeansEditorUtils.getAttrByOffset(currentNode, region.getOffset());
			IDOMAttr attr = (IDOMAttr) currentAttr;
			if (currentAttr != null && region.getOffset() >= attr.getValueRegionStartOffset()) {
				List<Element> appInfo = ToolAnnotationUtils.getApplicationInformationElements(currentNode, attr
						.getLocalName());
				for (Element elem : appInfo) {
					NodeList children = elem.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node child = children.item(j);
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							invokeAnnotationBasedHyperlinkDetector(textViewer, region, canShowMultipleHyperlinks,
									hyperlinks, child);
						}
					}
				}
			}
		}
	}

	private void invokeAnnotationBasedHyperlinkDetector(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks, List<IHyperlink> hyperlinks, Node child) {
		IAnnotationBasedHyperlinkDetector[] detectors = NamespaceUtils.getAnnotationBasedHyperlinkDetector(child
				.getNamespaceURI());
		for (IAnnotationBasedHyperlinkDetector detector : detectors) {
			IHyperlink[] detectedHyperlinks = detector.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks,
					child);
			if (detectedHyperlinks != null) {
				hyperlinks.addAll(Arrays.asList(detectedHyperlinks));
			}
		}
	}

}
