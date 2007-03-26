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
package org.springframework.ide.eclipse.beans.ui.navigator.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansExplorer;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Opens the project's property page for currently selected
 * {@link IModelElement}.
 * 
 * @author Torsten Juergeleit
 */
public class OpenPropertiesAction extends Action {

	private ICommonActionExtensionSite site;
	private IProject project;
	private int block = 0;

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
				IModelElement element = null;
				if (tElement instanceof IModelElement) {
					element = (IModelElement) tElement;
				} else if (tElement instanceof IFile) {
					if (site.getViewSite().getId().equals(
							BeansExplorer.BEANS_EXPLORER_ID)) {
						element = BeansCorePlugin.getModel().getConfig(
								(IFile) tElement);
					}
				} else if (tElement instanceof ZipEntryStorage) {
					element = BeansModelUtils
							.getConfig((ZipEntryStorage) tElement);
				}
				if (element != null) {
					project = BeansModelUtils.getProject(element).getProject();
					block = getProjectPropertyPageBlock(
							tSelection.getPaths()[0]);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		BeansUIUtils.showProjectPropertyPage(project, block);
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
