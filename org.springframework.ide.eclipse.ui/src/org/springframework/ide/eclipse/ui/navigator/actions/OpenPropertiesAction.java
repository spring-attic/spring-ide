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
package org.springframework.ide.eclipse.ui.navigator.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.dialogs.SpringPreferencePage;

/**
 * Opens the project's property page for currently selected {@link IProject}.
 * 
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OpenPropertiesAction extends AbstractNavigatorAction {

	private IProject project;

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		super(site);
		setText("&Properties"); // TODO externalize text
	}

	@Override
	public boolean isEnabled(IStructuredSelection selection) {
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				if (tElement instanceof ISpringProject) {
					project = ((ISpringProject) tElement).getProject();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		SpringUIUtils.showPreferenceDialog(SpringPreferencePage.ID, project,
				null);
	}
}
