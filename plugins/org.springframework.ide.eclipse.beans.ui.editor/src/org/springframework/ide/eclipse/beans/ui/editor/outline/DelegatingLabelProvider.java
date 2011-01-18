/*******************************************************************************
 * Copyright (c) 2006, 2010 Spring IDE Developers
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
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class DelegatingLabelProvider extends LabelProvider {

	private static XMLContentOutlineConfiguration contentOutlineConfiguration = new XMLContentOutlineConfiguration();

	private ILabelProvider xmlProvider;

	private boolean isContentAssist = false;

	public DelegatingLabelProvider(ILabelProvider xmlProvider) {
		this.xmlProvider = xmlProvider;
	}

	public DelegatingLabelProvider() {
		this.isContentAssist = true;
		this.xmlProvider = contentOutlineConfiguration.getLabelProvider(null);
	}

	@Override
	public Image getImage(Object object) {
		if (!BeansEditorUtils.isSpringStyleOutline() && !isContentAssist) {
			return xmlProvider.getImage(object);
		}

		Node node = (Node) object;
		String namespace = node.getNamespaceURI();

		ILabelProvider[] labelProviders = NamespaceUtils.getLabelProvider(namespace);
		for (ILabelProvider labelProvider : labelProviders) {
			Image image = labelProvider.getImage(object);
			if (image != null) {
				return image;
			}
		}

		INamespaceDefinition namespaceDefinition = BeansCorePlugin.getNamespaceDefinitionResolver(null)
				.resolveNamespaceDefinition(namespace);
		if (namespaceDefinition != null) {
			return org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils.getImage(namespaceDefinition);
		}

		return xmlProvider.getImage(object);
	}

	@Override
	public String getText(Object object) {
		if (!BeansEditorUtils.isSpringStyleOutline() && !isContentAssist) {
			return xmlProvider.getText(object);
		}

		Node node = (Node) object;
		String namespace = node.getNamespaceURI();

		ILabelProvider[] labelProviders = NamespaceUtils.getLabelProvider(namespace);
		for (ILabelProvider labelProvider : labelProviders) {
			String text = labelProvider.getText(object);
			if (text != null && !"".equals(text.trim())) {
				return text;
			}
		}
		return xmlProvider.getText(object);
	}
}
