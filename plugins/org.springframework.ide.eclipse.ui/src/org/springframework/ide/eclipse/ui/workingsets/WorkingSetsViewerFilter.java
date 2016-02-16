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
package org.springframework.ide.eclipse.ui.workingsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WorkingSetsViewerFilter extends ViewerFilter {

	private static final String WORKING_SET_ID = SpringUIPlugin.PLUGIN_ID
			+ ".springWorkingSetPage";

	private IWorkingSet workingSet;

	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (workingSet == null) {
			return true;
		}
		if (element != null) {
			if (WORKING_SET_ID.equals(workingSet.getId())) {
				return isInWorkingSet(parentElement, element);
			}
			else {
				// special handling for Java and Resource working sets; so
				// that at least the projects are correctly filtered
				IAdaptable[] elements = getElementsFromWorkingSet();
				for (IAdaptable elem : elements) {
					if (elem instanceof IJavaProject) {
						return SpringCoreUtils.isSpringProject(((IJavaProject) elem)
								.getProject());
					}
					else if (elem instanceof IProject) {
						return SpringCoreUtils.isSpringProject(((IProject) elem));
					}
				}
			}
		}
		return true;
	}

	private boolean isInWorkingSet(Object parentElement, Object element) {
		if (workingSet == null) {
			return true;
		}
		else if (parentElement instanceof IWorkingSet
				&& element instanceof IWorkingSet) {
			return true;
		}
		for (IWorkingSetFilter filter : WorkingSetUtils.getViewerFilter()) {
			if (filter.isInWorkingSet(getElementsFromWorkingSet(parentElement),
					parentElement, element)) {
				return true;
			}
		}
		return false;
	}

	private IAdaptable[] getElementsFromWorkingSet(Object parentElement) {
		if (parentElement instanceof TreePath
				&& ((TreePath) parentElement).getFirstSegment() != null) {
			Object firstSegment = ((TreePath) parentElement).getFirstSegment();
			if (firstSegment instanceof IWorkingSet) {
				if (workingSet.isAggregateWorkingSet()) {
					IWorkingSet[] workingSets = ((AggregateWorkingSet) workingSet)
							.getComponents();
					for (IWorkingSet ws : workingSets) {
						if (ws.equals(firstSegment)) {
							return ws.getElements();
						}
					}
				}
			}
		}
		return workingSet.getElements();
	}

	private IAdaptable[] getElementsFromWorkingSet() {
		return workingSet.getElements();
	}
}
