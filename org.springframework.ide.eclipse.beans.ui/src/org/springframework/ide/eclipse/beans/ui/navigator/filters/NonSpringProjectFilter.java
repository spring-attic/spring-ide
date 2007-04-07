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
package org.springframework.ide.eclipse.beans.ui.navigator.filters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Filters non-Spring projects.
 * 
 * @author Torsten Juergeleit
 */
public class NonSpringProjectFilter extends ViewerFilter {

	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IAdaptable) {
			IProject project = SpringCoreUtils.getAdapter(element,
					IProject.class);
			if (project != null) {
				return SpringCoreUtils.isSpringProject(project);
			}
		}
		return true;
	}
}
