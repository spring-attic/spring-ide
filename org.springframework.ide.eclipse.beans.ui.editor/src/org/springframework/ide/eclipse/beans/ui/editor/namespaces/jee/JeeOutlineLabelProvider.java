/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.jee;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class JeeOutlineLabelProvider extends JFaceNodeLabelProvider {

	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("jndi-lookup".equals(nodeName) || "local-slsb".equals(nodeName)
				|| "remote-slsb".equals(nodeName)
				|| "entity-manager-factory".equals(nodeName)) {
			return JeeUIImages.getImage(JeeUIImages.IMG_OBJS_JEE);
		}
		return null;
	}

	public String getText(Object o) {
		Node node = (Node) o;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();

		String text = null;
		if ("jndi-lookup".equals(shortNodeName)
				|| "local-slsb".equals(shortNodeName)
				|| "remote-slsb".equals(shortNodeName)) {
			text = nodeName;
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
			text = nodeName;
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