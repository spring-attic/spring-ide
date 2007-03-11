/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.webflow;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * 
 */
@SuppressWarnings("restriction")
public class WebflowConfigOutlineLabelProvider extends JFaceNodeLabelProvider {

	/**
	 * 
	 * 
	 * @param object 
	 * 
	 * @return 
	 */
	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String ns = node.getNamespaceURI();
		if ("http://www.springframework.org/schema/webflow-config".equals(ns)) {
			return WebflowConfigUIImages.getImage(WebflowConfigUIImages.IMG_OBJS_WEBFLOW);
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param o 
	 * 
	 * @return 
	 */
	@Override
	public String getText(Object o) {

		// Create Spring beans label text
		Node node = (Node) o;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();

		String text = null;
		if ("executor".equals(shortNodeName)) {
			text = nodeName;
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node, "registry-ref")) {
				text += " <" + BeansEditorUtils.getAttribute(node, "registry-ref") + ">";
			}
		}
		else if ("registry".equals(shortNodeName)) {
			text = nodeName;
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
		}
		else if ("location".equals(shortNodeName)) {
			text = nodeName;
			if (BeansContentOutlineConfiguration.isShowAttributes() && BeansEditorUtils.hasAttribute(node, "path")) {
				text += " [" + BeansEditorUtils.getAttribute(node, "path") + "]";
			}
		}
		else if ("listener".equals(shortNodeName)) {
			text = nodeName;
			if (BeansContentOutlineConfiguration.isShowAttributes() && BeansEditorUtils.hasAttribute(node, "ref")) {
				text += " <" + BeansEditorUtils.getAttribute(node, "ref") + ">";
			}
		}
		else if ("attribute".equals(shortNodeName)) {
			text = nodeName;
			if (BeansContentOutlineConfiguration.isShowAttributes() && BeansEditorUtils.hasAttribute(node, "name")) {
				text += " " + BeansEditorUtils.getAttribute(node, "name") + "="
						+ BeansEditorUtils.getAttribute(node, "value");
			}
		}
		else if ("repository".equals(shortNodeName)) {
			text = nodeName;
			if (BeansContentOutlineConfiguration.isShowAttributes() && BeansEditorUtils.hasAttribute(node, "type")) {
				text += " " + BeansEditorUtils.getAttribute(node, "type");
			}
		}
		else if ("alwaysRedirectOnPause".equals(shortNodeName)) {
			text = nodeName;
			if (BeansContentOutlineConfiguration.isShowAttributes() && BeansEditorUtils.hasAttribute(node, "value")) {
				text += " " + BeansEditorUtils.getAttribute(node, "value");
			}
		}
		return text;
	}
}