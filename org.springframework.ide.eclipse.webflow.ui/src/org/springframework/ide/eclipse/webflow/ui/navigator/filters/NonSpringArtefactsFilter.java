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
package org.springframework.ide.eclipse.webflow.ui.navigator.filters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * Filters non-Spring artefacts.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class NonSpringArtefactsFilter extends ViewerFilter {

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			IWebflowModel model = Activator.getModel();
			IWebflowProject project = model.getProject(folder.getProject());
			if (project != null) {
				String path = folder.getProjectRelativePath().toString() + '/';
				for (IWebflowConfig config : project.getConfigs()) {
					if (config.getResource().getProjectRelativePath()
							.toString().startsWith(path)) {
						return true;
					}
				}
			}
			return false;
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			IWebflowModel model = Activator.getModel();
			IWebflowProject project = model.getProject(file.getProject());
			if (project != null) {
				for (IWebflowConfig config : project.getConfigs()) {
					// The following comparison works for archived config files
					// too
					if (config.getResource().equals(file)) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}
}
