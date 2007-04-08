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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
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

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		super(site);
		setText("&Properties"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			IWebflowConfig element = null;
			if (sElement instanceof IWebflowConfig) {
				element = (IWebflowConfig) sElement;
			}
			else if (sElement instanceof IFile) {
				if (WebflowModelUtils.isWebflowConfig((IFile) sElement)) {
					element = WebflowModelUtils
							.getWebflowConfig((IFile) sElement);
				}
			}
			if (element != null) {
				project = element.getProject().getProject();
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		showProjectPropertyPage(project);
	}

	private static void showProjectPropertyPage(IProject project) {
		if (project != null) {
			String title = "";
			IPreferencePage page = new WebflowPropertyPage(project);
			SpringUIUtils.showPreferencePage(WebflowPropertyPage.ID, page,
					title);
		}
	}
}
