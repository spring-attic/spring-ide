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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class WebflowOutlineLabelProvider extends JFaceNodeLabelProvider {

	private static final Map<String, Image> IMAGES;

	static {
		IMAGES = new HashMap<String, Image>();
		IMAGES.put("action-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTION_STATE));
		IMAGES.put("action", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTION));
		IMAGES.put("view-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_VIEW_STATE));
		IMAGES.put("end-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_END_STATE));
		IMAGES.put("decision-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_DECISION_STATE));
		IMAGES.put("if", WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_IF));
		IMAGES.put("transition", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_TRANSITION));
		IMAGES.put("attribute-mapper", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER));
	}

	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		Image image = IMAGES.get(nodeName);
		if (image != null) {
			return image;
		}
		else {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
	}

	public String getText(Object o) {
		// Node node = (Node) o;
		// String nodeName = node.getNodeName();
		// String shortNodeName = node.getLocalName();
		String text = null;

		return text;
	}
}