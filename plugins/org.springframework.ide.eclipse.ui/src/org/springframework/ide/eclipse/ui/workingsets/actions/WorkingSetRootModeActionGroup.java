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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.springframework.ide.eclipse.ui.workingsets.WorkingSetContentProvider;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WorkingSetRootModeActionGroup extends ActionGroup {

	private IExtensionStateModel stateModel;

	private StructuredViewer structuredViewer;

	private boolean hasContributedToViewMenu = false;

	private IAction workingSetsAction = null;

	private IAction projectsAction = null;

	private IAction[] actions;

	private int currentSelection;

	private MenuItem[] items;

	private class TopLevelContentAction extends Action implements IAction {

		private final boolean groupWorkingSets;

		/**
		 * Construct an Action that represents a toggle-able state between
		 * Showing top level Working Sets and Projects.
		 * @param toGroupWorkingSets
		 */
		public TopLevelContentAction(boolean toGroupWorkingSets) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			groupWorkingSets = toGroupWorkingSets;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			if (stateModel
					.getBooleanProperty(WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS) != groupWorkingSets) {
				stateModel.setBooleanProperty(
						WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
						groupWorkingSets);

				structuredViewer.getControl().setRedraw(false);
				try {
					structuredViewer.refresh();
				}
				finally {
					structuredViewer.getControl().setRedraw(true);
				}
			}
		}
	}

	/**
	 * Create an action group that will listen to the stateModel and update the
	 * structuredViewer when necessary.
	 * @param structuredViewer
	 * @param stateModel
	 */
	public WorkingSetRootModeActionGroup(StructuredViewer aStructuredViewer,
			IExtensionStateModel aStateModel) {
		super();
		structuredViewer = aStructuredViewer;
		stateModel = aStateModel;
	}

	/*
	 * (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		if (!hasContributedToViewMenu) {
			synchronized (this) {
				if (!hasContributedToViewMenu) {
					hasContributedToViewMenu = true;
					contributeToViewMenu(actionBars.getMenuManager());
				}
			}
		}
	}

	private void contributeToViewMenu(IMenuManager viewMenu) {
		viewMenu.add(new Separator());

		// Create layout sub menu

		IMenuManager topLevelSubMenu = new MenuManager(
				WorkbenchNavigatorMessages.WorkingSetRootModeActionGroup_Top_Level_Element_);
		final String layoutGroupName = "topLevelElements"; //$NON-NLS-1$
		Separator marker = new Separator(layoutGroupName);

		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(marker);
		viewMenu.appendToGroup(layoutGroupName, topLevelSubMenu);
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end"));//$NON-NLS-1$		
		addActions(topLevelSubMenu);
	}

	/**
	 * Adds the actions to the given menu manager.
	 */
	protected void addActions(IMenuManager viewMenu) {

		viewMenu.add(new Separator());
		items = new MenuItem[actions.length];

		for (int i = 0; i < actions.length; i++) {
			final int j = i;

			viewMenu.add(new ContributionItem() {

				public void fill(Menu menu, int index) {

					int style = SWT.CHECK;
					if ((actions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0)
						style = SWT.RADIO;

					final MenuItem mi = new MenuItem(menu, style, index);
					items[j] = mi;
					mi.setText(actions[j].getText());
					mi.setSelection(currentSelection == j);
					mi.addSelectionListener(new SelectionAdapter() {

						public void widgetSelected(SelectionEvent e) {
							if (currentSelection == j) {
								items[currentSelection].setSelection(true);
								return;
							}
							actions[j].run();

							// Update checked state
							items[currentSelection].setSelection(false);
							currentSelection = j;
							items[currentSelection].setSelection(true);
						}

					});

				}

				public boolean isDynamic() {
					return false;
				}
			});
		}
	}

	private IAction[] createActions() {

		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();

		projectsAction = new TopLevelContentAction(false);
		projectsAction
				.setText(WorkbenchNavigatorMessages.WorkingSetRootModeActionGroup_Project_);
		projectsAction.setImageDescriptor(sharedImages
				.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT));

		workingSetsAction = new TopLevelContentAction(true);
		workingSetsAction
				.setText(WorkbenchNavigatorMessages.WorkingSetRootModeActionGroup_Working_Set_);
		workingSetsAction.setImageDescriptor(WorkbenchNavigatorPlugin
				.getDefault().getImageRegistry().getDescriptor(
						"full/obj16/workingsets.gif")); //$NON-NLS-1$

		return new IAction[] { projectsAction, workingSetsAction };
	}

	/**
	 * Toggle whether top level working sets should be displayed as a group or
	 * collapse to just show their contents.
	 * @param showTopLevelWorkingSets
	 */
	public void setShowTopLevelWorkingSets(boolean showTopLevelWorkingSets) {
		if (actions == null) {
			actions = createActions();
			setActions(actions, showTopLevelWorkingSets ? 1 /*
															 * Show Top Level
															 * Working Sets
															 */
					: 0);
		}
		workingSetsAction.setChecked(showTopLevelWorkingSets);
		projectsAction.setChecked(!showTopLevelWorkingSets);

		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				items[i].setSelection(actions[i].isChecked());
			}
		}
		if (stateModel != null) {
			stateModel.setBooleanProperty(
					WorkingSetContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
					showTopLevelWorkingSets);
		}

	}

	/**
	 * Configure the actions that are displayed in the menu by this ActionGroup.
	 * @param theActions An array of possible actions.
	 * @param selected The index of the "enabled" action.
	 */
	private void setActions(IAction[] theActions, int selected) {
		actions = theActions;
		currentSelection = selected;

	}

	public void setStateModel(IExtensionStateModel sStateModel) {
		stateModel = sStateModel;
	}
}
