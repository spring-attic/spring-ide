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
package org.springframework.ide.eclipse.beans.ui.editor.outline.lang;

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
public class LangOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("groovy".equals(nodeName) || "bsh".equals(nodeName)
				|| "jruby".equals(nodeName) || "inline-script".equals(nodeName)
				|| "defaults".equals(nodeName) || "property".equals(nodeName)) {
			return LangUIImages.getImage(LangUIImages.IMG_OBJS_LANG);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		String shortNodeName = node.getLocalName();

		String text = shortNodeName;
		if ("groovy".equals(shortNodeName) || "bsh".equals(shortNodeName)
				|| "jruby".equals(shortNodeName)) {
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()) {
				String ss = BeansEditorUtils
						.getAttribute(node, "script-source");
				if (StringUtils.hasText(ss)) {
					text += " [" + ss + "]";
				}
			}
		}
		return text;
	}
}
