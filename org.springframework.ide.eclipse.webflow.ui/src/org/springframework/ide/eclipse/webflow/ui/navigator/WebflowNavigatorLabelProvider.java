/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * {@link LabelProvider} implementation for {@link IWebflowConfig} elements.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowNavigatorLabelProvider extends BeansModelLabelProvider
		implements ICommonLabelProvider {

	private String providerID;

	public WebflowNavigatorLabelProvider() {
		super(true);
	}

	public String getDescription(Object element) {
		if (element instanceof IWebflowConfig) {
			IFile file = ((IWebflowConfig) element).getResource();
			return file.getName()
					+ " - "
					+ file.getProjectRelativePath().removeLastSegments(1)
							.toString();

		}
		else {
			return null;
		}
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}

	public Image getImage(Object element) {
		if (element instanceof IWebflowConfig) {
			return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
		}
		else {
			return super.getImage(element);
		}
	}

	public String getText(Object element) {
		if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getName() != null
					&& WebflowNavigatorContentProvider.BEANS_EXPLORER_CONTENT_PROVIDER_ID
							.equals(providerID)
					|| WebflowNavigatorContentProvider.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
							.equals(providerID)) {
				return config.getName();
			}
			else {

				return config.getResource().getProjectRelativePath().toString();
			}
		}
		return super.getText(element);
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}
}
