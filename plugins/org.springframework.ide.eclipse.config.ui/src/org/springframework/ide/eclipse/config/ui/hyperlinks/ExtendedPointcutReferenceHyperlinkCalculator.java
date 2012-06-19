/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.hyperlinks;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.HyperlinkUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.aop.PointcutReferenceHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.AopSchemaConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @deprecated Use {@link PointcutReferenceHyperlinkCalculator} instead.
 */
@Deprecated
public class ExtendedPointcutReferenceHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink hyperlink = searchPointcutElements(target, parentNode, textViewer, hyperlinkRegion);
		if (hyperlink == null && parentNode.getParentNode() != null) {
			hyperlink = searchPointcutElements(target, parentNode.getParentNode(), textViewer, hyperlinkRegion);
		}
		return hyperlink;
	}

	private IHyperlink searchPointcutElements(String name, Node node, ITextViewer textViewer, IRegion hyperlinkRegion) {
		NodeList beanNodes = node.getChildNodes();
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			if (AopSchemaConstants.ELEM_POINTCUT.equals(beanNode.getLocalName())) {
				if (name.equals(BeansEditorUtils.getAttribute(beanNode, AopSchemaConstants.ATTR_ID))) {
					IRegion region = HyperlinkUtils.getHyperlinkRegion(beanNode);
					return new ExtendedNodeElementHyperlink(beanNode, hyperlinkRegion, region, textViewer);
				}
			}
		}
		return null;
	}

}
