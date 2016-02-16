/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.outline.context;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Label provider for the context namespace
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class ContextOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("component-scan".equals(nodeName)
				|| "load-time-weaver".equals(nodeName)
				|| "annotation-config".equals(nodeName)
				|| "spring-configured".equals(nodeName)
				|| "include-filter".equals(nodeName)
				|| "exclude-filter".equals(nodeName)
				|| "mbean-export".equals(nodeName)
				|| "mbean-server".equals(nodeName)
				|| "property-placeholder".equals(nodeName)
				|| "property-override".equals(nodeName)) {
			return ContextUIImages.getImage(ContextUIImages.IMG_OBJS_CONTEXT);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		NamedNodeMap attrs = node.getAttributes();
		Node attr;
		String shortNodeName = node.getLocalName();
		String text = shortNodeName;

		if ("load-time-weaver".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("weaver-class");
				if (attr != null) {
					text += '[' + attr.getNodeValue() + ']';
				}
			}
		}
		else if ("component-scan".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("base-package");
				if (attr != null) {
					text += '[' + attr.getNodeValue() + ']';
				}
			}
		}
		
		return text;
	}
}
