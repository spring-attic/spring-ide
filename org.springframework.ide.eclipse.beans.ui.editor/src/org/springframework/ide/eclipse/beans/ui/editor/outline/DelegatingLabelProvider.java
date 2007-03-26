/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class DelegatingLabelProvider extends LabelProvider {

	private static ILabelProvider xmlProvider;

	public DelegatingLabelProvider(ILabelProvider xmlProvider) {
		DelegatingLabelProvider.xmlProvider = xmlProvider;
	}

	public DelegatingLabelProvider() {
	}

	@Override
	public Image getImage(Object object) {
		if (!BeansEditorUtils.isSpringStyleOutline()) {
			return xmlProvider.getImage(object);
		}

		Node node = (Node) object;
		String namespace = node.getNamespaceURI();

		ILabelProvider labelProvider = NamespaceUtils
				.getLabelProvider(namespace);
		if (labelProvider != null) {
			Image image = labelProvider.getImage(object);
			if (image != null) {
				return image;
			}
		}
		return xmlProvider.getImage(object);
	}

	@Override
	public String getText(Object object) {
		if (!BeansEditorUtils.isSpringStyleOutline()) {
			return xmlProvider.getText(object);
		}

		Node node = (Node) object;
		String namespace = node.getNamespaceURI();

		ILabelProvider labelProvider = NamespaceUtils
				.getLabelProvider(namespace);
		if (labelProvider != null) {
			String text = labelProvider.getText(object);
			if (text != null && !"".equals(text.trim())) {
				return text;
			}
		}
		return xmlProvider.getText(object);
	}
}
