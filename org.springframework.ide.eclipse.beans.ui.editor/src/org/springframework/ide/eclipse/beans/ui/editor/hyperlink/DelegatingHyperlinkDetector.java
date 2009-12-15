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
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkDetector} implementation that delegates to {@link IHyperlinkDetector}s that are contributed over the
 * namespace extension point.
 * @author Christian Dupuis
 */
public class DelegatingHyperlinkDetector implements IHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();

		IDocument document = textViewer.getDocument();
		Node currentNode = BeansEditorUtils.getNodeByOffset(document, region.getOffset());

		detectHyperlinks(textViewer, region, canShowMultipleHyperlinks, hyperlinks, currentNode);

		if (hyperlinks.size() == 0 || canShowMultipleHyperlinks) {
			detectAnnotationBasedHyperlinks(textViewer, region, canShowMultipleHyperlinks, hyperlinks, currentNode);
		}
		// Check hyperlinks to make sure that no nulls are in there
		List<IHyperlink> safeHylerlinks = new ArrayList<IHyperlink>();
		for (IHyperlink hyperlink : hyperlinks) {
			if (hyperlink != null) {
				safeHylerlinks.add(hyperlink);
			}
		}

		hyperlinks = safeHylerlinks;

		if (hyperlinks.size() > 0) {
			return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
		}
		else {
			return null;
		}
	}

	private void detectAnnotationBasedHyperlinks(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks, List<IHyperlink> hyperlinks, Node currentNode) {
		ToolAnnotationBasedHyperlinkDetector detector = new ToolAnnotationBasedHyperlinkDetector();
		if (currentNode != null) {
			IHyperlink[] detectedHyperlinks = detector.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
			if (detectedHyperlinks != null) {
				hyperlinks.addAll(Arrays.asList(detectedHyperlinks));
			}
		}
	}

	private void detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks,
			List<IHyperlink> hyperlinks, Node currentNode) {
		if (currentNode != null) {
			IHyperlinkDetector[] detectors = NamespaceUtils.getHyperlinkDetector(currentNode.getNamespaceURI());
			for (IHyperlinkDetector detector : detectors) {
				IHyperlink[] detectedHyperlinks = detector.detectHyperlinks(textViewer, region,
						canShowMultipleHyperlinks);
				if (detectedHyperlinks != null) {
					hyperlinks.addAll(Arrays.asList(detectedHyperlinks));
				}
			}
		}
	}

	private class ToolAnnotationBasedHyperlinkDetector extends AbstractHyperlinkDetector implements IHyperlinkDetector {

		private final ToolAnnotationBasedHyperlinkCalculator calculator = new ToolAnnotationBasedHyperlinkCalculator();

		public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
				ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
			return calculator.createHyperlink(name, target, node, parentNode, document, textViewer, hyperlinkRegion,
					cursor);
		}

		@Override
		public IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode,
				IDocument document, ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
			return calculator.createHyperlinks(name, target, node, parentNode, document, textViewer, hyperlinkRegion,
					cursor);
		}

		public boolean isLinkableAttr(Attr attr) {
			// The calculator will know if the attribute is linkable or not
			return true;
		}
	}

}
