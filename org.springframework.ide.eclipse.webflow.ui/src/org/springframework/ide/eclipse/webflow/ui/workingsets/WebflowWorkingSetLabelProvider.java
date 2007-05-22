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
package org.springframework.ide.eclipse.webflow.ui.workingsets;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.ui.workingsets.IElementSpecificLabelProvider;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IPersistableWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.navigator.WebflowNavigatorLabelProvider;

/**
 * Specialized {@link ILabelProvider} that is only resonsible for providing
 * images and texts for elements of the {@link IWebflowModel}.
 * @author Christian Dupuis
 * @since 2.0
 * @see WebflowNavigatorLabelProvider
 */
public class WebflowWorkingSetLabelProvider implements
		IElementSpecificLabelProvider {

	private WebflowNavigatorLabelProvider labelProvider = new WebflowNavigatorLabelProvider();

	public boolean supportsElement(Object object) {
		return object instanceof IWebflowModelElement
				|| object instanceof IPersistableWebflowModelElement
				|| (object instanceof IResource && WebflowModelUtils
						.isWebflowConfig((IResource) object));
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
