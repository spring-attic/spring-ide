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
package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesConfig;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesConfigSet;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Property page tab for defining the beans config sets.
 * 
 * @author Torsten Juergeleit
 */
public class ConfigSetsTab {

	private static final String PREFIX = "ConfigurationPropertyPage."
		+ "tabConfigSets.";
	private static final String DESCRIPTION = PREFIX + "description";
	private static final String NEW_BUTTON = PREFIX + "newButton";
	private static final String EDIT_BUTTON = PREFIX + "editButton";
	private static final String REMOVE_BUTTON = PREFIX + "removeButton";
	private static final String UP_BUTTON = PREFIX + "upButton";
	private static final String DOWN_BUTTON = PREFIX + "downButton";

	private static final int TABLE_WIDTH = 250;

	private PropertiesModel model;
	private PropertiesProject project;

	private Tree configSetsTree;
	private TreeViewer configSetsViewer;
	private IModelElement selectedElement;
	private Button newButton, editButton, removeButton, upButton, downButton;

	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private IModelChangeListener modelChangeListener =
				new IModelChangeListener() {
		public void elementChanged(ModelChangeEvent event) {
			if (configSetsViewer != null
					&& !configSetsViewer.getControl().isDisposed()) {
				configSetsViewer.refresh();
			}
		}
	};

	private boolean hasUserMadeChanges;

	public ConfigSetsTab(PropertiesModel model, IProject project) {
		this.model = model;
		this.project = (PropertiesProject) model.getProject(project);
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

		// Create table and viewer for Spring bean configurations
		configSetsTree = new Tree(tableAndButtons, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = TABLE_WIDTH;
		configSetsTree.setLayoutData(data);
		configSetsTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleTreeSelectionChanged();
			}
		});
		configSetsViewer = new TreeViewer(configSetsTree);
		configSetsViewer.setContentProvider(new ConfigSetContentProvider(
				project));
		configSetsViewer.setLabelProvider(new PropertiesModelLabelProvider());
		configSetsViewer.setSorter(new ConfigSetsSorter());
		configSetsViewer.setInput(project.getProject()); // activate content provider
		configSetsViewer.expandToLevel(project, 1);
		configSetsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		// Create button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		newButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(NEW_BUTTON), buttonListener);
		editButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(EDIT_BUTTON), buttonListener, 0, false);
		removeButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(REMOVE_BUTTON), buttonListener, 0, false);
		upButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(UP_BUTTON), buttonListener, 0, false);
		downButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(DOWN_BUTTON), buttonListener, 0, false);
		model.addChangeListener(modelChangeListener);
		return composite;
	}

	public void dispose() {
		model.removeChangeListener(modelChangeListener);
	}

	/**
	 * The user has selected a different configuration in table. Update button
	 * enablement.
	 */
	private void handleTreeSelectionChanged() {
		boolean configSetButtonsEnabled = false;
		boolean moveButtonsEnabled = false;
		IStructuredSelection selection = (IStructuredSelection) configSetsViewer
				.getSelection();
		Object selected = selection.getFirstElement();
		if (selected != null) {
			if (selected instanceof PropertiesConfigSet) {
				selectedElement = (PropertiesConfigSet) selected;
				configSetButtonsEnabled = true;
			} else if (selected instanceof PropertiesConfig) {
				PropertiesConfig config = (PropertiesConfig) selected;
				PropertiesConfigSet configSet = (PropertiesConfigSet) config
						.getElementParent();
				if (configSet != null && configSet.getConfigs().size() > 1) {
					selectedElement = config;
					moveButtonsEnabled = true;
				}
			} else {
				selectedElement = null;
			}
		} else {
			selectedElement = null;
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
		ConfigSetDialog dialog = new ConfigSetDialog(SpringUIUtils
				.getStandardDisplay().getActiveShell(), project, null);
		if (dialog.open() == Window.OK) {
			configSetsViewer.refresh(false);
			hasUserMadeChanges = true;
		}
	}

	private void handleDoubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof PropertiesProject) {

				// Expand or collapse selected project
				if (configSetsViewer.getExpandedState(elem)) {
					configSetsViewer.collapseToLevel(elem,
							AbstractTreeViewer.ALL_LEVELS);
				} else {
					configSetsViewer.expandToLevel(elem, 1);
				}
			} else if (elem instanceof PropertiesConfigSet) {

				// Edit corresponding config set
				handleEditButtonPressed();
			}
		}
	}

	/**
	 * The user has pressed the add button. Opens the configuration selection
	 * dialog and adds the selected configuration.
	 */
	private void handleEditButtonPressed() {
		if (selectedElement != null
				&& selectedElement instanceof PropertiesConfigSet) {
			ConfigSetDialog dialog = new ConfigSetDialog(SpringUIUtils
					.getStandardDisplay().getActiveShell(), project,
					selectedElement.getElementName());
			if (dialog.open() == Window.OK) {
				configSetsViewer.refresh(false);
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the remove button. Delete the selected config set.
	 */
	private void handleRemoveButtonPressed() {
		if (selectedElement != null
				&& selectedElement instanceof PropertiesConfigSet) {
			project.removeConfigSet(selectedElement.getElementName());
			configSetsViewer.refresh(false);
			hasUserMadeChanges = true;
		}
	}

	/**
	 * The user has pressed the up button. Move the selected config up.
	 */
	private void handleUpButtonPressed() {
		if (selectedElement != null
				&& selectedElement instanceof PropertiesConfig) {
			PropertiesConfig config = (PropertiesConfig) selectedElement;
			PropertiesConfigSet configSet = (PropertiesConfigSet) config
					.getElementParent();
			configSet.moveConfigUp(config);
			configSetsViewer.refresh(false);
			hasUserMadeChanges = true;
		}
	}

	/**
	 * The user has pressed the down button. Move the selected config down.
	 */
	private void handleDownButtonPressed() {
		if (selectedElement != null
				&& selectedElement instanceof PropertiesConfig) {
			PropertiesConfig config = (PropertiesConfig) selectedElement;
			PropertiesConfigSet configSet = (PropertiesConfigSet) config
					.getElementParent();
			configSet.moveConfigDown(config);
			configSetsViewer.refresh(false);
			hasUserMadeChanges = true;
		}
	}

	private static class ConfigSetContentProvider implements
			ITreeContentProvider {

		private PropertiesProject project;

		public ConfigSetContentProvider(PropertiesProject project) {
			this.project = project;
		}

		public Object[] getElements(Object obj) {
			return getChildren(project);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof PropertiesProject) {
				Set<IBeansConfigSet> configSets = ((PropertiesProject)
						parentElement).getConfigSets();
				return configSets.toArray();
			} else if (parentElement instanceof PropertiesConfigSet) {
				Set<IBeansConfig> configs = ((PropertiesConfigSet)
						parentElement).getConfigs();
				return configs.toArray();
			}
			return IModelElement.NO_CHILDREN;
		}

		public Object getParent(Object element) {
			if (element instanceof PropertiesConfigSet) {
				return ((PropertiesConfigSet) element).getElementParent();
			} else if (element instanceof PropertiesConfig) {
				return ((PropertiesConfig) element).getElementParent()
						.getElementParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return (getChildren(element).length > 0);
		}

		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		public void dispose() {
		}
	}

	private static class ConfigSetsSorter extends ViewerSorter {
		@Override
		public void sort(Viewer viewer, Object[] elements) {

			// Do NOT sort configs within a config set
			if (elements.length > 0 && !(elements[0] instanceof IBeansConfig)) {
				super.sort(viewer, elements);
			}
		}
	}
}
