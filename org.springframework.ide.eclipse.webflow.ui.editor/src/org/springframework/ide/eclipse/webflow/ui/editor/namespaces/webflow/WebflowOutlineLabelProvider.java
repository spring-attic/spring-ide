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

package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class WebflowOutlineLabelProvider extends JFaceNodeLabelProvider {

	public Image getImage(Object object) {

		Node node = (Node) object;
		String prefix = node.getPrefix();
		String nodeName = node.getNodeName();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
	}

	public String getText(Object o) {

		// Create Spring beans label text
		Node node = (Node) o;
		String nodeName = node.getNodeName();
		String shortNodeName = node.getLocalName();

		String text = null;
		return text;
	}
}