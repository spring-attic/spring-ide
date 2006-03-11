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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPropertyListener;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class ConfigSetsTab {

	private static final int TABLE_WIDTH = 250;
	private static final String DESCRIPTION =
						  "ConfigurationPropertyPage.tabConfigSets.description";
	private static final String NEW_BUTTON =
							"ConfigurationPropertyPage.tabConfigSets.newButton";
	private static final String EDIT_BUTTON =
						   "ConfigurationPropertyPage.tabConfigSets.editButton";
	private static final String REMOVE_BUTTON =
						 "ConfigurationPropertyPage.tabConfigSets.removeButton";
	private static final String UP_BUTTON =
							 "ConfigurationPropertyPage.tabConfigSets.upButton";
	private static final String DOWN_BUTTON =
						   "ConfigurationPropertyPage.tabConfigSets.downButton";
	private ProjectNode project;
	private Object element;
	private List configSets;
	private Tree configSetsTree;
	private TreeViewer configSetsViewer;
	private INode selectedNode;
	private Button newButton, editButton, removeButton, upButton, downButton;

	private SelectionListener buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleButtonPressed((Button) e.widget);
			}
		};

	private IPropertyListener propertyListener = new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				handlePropertyChanged(source, propId);
			}
		};

	private boolean hasUserMadeChanges;

	public ConfigSetsTab(ProjectNode project, IAdaptable element) {
		this.project = project;
		this.element = element;

		this.project.addPropertyListener(propertyListener);
	}

	public List getConfigSets() {
		return configSets;
	}

	public boolean hasUserMadeChanges() {
		return hasUserMadeChanges;
	}

	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(composite, SWT.WRAP);
		description.setText(BeansUIPlugin.getResourceString(DESCRIPTION));
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(composite, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		tableAndButtons.setLayout(layout);

		// table and viewer for Spring bean configurations		
		configSetsTree = new Tree(tableAndButtons,
								  SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = TABLE_WIDTH;
		configSetsTree.setLayoutData(data);
		configSetsTree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTreeSelectionChanged();
			}
		});
		configSetsViewer = new TreeViewer(configSetsTree);
		configSetsViewer.setContentProvider(new ConfigSetContentProvider(
																	 project));
		configSetsViewer.setLabelProvider(new ModelLabelProvider());
		configSetsViewer.setSorter(new ConfigSetsSorter());
		configSetsViewer.setInput(element);	// activate content provider
		configSetsViewer.expandToLevel(project, 1);
		configSetsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		newButton = SpringUIUtils.createButton(buttonArea,
				  BeansUIPlugin.getResourceString(NEW_BUTTON), buttonListener);
		editButton = SpringUIUtils.createButton(buttonArea,
				  BeansUIPlugin.getResourceString(EDIT_BUTTON), buttonListener,
				  0, false);
		removeButton = SpringUIUtils.createButton(buttonArea,
		 		BeansUIPlugin.getResourceString(REMOVE_BUTTON), buttonListener,
		 		0, false);
		upButton = SpringUIUtils.createButton(buttonArea,
					BeansUIPlugin.getResourceString(UP_BUTTON), buttonListener,
					0, false);
		downButton = SpringUIUtils.createButton(buttonArea,
				  BeansUIPlugin.getResourceString(DOWN_BUTTON), buttonListener,
				  0, false);
		return composite;
	}

	/**
	 * The user has selected a different configuration in table.
	 * Update button enablement.
	 */
	private void handleTreeSelectionChanged() {
		boolean configSetButtonsEnabled = false;
		boolean moveButtonsEnabled = false;
		IStructuredSelection selection = (IStructuredSelection)
												configSetsViewer.getSelection();
		Object selected = selection.getFirstElement();
		if (selected != null) {
			if (selected instanceof ConfigSetNode) {
				selectedNode = (ConfigSetNode) selected;
				configSetButtonsEnabled = true;
			} else if (selected instanceof ConfigNode) {
				ConfigNode config = (ConfigNode) selected;
				ConfigSetNode configSet = (ConfigSetNode) config.getParent();
				if (configSet != null && configSet.getConfigCount() > 1) {
					selectedNode = (ConfigNode) selected;
					moveButtonsEnabled = true;
				}
			} else {
				selectedNode = null;
			}
		} else {
			selectedNode = null;
		}
		editButton.setEnabled(configSetButtonsEnabled);
		removeButton.setEnabled(configSetButtonsEnabled);
		upButton.setEnabled(moveButtonsEnabled);
		downButton.setEnabled(moveButtonsEnabled);
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 */
	private void handleButtonPressed(Button button) {
		if (button == newButton) {
			handleNewButtonPressed();
		} else if (button == editButton) {
			handleEditButtonPressed();
		} else if (button == removeButton) {
			handleRemoveButtonPressed();
		} else if (button == upButton) {
			handleUpButtonPressed();
		} else if (button == downButton) {
			handleDownButtonPressed();
		}
		handleTreeSelectionChanged();
		configSetsTree.setFocus();
	}

	/**
	 * The user has pressed the new button. Opens the config set definition
	 * dialog and adds the specified config set.
	 */
	private void handleNewButtonPressed() {
		ConfigSetDialog dialog = new ConfigSetDialog(
							SpringUIUtils.getStandardDisplay().getActiveShell(),
							project, null);
		if (dialog.open() == ConfigSetDialog.OK) {
			hasUserMadeChanges = true;
		}
	}

	private void handleDoubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof ProjectNode) {

				// expand or collapse selected project
				if (configSetsViewer.getExpandedState(elem)) {
					configSetsViewer.collapseToLevel(elem,
													 TreeViewer.ALL_LEVELS);
				} else {
					configSetsViewer.expandToLevel(elem, 1);
				}
			} else if (elem instanceof ConfigSetNode){

				// edit corresponding config set
				handleEditButtonPressed();
			}
		}
	}

	/**
	 * The user has pressed the add button. Opens the configuration selection
	 * dialog and adds the selected configuration.
	 */
	private void handleEditButtonPressed() {
		if (selectedNode != null && selectedNode instanceof ConfigSetNode) {
			ConfigSetDialog dialog = new ConfigSetDialog(
							SpringUIUtils.getStandardDisplay().getActiveShell(),
							project, selectedNode.getName());
			if (dialog.open() == ConfigSetDialog.OK) {
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the remove button. Delete the selected config set.
	 */
	private void handleRemoveButtonPressed() {
		if (selectedNode != null && selectedNode instanceof ConfigSetNode) {
			project.removeConfigSet(selectedNode.getName());

			hasUserMadeChanges = true;
		}
	}

	/**
	 * The user has pressed the up button. Move the selected config up.
	 */
	private void handleUpButtonPressed() {
		if (selectedNode != null && selectedNode instanceof ConfigNode) {
			ConfigNode config = (ConfigNode) selectedNode;
			ConfigSetNode configSet = (ConfigSetNode) config.getParent();
			configSet.moveConfigUp(config);

			hasUserMadeChanges = true;
		}
	}

	/**
	 * The user has pressed the down button. Move the selected config down.
	 */
	private void handleDownButtonPressed() {
		if (selectedNode != null && selectedNode instanceof ConfigNode) {
			ConfigNode config = (ConfigNode) selectedNode;
			ConfigSetNode configSet = (ConfigSetNode) config.getParent();
			configSet.moveConfigDown(config);

			hasUserMadeChanges = true;
		}
	}

	private void handlePropertyChanged(Object source, int propId) {
		if (configSetsViewer != null &&
								  !configSetsViewer.getControl().isDisposed()) {
			configSetsViewer.refresh();
		}
	}

	public void dispose() {
		project.removePropertyListener(propertyListener);
	}

	private class ConfigSetsSorter extends ViewerSorter {
		public void sort(Viewer viewer, Object[] elements) {

			// Do NOT sort configs within a config set
			if (elements.length > 0 && !(elements[0] instanceof ConfigNode)) {
				super.sort(viewer, elements);
			}
		}
	}
}
