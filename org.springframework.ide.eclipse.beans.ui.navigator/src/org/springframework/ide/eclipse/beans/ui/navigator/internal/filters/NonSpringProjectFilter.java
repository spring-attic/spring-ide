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

package org.springframework.ide.eclipse.beans.ui.navigator.internal.filters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Filters non-Spring projects.
 * @author Torsten Juergeleit
 */
public class NonSpringProjectFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) element)
					.getAdapter(IProject.class);
			if (project != null) {
				return SpringCoreUtils.isSpringProject(project);
			}
		}
		return true;
	}
}
