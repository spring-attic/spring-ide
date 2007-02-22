/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class DelegatingLabelProvider extends JFaceNodeLabelProvider {

	private static ILabelProvider xmlProvider;

	public DelegatingLabelProvider(ILabelProvider xmlProvider) {
		DelegatingLabelProvider.xmlProvider = xmlProvider;
	}

	public DelegatingLabelProvider() {
	}

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