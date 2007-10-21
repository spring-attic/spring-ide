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
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;
import org.springframework.ide.eclipse.webflow.ui.properties.WebflowPropertyPage;

/**
 * Opens the project's property page for currently selected
 * {@link IWebflowConfig}.
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class OpenPropertiesAction extends AbstractNavigatorAction {

	private IProject project;

	private IModelElement modelElement;

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		super(site);
		setText("&Properties"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			IProject project = null;
			if (sElement instanceof IWebflowProject) {
				project = ((IWebflowProject) sElement).getProject();
				modelElement = null;
			}
			else if (sElement instanceof IWebflowConfig) {
				project = ((IWebflowConfig) sElement).getProject().getProject();
				modelElement = (IWebflowConfig) sElement;
			}
			else if (sElement instanceof IFile) {
				if (WebflowModelUtils.isWebflowConfig((IFile) sElement)
						&& Activator.SPRING_EXPLORER_CONTENT_PROVIDER_ID
								.equals(getActionSite().getExtensionId())) {
					project = WebflowModelUtils.getWebflowConfig(
							(IFile) sElement).getProject().getProject();
					modelElement = WebflowModelUtils
							.getWebflowConfig((IFile) sElement);
				}
			}
			if (project != null) {
				this.project = project;
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		Map<String, Object> data = new HashMap<String, Object>();
		if (modelElement != null) {
			data.put(WebflowPropertyPage.SELECTED_RESOURCE, modelElement);
		}
		SpringUIUtils.showPreferenceDialog(WebflowPropertyPage.ID, project, data);
	}
}
