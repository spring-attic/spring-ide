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
package org.springframework.ide.eclipse.ui.workingsets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.AggregateWorkingSet;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WorkingSetViewerFilter extends ViewerFilter {

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
			return isInWorkingSet(parentElement, element);
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
		for (IWorkingSetFilter filter : getWorkingSetFilter()) {
			if (filter.isInWorkingSet(getElementsFromWorkingSet(parentElement), parentElement, element)) {
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

	private Set<IWorkingSetFilter> getWorkingSetFilter() {
		Set<IWorkingSetFilter> workingSetFilter = new HashSet<IWorkingSetFilter>();
		IExtensionPoint point = Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						"org.springframework.ide.eclipse.ui.contentContribution");
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					String uri = config.getAttribute("viewerFilter");
					if (uri != null) {
						try {
							Object handler = config
									.createExecutableExtension("viewerFilter");
							if (handler instanceof IWorkingSetFilter) {
								IWorkingSetFilter contentProvider = (IWorkingSetFilter) handler;
								workingSetFilter.add(contentProvider);
							}
						}
						catch (CoreException e) {
						}
					}
				}
			}
		}
		return workingSetFilter;
	}
}
