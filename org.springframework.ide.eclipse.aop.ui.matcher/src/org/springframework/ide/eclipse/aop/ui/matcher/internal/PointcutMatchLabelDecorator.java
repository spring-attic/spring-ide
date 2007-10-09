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
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * {@link ILabelDecorator} that delegates decoration to
 * {@link PointcutMatchImageDescriptor}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatchLabelDecorator implements ILabelDecorator {

	public Image decorateImage(Image image, Object element) {
		ImageDescriptor descriptor = new PointcutMatchImageDescriptor(image,
				element);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
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
