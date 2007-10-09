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
package org.springframework.ide.eclipse.webflow.ui.graph.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelLabelDecorator implements ILabelDecorator {

	public Image decorateImage(Image image, Object element) {

		if (element instanceof IWebflowModelElement && image != null) {
			ImageDescriptor descriptor = new WebflowModelImageDescriptor(image,
					(IWebflowModelElement) element);
			image = BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return image;
	}

	public ImageDescriptor getDecoratedImageDescriptor(Image image,
			Object element) {
		if (element instanceof IWebflowModelElement && image != null) {
			ImageDescriptor descriptor = new WebflowModelImageDescriptor(image,
					(IWebflowModelElement) element);
			return descriptor;
		}
		return null;
	}

	public String decorateText(String text, Object element) {
		return text;
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}
}
	