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
 * 
 */
public class WebflowModelLabelDecorator implements ILabelDecorator {

	/**
	 * 
	 */
	public WebflowModelLabelDecorator() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {

		if (element instanceof IWebflowModelElement && image != null) {
			ImageDescriptor descriptor = new WebflowModelImageDescriptor(
					new ImageImageDescriptor(image),
					(IWebflowModelElement) element);
			image = BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return image;
	}

	/**
	 * 
	 * 
	 * @param element 
	 * @param image 
	 * 
	 * @return 
	 */
	public ImageDescriptor getDecoratedImageDescriptor(Image image,
			Object element) {
		if (element instanceof IWebflowModelElement && image != null) {
			ImageDescriptor descriptor = new WebflowModelImageDescriptor(
					new ImageImageDescriptor(image),
					(IWebflowModelElement) element);
			return descriptor;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}
}
