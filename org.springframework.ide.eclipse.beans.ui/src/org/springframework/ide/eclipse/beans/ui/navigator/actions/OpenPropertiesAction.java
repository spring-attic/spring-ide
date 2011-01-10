/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.properties.ProjectPropertyPage;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * Opens the project's property page for currently selected
 * {@link IModelElement}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class OpenPropertiesAction extends AbstractNavigatorAction {

	private IProject project;
	private int block = 0;
	private IModelElement modelElement;

	public OpenPropertiesAction(ICommonActionExtensionSite site) {
		super(site);
		setText("&Properties");	// TODO externalize text
    }

	@Override
	public boolean isEnabled(IStructuredSelection selection) {
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				IModelElement element = null;
				if (tElement instanceof IModelElement) {
					element = (IModelElement) tElement;
				}
				else if (tElement instanceof IFile) {
					if (BeansUIPlugin.SPRING_EXPLORER_CONTENT_PROVIDER_ID
							.equals(getActionSite().getExtensionId())) {
						element = BeansCorePlugin.getModel().getConfig(
								(IFile) tElement);
					}
				} else if (tElement instanceof ZipEntryStorage) {
					element = BeansModelUtils
							.getConfig((ZipEntryStorage) tElement);
				}
				if (element != null) {
					if (element instanceof IResourceModelElement) {
						project = ((IResourceModelElement) element).getElementResource().getProject();
					}
					else {
						project = BeansModelUtils.getProject(element).getProject();
					}
					block = getProjectPropertyPageBlock(
							tSelection.getPaths()[0]);
					modelElement = element;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ProjectPropertyPage.BLOCK_ID, block);
		data.put(ProjectPropertyPage.SELECTED_RESOURCE, modelElement);
		BeansUIUtils.showProjectPropertyPage(project, data);
	}

	/**
	 * Returns 1 if given path contains a segment of type
	 * {@link IBeansConfigSet} else 0.
	 */
	private int getProjectPropertyPageBlock(TreePath path) {
		for (int i = path.getSegmentCount() - 1; i > 0; i--) {
			Object segment = path.getSegment(i);
			if (segment instanceof IBeansConfigSet) {
				return 1;
			}
		}
		return 0;
	}
}
