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
package org.springframework.ide.eclipse.ui.workingsets.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.springframework.ide.eclipse.ui.workingsets.WorkingSetsViewerFilter;
import org.springframework.ide.eclipse.ui.workingsets.WorkingSetContentProvider;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WorkingSetActionProvider extends CommonActionProvider {

	private static final String TAG_CURRENT_WORKING_SET_NAME = "currentWorkingSetName"; //$NON-NLS-1$

	private boolean contributedToViewMenu = false;

	private boolean ready = false;

	private StructuredViewer viewer;

	private INavigatorContentService contentService;

	private WorkingSetFilterActionGroup workingSetActionGroup;

	private WorkingSetRootModeActionGroup workingSetRootModeActionGroup;

	private Object originalViewerInput = ResourcesPlugin.getWorkspace()
			.getRoot();

	private IExtensionStateModel extensionStateModel;

	private WorkingSetsViewerFilter workingSetFilter = null;

	public class WorkingSetManagerListener implements IPropertyChangeListener {

		private boolean listening = false;

		public void propertyChange(PropertyChangeEvent event) {

			Object input = viewer.getInput();
			if (input instanceof IWorkingSet) {
				IWorkingSet workingSet = (IWorkingSet) input;

				String property = event.getProperty();
				Object newValue = event.getNewValue();
				Object oldValue = event.getOldValue();

				if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE
						.equals(property)
						&& oldValue == workingSet) {
					if (viewer != null) {
						viewer.setInput(originalViewerInput);
					}
				}
				else if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE
						.equals(property)
						&& newValue == workingSet) {
				}
				else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE
						.equals(property)
						&& newValue == workingSet) {
					if (viewer != null) {
						viewer.refresh();
					}
				}
			}

		}

		/**
		 * Begin listening to the correct source if not already listening.
		 */
		public synchronized void listen() {
			if (!listening) {
				PlatformUI.getWorkbench().getWorkingSetManager()
						.addPropertyChangeListener(managerChangeListener);
				listening = true;
			}
		}

		/**
		 * Begin listening to the correct source if not already listening.
		 */
		public synchronized void ignore() {
			if (listening) {
				PlatformUI.getWorkbench().getWorkingSetManager()
						.removePropertyChangeListener(managerChangeListener);
				listening = false;
			}
		}
	}

	private IPropertyChangeListener filterChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			IWorkingSet oldWorkingSet = (IWorkingSet) event.getOldValue();
			IWorkingSet newWorkingSet = (IWorkingSet) event.getNewValue();

			if (newWorkingSet != null
					&& !contentService
							.isActive(WorkingSetContentProvider.EXTENSION_ID)) {
				contentService
						.getActivationService()
						.activateExtensions(
								new String[] { WorkingSetContentProvider.EXTENSION_ID },
								false);
				contentService.getActivationService()
						.persistExtensionActivations();
			}

			if (viewer != null) {
				if (newWorkingSet == null) {
					workingSetFilter.setWorkingSet(null);
					viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
				}
				else if (oldWorkingSet != newWorkingSet) {
					workingSetFilter.setWorkingSet(newWorkingSet);
					viewer.setInput(newWorkingSet);
				}
			}

		}
	};

	private WorkingSetManagerListener managerChangeListener = new WorkingSetManagerListener();

	private IExtensionActivationListener activationListener = new IExtensionActivationListener() {

		private IWorkingSet workingSet;

		public void onExtensionActivation(String aViewerId,
				String[] theNavigatorExtensionIds, boolean isActive) {

			for (int i = 0; i < theNavigatorExtensionIds.length; i++) {
				if (WorkingSetContentProvider.EXTENSION_ID
						.equals(theNavigatorExtensionIds[i])) {
					if (isActive) {
						extensionStateModel = contentService
								.findStateModel(WorkingSetContentProvider.EXTENSION_ID);
						workingSetRootModeActionGroup
								.setStateModel(extensionStateModel);

						if (workingSet != null) {
							viewer.setInput(workingSet);
							workingSetActionGroup.setWorkingSet(workingSet);
							workingSetFilter.setWorkingSet(workingSet);
							workingSetRootModeActionGroup
									.setShowTopLevelWorkingSets(true);
						}
						managerChangeListener.listen();

					}
					else {
						Object input = viewer.getInput();
						if (input instanceof IWorkingSet) {
							workingSet = (IWorkingSet) input;
							if (viewer != null && input != originalViewerInput) {
								viewer.setInput(originalViewerInput);
							}
							workingSetFilter.setWorkingSet(workingSet);
						}
						else {
							workingSet = null;
							workingSetFilter.setWorkingSet(null);
						}
						managerChangeListener.ignore();
						workingSetActionGroup.setWorkingSet(null);
						workingSetRootModeActionGroup
								.setShowTopLevelWorkingSets(false);

					}
				}
			}
		}

	};

	public void init(ICommonActionExtensionSite aSite) {
		viewer = aSite.getStructuredViewer();

		workingSetFilter = new WorkingSetsViewerFilter();
		viewer.addFilter(workingSetFilter);

		contentService = aSite.getContentService();

		extensionStateModel = contentService
				.findStateModel(WorkingSetContentProvider.EXTENSION_ID);

		workingSetActionGroup = new WorkingSetFilterActionGroup(aSite
				.getViewSite().getShell(), filterChangeListener);

		if (extensionStateModel != null) {
			workingSetRootModeActionGroup = new WorkingSetRootModeActionGroup(
					viewer, extensionStateModel);
		}

		if (contentService.isActive(WorkingSetContentProvider.EXTENSION_ID)) {
			managerChangeListener.listen();
		}

		contentService.getActivationService().addExtensionActivationListener(
				activationListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		super.restoreState(aMemento);

		boolean showWorkingSets = true;
		if (aMemento != null) {
			Integer showWorkingSetsInt = aMemento
					.getInteger(WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS);
			showWorkingSets = showWorkingSetsInt == null
					|| showWorkingSetsInt.intValue() == 1;
			extensionStateModel.setBooleanProperty(
					WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
					showWorkingSets);
			workingSetRootModeActionGroup
					.setShowTopLevelWorkingSets(showWorkingSets);

			if (viewer != null) {
				String lastWorkingSetName = aMemento
						.getString(TAG_CURRENT_WORKING_SET_NAME);
				IWorkingSetManager workingSetManager = PlatformUI
						.getWorkbench().getWorkingSetManager();
				IWorkingSet lastWorkingSet = workingSetManager
						.getWorkingSet(lastWorkingSetName);
				viewer.setInput(lastWorkingSet);
				workingSetFilter.setWorkingSet(lastWorkingSet);
				workingSetActionGroup.setWorkingSet(lastWorkingSet);
			}
			ready = true;
		}

	}

	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);

		if (aMemento != null) {
			int showWorkingSets = extensionStateModel
					.getBooleanProperty(WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS) ? 1
					: 0;
			aMemento.putInteger(
					WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
					showWorkingSets);

			if (viewer != null) {
				Object input = viewer.getInput();
				if (input instanceof IWorkingSet) {
					IWorkingSet workingSet = (IWorkingSet) input;
					aMemento.putString(TAG_CURRENT_WORKING_SET_NAME, workingSet
							.getName());
				}
			}
		}

	}

	public void fillActionBars(IActionBars actionBars) {
		if (ready) {
			if (!contributedToViewMenu) {
				try {
					super.fillActionBars(actionBars);
					workingSetActionGroup.fillActionBars(actionBars);
					if (workingSetRootModeActionGroup != null) {
						workingSetRootModeActionGroup
								.fillActionBars(actionBars);
					}
				}
				finally {
					contributedToViewMenu = true;
				}
			}
		}
	}

	public void dispose() {
		super.dispose();
		workingSetActionGroup.dispose();
		if (workingSetRootModeActionGroup != null) {
			workingSetRootModeActionGroup.dispose();
		}

		managerChangeListener.ignore();

		contentService.getActivationService()
				.removeExtensionActivationListener(activationListener);
	}
}
