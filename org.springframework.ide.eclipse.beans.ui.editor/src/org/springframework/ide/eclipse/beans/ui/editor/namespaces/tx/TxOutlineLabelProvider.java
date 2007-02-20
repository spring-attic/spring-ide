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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class TxOutlineLabelProvider extends JFaceNodeLabelProvider {

	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("advice".equals(nodeName) || "annotation-driven".equals(nodeName)) {
			return TxUIImages.getImage(TxUIImages.IMG_OBJS_TX);
		}
		return null;
	}

	public String getText(Object o) {
		Node node = (Node) o;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();

		String text = null;
		if ("advice".equals(shortNodeName) || "annotation-driven".equals(shortNodeName)) {
			text = nodeName;
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node, "transaction-manager")) {
				text += " <" + BeansEditorUtils.getAttribute(node, "transaction-manager") + ">";
			}
		}
		return text;
	}
}