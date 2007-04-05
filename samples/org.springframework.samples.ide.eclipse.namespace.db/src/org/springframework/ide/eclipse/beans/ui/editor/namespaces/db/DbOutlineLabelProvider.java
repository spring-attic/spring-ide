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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.db;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class DbOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String ns = node.getNamespaceURI();
		if ("http://www.springframework.org/schema/db".equals(ns)) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_TX);
		}
		return null;
	}

	@Override
	public String getText(Object o) {

		// Create Spring beans label text
		Node node = (Node) o;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();

		String text = null;
		if ("driver-manager-data-source".equals(shortNodeName)) {
			text = nodeName;
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node, "driver-class-name")) {
				text += " [" + BeansEditorUtils.getAttribute(node, "driver-class-name") + "]";
			}
		}
		return text;
	}
}
