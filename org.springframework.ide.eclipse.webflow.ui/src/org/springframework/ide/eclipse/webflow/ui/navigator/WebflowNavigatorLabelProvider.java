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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * {@link ICommonLabelProvider} which knows about the beans core model's
 * {@link IModelElement elements} and the {@link IWebflowConfig}
 * elements.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowNavigatorLabelProvider extends BeansNavigatorLabelProvider {

	@Override
	public String getDescription(Object element) {
		if (element instanceof IWebflowProject) {
			return "Web Flow" // TODO Externalize text
					+ " - "
					+ ((IWebflowProject) element).getProject().getName();
		}
		else if (element instanceof IWebflowConfig) {
			return getFileDescription(((IWebflowConfig) element).getResource());
		}
		else if (element instanceof IFile) {
			return getFileDescription((IFile) element);
		}
		return super.getDescription(element);
	}

	protected String getFileDescription(IFile file) {
		return file.getName() + " - " + file.getFullPath().makeRelative()
				.removeLastSegments(1).toString();
	}

	@Override
	protected Image getImage(Object element, Object parentElement,
			int severity) {
		if (element instanceof IWebflowConfig
				|| element instanceof IWebflowProject) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		return super.getImage(element, parentElement, severity);
	}

	@Override
	protected String getText(Object element, Object parentElement,
			int severity) {
		if (element instanceof IWebflowProject) {
			return "Web Flow"; // TODO Externalize text
		}
		else if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getName() != null) {
				return config.getName();
			}
			else {
				return config.getResource().getFullPath().makeRelative()
						.toString();
			}
		}
		else if (element instanceof IFile
				&& parentElement instanceof IWebflowProject) {
			return ((IFile) element).getProjectRelativePath().toString();
		}
		return super.getText(element, parentElement, severity);
	}
}
