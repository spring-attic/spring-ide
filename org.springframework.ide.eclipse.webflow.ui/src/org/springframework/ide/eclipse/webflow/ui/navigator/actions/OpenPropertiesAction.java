/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.properties.WebflowPropertyPage;

/**
 * Opens the project's property page for currently selected
 * {@link IWebflowConfig}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenPropertiesAction extends Action {

	private ICommonActionExtensionSite site;
	private IProject project;

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("&Properties");	// TODO externalize text
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
			SpringUIUtils.showPreferencePage(WebflowPropertyPage.ID,
					page, title);
		}
	}
}
