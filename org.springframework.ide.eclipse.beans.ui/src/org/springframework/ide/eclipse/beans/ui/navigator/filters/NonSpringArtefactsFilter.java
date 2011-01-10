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
package org.springframework.ide.eclipse.beans.ui.navigator.filters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Filters non-Spring artefacts.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class NonSpringArtefactsFilter extends ViewerFilter {

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					folder.getProject());
			if (project != null) {
				String path = folder.getProjectRelativePath().toString() + '/';
				for (IBeansConfig config : project.getConfigs()) {
					if (config.getElementName().startsWith(path)) {
						return true;
					}
				}
			}
			return false;
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					file.getProject());
			if (project != null) {
				for (IBeansConfig config : project.getConfigs()) {

					// The following comparison works for archived config files
					// too
					if (config.getElementResource().equals(file)) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}
}
