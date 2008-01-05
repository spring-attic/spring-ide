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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IHyperlinkDetector} implementation that delegates to
 * {@link IHyperlinkDetector}s that are contributed over the namespace
 * extension point.
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class DelegatingHyperlinkDetector implements IHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();

		IDocument document = textViewer.getDocument();
		Node currentNode = BeansEditorUtils.getNodeByOffset(document, region
				.getOffset());

		detectHyperlinks(textViewer, region, canShowMultipleHyperlinks,
				hyperlinks, currentNode);

		if (hyperlinks.size() == 0 || canShowMultipleHyperlinks) {
			detectAnnotationBasedHyperlinks(textViewer, region,
					canShowMultipleHyperlinks, hyperlinks, currentNode);
		}

		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}

	private void detectAnnotationBasedHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks,
			List<IHyperlink> hyperlinks, Node currentNode) {
		if (currentNode != null) {
			Attr currentAttr = BeansEditorUtils.getAttrByOffset(currentNode,
					region.getOffset());
			IDOMAttr attr = (IDOMAttr) currentAttr;
			if (currentAttr != null
					&& region.getOffset() >= attr.getValueRegionStartOffset()) {
				List<Element> appInfo = ToolAnnotationUtils
						.getApplicationInformationElements(currentNode, attr
								.getLocalName());
				for (Element elem : appInfo) {
					NodeList children = elem.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node child = children.item(j);
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							invokeAnnotationBasedHyperlinkDetector(textViewer,
									region, canShowMultipleHyperlinks,
									hyperlinks, child);
						}
					}
				}

			}
		}
	}

	private void invokeAnnotationBasedHyperlinkDetector(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks,
			List<IHyperlink> hyperlinks, Node child) {
		IAnnotationBasedHyperlinkDetector detector = NamespaceUtils
				.getAnnotationBasedHyperlinkDetector(child.getNamespaceURI());
		if (detector != null) {
			IHyperlink[] detectedHyperlinks = detector.detectHyperlinks(
					textViewer, region, canShowMultipleHyperlinks, child);
			if (detectedHyperlinks != null) {
				hyperlinks.addAll(Arrays.asList(detectedHyperlinks));
			}
		}
	}

	private void detectHyperlinks(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks, List<IHyperlink> hyperlinks,
			Node currentNode) {
		if (currentNode != null) {
			IHyperlinkDetector detector = NamespaceUtils
					.getHyperlinkDetector(currentNode.getNamespaceURI());
			if (detector != null) {
				IHyperlink[] detectedHyperlinks = detector.detectHyperlinks(
						textViewer, region, canShowMultipleHyperlinks);
				if (detectedHyperlinks != null) {
					hyperlinks.addAll(Arrays.asList(detectedHyperlinks));
				}
			}
		}
	}

}
