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
package org.springframework.ide.eclipse.beans.ui.editor.outline.tx;

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
public class TxOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		if ("advice".equals(nodeName) || "annotation-driven".equals(nodeName)
				|| "attributes".equals(nodeName) || "method".equals(nodeName)
				|| "jta-transaction-manager".equals(nodeName)) {
			return TxUIImages.getImage(TxUIImages.IMG_OBJS_TX);
		}
		return null;
	}

	@Override
	public String getText(Object o) {
		Node node = (Node) o;
		String shortNodeName = node.getLocalName();

		String text = shortNodeName;
		if ("advice".equals(shortNodeName)
				|| "annotation-driven".equals(shortNodeName)) {
			String id = BeansEditorUtils.getAttribute(node, "id");
			if (StringUtils.hasText(id)) {
				text += " " + id;
			}
			if (BeansContentOutlineConfiguration.isShowAttributes()
					&& BeansEditorUtils.hasAttribute(node,
							"transaction-manager")) {
				text += " <"
						+ BeansEditorUtils.getAttribute(node,
								"transaction-manager") + ">";
			}
		}
		return text;
	}
}
