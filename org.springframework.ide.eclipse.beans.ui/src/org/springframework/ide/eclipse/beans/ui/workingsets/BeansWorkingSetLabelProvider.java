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
package org.springframework.ide.eclipse.beans.ui.workingsets;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorLabelProvider;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.ui.workingsets.IElementSpecificLabelProvider;


/**
 * Specialized {@link ILabelProvider} that is only resonsible for providing
 * images and texts for elements of the {@link IBeansModelElement}.
 * @author Christian Dupuis
 * @since 2.0
 * @see BeansNavigatorLabelProvider
 */
public class BeansWorkingSetLabelProvider implements
		IElementSpecificLabelProvider {

	private BeansNavigatorLabelProvider labelProvider = new BeansNavigatorLabelProvider();

	public boolean supportsElement(Object object) {
		return (object instanceof IBeansModelElement || (object instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) object)) || (object instanceof ZipEntryStorage));
	}

	public String getText(Object object) {
		if (object instanceof IResource) {
			return ((IResource) object).getProjectRelativePath().toString();
		}
		return labelProvider.getText(object);
	}

	public Image getImage(Object element) {
		return labelProvider.getImage(element);
	}

	public void addListener(ILabelProviderListener listener) {
		labelProvider.addListener(listener);
	}

	public void dispose() {
		labelProvider.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		return labelProvider.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		labelProvider.removeListener(listener);
	}
}
