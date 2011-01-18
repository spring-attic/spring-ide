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
package org.springframework.ide.eclipse.beans.ui.editor.outline.jms;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.w3c.dom.Node;

/**
 * Label provider for the jms namespace
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class JmsOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("listener-container".equals(nodeName) || "listener".equals(nodeName)
				|| "jca-listener-container".equals(nodeName)) {
			return JmsUIImages.getImage(JmsUIImages.IMG_OBJS_JMS);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		return node.getLocalName();
	}
}
