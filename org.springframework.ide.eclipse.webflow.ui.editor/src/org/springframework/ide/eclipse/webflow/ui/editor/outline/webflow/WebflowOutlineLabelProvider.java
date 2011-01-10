/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class WebflowOutlineLabelProvider extends JFaceNodeLabelProvider {

	/**
	 * 
	 */
	private static final Map<String, Image> IMAGES;

	static {
		IMAGES = new HashMap<String, Image>();
		IMAGES.put("flow", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW));
		IMAGES.put("start-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_START_STATE));
		IMAGES.put("input-mapper", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_INPUT));
		IMAGES.put("mapping", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER));
		IMAGES.put("input-attribute", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_INPUT));
		IMAGES.put("attribute", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_PROPERTIES));
		//IMAGES.put("value", WebflowUIImages
		//		.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW));
		IMAGES.put("var", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_VAR));
		IMAGES.put("start-actions", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTIONS));
		IMAGES.put("action-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTION_STATE));
		IMAGES.put("action", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTION));
		IMAGES.put("bean-action", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION));
		IMAGES.put("method-arguments", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ARGUMENT));
		IMAGES.put("argument", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ARGUMENT));
		IMAGES.put("method-result", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ARGUMENT));
		IMAGES.put("evaluate-action", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION));
		IMAGES.put("evaluate-result", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTION));
		IMAGES.put("set", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION));
		IMAGES.put("transition", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_TRANSITION));
		IMAGES.put("view-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_VIEW_STATE));
		IMAGES.put("entry-actions", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTIONS));
		IMAGES.put("render-actions", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTIONS));
		IMAGES.put("exit-actions", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ACTIONS));
		IMAGES.put("decision-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_DECISION_STATE));
		IMAGES.put("if", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_IF));
		IMAGES.put("subflow-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_SUBFLOW_STATE));
		IMAGES.put("attribute-mapper", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER));
		IMAGES.put("end-state", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_END_STATE));
		IMAGES.put("global-transitions", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_GLOBAL_TRANSITION));
		IMAGES.put("output-mapper", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_OUTPUT));
		IMAGES.put("output-attribute", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_OUTPUT));
		IMAGES.put("exception-handler", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_EXCEPTION_HANDLER));
		IMAGES.put("import", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_IMPORT));
		IMAGES.put("inline-flow", WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW));
	}

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
		String nodeName = node.getLocalName();
		Image image = IMAGES.get(nodeName);
		if (image != null) {
			return image;
		}
		else {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
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
		// Node node = (Node) o;
		// String nodeName = node.getNodeName();
		// String shortNodeName = node.getLocalName();
		String text = null;

		return text;
	}
}
