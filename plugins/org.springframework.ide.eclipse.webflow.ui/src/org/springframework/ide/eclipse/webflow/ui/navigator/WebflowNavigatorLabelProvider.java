/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * {@link ICommonLabelProvider} which knows about the beans core model's
 * {@link IModelElement elements} and the {@link IWebflowConfig} elements.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
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
		return file.getName()
				+ " - "
				+ file.getFullPath().makeRelative().removeLastSegments(1)
						.toString();
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof IWebflowConfig) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		else if (element instanceof IWebflowProject) {
			return WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_VIRTUAL_FOLDER);
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IWebflowProject) {
			return "Web Flow"; // TODO Externalize text
		}
		else if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getName() != null) {
				return config.getName();
			}
			else {
				return getFileLabel(config.getResource());
			}
		}
		else if (element instanceof IFile
				&& parentElement != null) {
			return getFileLabel((IFile) element);
		}
		return super.getText(element, parentElement);
	}
	
	private String getFileLabel(IFile file) {
		return file.getName()
				+ " - "
				+ file.getProjectRelativePath().removeLastSegments(1)
						.toString();
	}
}
