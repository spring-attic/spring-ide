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
package org.springframework.ide.eclipse.webflow.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorLabelProvider;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * {@link ICommonLabelProvider} implementation for {@link IWebflowConfig}
 * elements.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowNavigatorLabelProvider extends BeansNavigatorLabelProvider {

	public String getDescription(Object element) {
		if (element instanceof IWebflowConfig) {
			IFile file = ((IWebflowConfig) element).getResource();
			return file.getName()
					+ " - "
					+ file.getProjectRelativePath().removeLastSegments(1)
							.toString();
		} else {
			return null;
		}
	}

	public Image getBaseImage(Object element) {
		if (element instanceof IWebflowConfig) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		return super.getBaseImage(element);
	}

	public String getBaseText(Object element) {
		if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getName() != null
					&& WebflowNavigatorContentProvider
							.BEANS_EXPLORER_CONTENT_PROVIDER_ID
									.equals(getProviderID())
					|| WebflowNavigatorContentProvider
							.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
									.equals(getProviderID())) {
				return config.getName();
			} else {

				return config.getResource().getProjectRelativePath().toString();
			}
		}
		return super.getBaseText(element);
	}
}
