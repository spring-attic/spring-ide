/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.outline.util;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class UtilOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();

		if ("properties".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_PROPERTIES);
		}
		else if ("property-path".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_PROPERTIES);
		}
		else if ("constant".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_CONSTANT);
		}
		else if ("set".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_SET);
		}
		else if ("list".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_LIST);
		}
		else if ("map".equals(nodeName)) {
			return UtilUIImages.getImage(UtilUIImages.IMG_OBJS_MAP);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		NamedNodeMap attrs = node.getAttributes();
		Node attr;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();
		String text = shortNodeName;
		
		if ("properties".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("location");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		else if ("property-path".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("path");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		else if ("constant".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("static-field");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		else if ("set".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("set-class");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		else if ("list".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("list-class");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		else if ("map".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("map-class");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}
		return text;
	}
}
