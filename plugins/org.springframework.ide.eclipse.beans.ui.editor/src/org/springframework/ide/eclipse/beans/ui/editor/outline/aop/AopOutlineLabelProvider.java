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
package org.springframework.ide.eclipse.beans.ui.editor.outline.aop;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class AopOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();

		if ("config".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
		}
		if ("aspect".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ASPECT);
		}
		if ("before".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEFORE_ADVICE);
		}
		if ("after".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("around".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AROUND_ADVICE);
		}
		if ("after-returning".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("after-throwing".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		if ("pointcut".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
		}
		if ("advisor".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ADVICE);
		}
		if ("aspectj-autoproxy".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
		}
		if ("include".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
		}
		if ("declare-parents".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
		}
		if ("scoped-proxy".equals(nodeName)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
		}

		return null;
	}

	@Override
	public String getText(Object o) {
		// Create Spring beans label text
		Node node = (Node) o;
		NamedNodeMap attrs = node.getAttributes();
		Node attr;
		String shortNodeName = node.getLocalName();
		String text = shortNodeName;

		if ("config".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("proxy-target-class");
				if (attr != null) {
					text += attr.getNodeValue();
				}
			}
		}
		else if ("aspect".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("ref");
				if (attr != null) {
					text += "<" + attr.getNodeValue() + "> ";
				}
			}
		}
		else if ("advisor".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("advice-ref");
				if (attr != null) {
					text += "<" + attr.getNodeValue() + "> ";
				}
				attr = attrs.getNamedItem("pointcut-ref");
				if (attr != null) {
					text += "<" + attr.getNodeValue() + "> ";
				}
			}
		}
		else if ("before".equals(shortNodeName)
				|| "after".equals(shortNodeName)
				|| "after-returning".equals(shortNodeName)
				|| "after-throwing".equals(shortNodeName)
				|| "around".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("method");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
				attr = attrs.getNamedItem("pointcut");
				if (attr != null) {
					text += "{" + attr.getNodeValue() + "} ";
				}
			}
		}
		else if ("pointcut".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("id");
				if (attr != null) {
					text += attr.getNodeValue() + " ";
				}
			}
		}
		else if ("include".equals(shortNodeName)) {
			text += " ";
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				attr = attrs.getNamedItem("name");
				if (attr != null) {
					text += "[" + attr.getNodeValue() + "]";
				}
			}
		}

		return text;
	}
}
