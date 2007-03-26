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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.properties.WebflowPropertyPage;

/**
 * Opens the project's property page for currently selected
 * {@link IWebflowConfig}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenPropertiesAction extends Action {

	private ICommonActionExtensionSite site;

	private IProject project;

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("&Properties"); // TODO externalize text
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				IWebflowConfig element = null;
				if (tElement instanceof IWebflowConfig) {
					element = (IWebflowConfig) tElement;
				}
				else if (tElement instanceof IFile) {
					if (WebflowModelUtils.isWebflowConfig((IFile) tElement)) {
						element = WebflowModelUtils
								.getWebflowConfig((IFile) tElement);
					}
				}
				if (element != null) {
					project = element.getProject().getProject();
					return true;
				}
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
