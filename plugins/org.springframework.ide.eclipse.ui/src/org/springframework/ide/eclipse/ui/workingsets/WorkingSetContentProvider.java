/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
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
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WorkingSetContentProvider implements ICommonContentProvider {

	public static final String EXTENSION_ID = "org.springframework.ide.eclipse.ui.navigator.workingsets"; //$NON-NLS-1$

	public static final String SHOW_TOP_LEVEL_WORKING_SETS = EXTENSION_ID
			+ ".showTopLevelWorkingSets";

	private static final Object[] NO_CHILDREN = new Object[0];

	private static final int WORKING_SETS = 0;

	private static final int PROJECTS = 1;

	private WorkingSetHelper helper;

	private int rootMode = WORKING_SETS;

	private IExtensionStateModel extensionStateModel;

	private IPropertyChangeListener rootModeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (SHOW_TOP_LEVEL_WORKING_SETS.equals(event.getProperty())) {
				updateRootMode();
			}
		}
	};

	public void init(ICommonContentExtensionSite aConfig) {
		extensionStateModel = aConfig.getExtensionStateModel();
		extensionStateModel.addPropertyChangeListener(rootModeListener);
		updateRootMode();
	}

	public void restoreState(IMemento aMemento) {

	}

	public void saveState(IMemento aMemento) {

	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkingSet) {
			IWorkingSet workingSet = (IWorkingSet) parentElement;
			if (workingSet.isAggregateWorkingSet()) {
				switch (rootMode) {
				case WORKING_SETS:
					Set<IWorkingSet> filteredWorkingSet = new HashSet<IWorkingSet>();
					IWorkingSet[] workingSets = ((AggregateWorkingSet) workingSet)
							.getComponents();
					for (IWorkingSet ws : workingSets) {
						if ("org.springframework.ide.eclipse.ui.springWorkingSetPage"
								.equals(ws.getId())) {
							filteredWorkingSet.add(ws);
						}
					}
					return filteredWorkingSet.toArray();
				case PROJECTS:
					return filterWorkingSet(workingSet).toArray();
				}
			}
			return filterWorkingSet(workingSet).toArray();
		}
		return NO_CHILDREN;
	}

	private Set<ISpringProject> filterWorkingSet(IWorkingSet workingSet) {
		Set<ISpringProject> projects = new HashSet<ISpringProject>();
		IAdaptable[] elements = workingSet.getElements();
		for (IAdaptable element : elements) {
			IProject project = (IProject) element.getAdapter(IProject.class);
			if (project == null && element instanceof IFile) {
				project = ((IFile) element).getProject();
			}
			if (project != null) {
				ISpringProject springProject = SpringCore.getModel()
						.getProject(project);
				if (springProject != null) {
					projects.add(springProject);
				}
			}
		}
		return projects;
	}

	public Object getParent(Object element) {
		if (helper != null)
			return helper.getParent(element);
		return null;
	}

	public boolean hasChildren(Object element) {
		return true;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		helper = null;
		extensionStateModel.removePropertyChangeListener(rootModeListener);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IWorkingSet) {
			IWorkingSet rootSet = (IWorkingSet) newInput;
			helper = new WorkingSetHelper(rootSet);
		}

	}

	private void updateRootMode() {
		if (extensionStateModel.getBooleanProperty(SHOW_TOP_LEVEL_WORKING_SETS))
			rootMode = WORKING_SETS;
		else
			rootMode = PROJECTS;
	}

	protected class WorkingSetHelper {

		private final IWorkingSet workingSet;

		private final Map<IAdaptable, IWorkingSet> parents = new WeakHashMap<IAdaptable, IWorkingSet>();

		/**
		 * Create a Helper class for the given working set
		 * @param set The set to use to build the item to parent map.
		 */
		public WorkingSetHelper(IWorkingSet set) {
			workingSet = set;

			if (workingSet.isAggregateWorkingSet()) {
				AggregateWorkingSet aggregateSet = (AggregateWorkingSet) workingSet;

				IWorkingSet[] components = aggregateSet.getComponents();

				for (int componentIndex = 0; componentIndex < components.length; componentIndex++) {
					IAdaptable[] elements = components[componentIndex]
							.getElements();
					for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
						parents.put(elements[elementsIndex],
								components[componentIndex]);
					}
					parents.put(components[componentIndex], aggregateSet);

				}
			}
			else {
				IAdaptable[] elements = workingSet.getElements();
				for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
					parents.put(elements[elementsIndex], workingSet);
				}
			}
		}

		/**
		 * @param element An element from the viewer
		 * @return The parent associated with the element, if any.
		 */
		public Object getParent(Object element) {
			return parents.get(element);
		}
	}
}
