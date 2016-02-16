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
package org.springframework.ide.eclipse.beans.ui.editor.outline.jee;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class JeeOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("jndi-lookup".equals(nodeName) || "local-slsb".equals(nodeName)
				|| "remote-slsb".equals(nodeName)
				|| "entity-manager-factory".equals(nodeName)
				|| "environment".equals(nodeName)) {
			return JeeUIImages.getImage(JeeUIImages.IMG_OBJS_JEE);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		String shortNodeName = node.getLocalName();
		
		String text = shortNodeName;
		if ("jndi-lookup".equals(shortNodeName)
				|| "local-slsb".equals(shortNodeName)
				|| "remote-slsb".equals(shortNodeName)) {
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node, "jndi-name")) {
				text += " [" + BeansEditorUtils.getAttribute(node, "jndi-name")
						+ "]";
			}
		}
		else if ("entity-manager-factory".equals(shortNodeName)) {
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node,
							"persistence-unit-name")) {
				text += " ["
						+ BeansEditorUtils.getAttribute(node,
								"persistence-unit-name") + "]";
			}
		}
		return text;
	}
}
