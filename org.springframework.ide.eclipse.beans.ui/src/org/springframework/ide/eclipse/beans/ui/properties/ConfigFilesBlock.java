/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.ProjectNode;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class ConfigFilesBlock {

	private static final int TABLE_WIDTH = 250;
	private static final String DESCRIPTION =
						 "ConfigurationPropertyPage.tabConfigFiles.description";
	private static final String ADD_BUTTON =
						   "ConfigurationPropertyPage.tabConfigFiles.addButton";
	private static final String REMOVE_BUTTON =
						"ConfigurationPropertyPage.tabConfigFiles.removeButton";
	private static final String DIALOG_TITLE =
			   "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.title";
	private static final String DIALOG_MESSAGE =
			 "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.message";
	private ProjectNode project;
	private IAdaptable element;
	private Table configsTable;
	private TableViewer configsViewer;
	private Button addButton, removeButton;

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

	public ConfigFilesBlock(ProjectNode project, IAdaptable element) {
		this.project = project;
		this.element = element;

		this.project.addPropertyListener(propertyListener);
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
		configsTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL |
								SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = TABLE_WIDTH;
		configsTable.setLayoutData(data);
		configsTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		configsViewer = new TableViewer(configsTable);
		configsViewer.setContentProvider(new ConfigFilesContentProvider(
																	  project));
		configsViewer.setLabelProvider(new ModelLabelProvider());
		configsViewer.setInput(this);	// activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());

		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = SpringUIUtils.createButton(buttonArea,
							  BeansUIPlugin.getResourceString(ADD_BUTTON), true,
							  buttonListener);
		removeButton = SpringUIUtils.createButton(buttonArea,
		 				  BeansUIPlugin.getResourceString(REMOVE_BUTTON), false,
						  buttonListener);
		return composite;
	}

	/**
	 * The user has selected a different configuration in table.
	 * Update button enablement.
	 */
	private void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection)
												   configsViewer.getSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
		}
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 */
	private void handleButtonPressed(Button button) {
		if (button == addButton) {
			handleAddButtonPressed();
		} else if (button == removeButton) {
			handleRemoveButtonPressed();
		}
		handleTableSelectionChanged();
		configsTable.setFocus();
	}

	/**
	 * The user has pressed the add button. Opens the configuration selection
	 * dialog and adds the selected configuration.
	 */
	private void handleAddButtonPressed() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				  SpringUIUtils.getStandardDisplay().getActiveShell(),
				  new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setTitle(BeansUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(BeansUIPlugin.getResourceString(DIALOG_MESSAGE));
		dialog.addFilter(new FileFilter(new String[] {
									   IBeansConfig.DEFAULT_FILE_EXTENSION }));
		dialog.setValidator(new FileSelectionValidator(true));
		dialog.setInput(element);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (int i = 0; i < selection.length; i++) {
					IFile file = (IFile) selection[i];
					String config = file.getProjectRelativePath().toString();
					project.addConfig(config);
				}
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the remove button. Delete the selected
	 * configuration.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection)
												   configsViewer.getSelection();
		if (!selection.isEmpty()) {
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				ConfigNode node = (ConfigNode) elements.next();
				project.removeConfig(node.getName());
			}
			hasUserMadeChanges = true;
		}
	}

	private void handlePropertyChanged(Object source, int propId) {
		if (configsViewer != null && !configsViewer.getControl().isDisposed()) {
			configsViewer.refresh();
		}
	}

	public void dispose() {
		project.removePropertyListener(propertyListener);
	}

	private class ConfigFilesContentProvider
										 implements IStructuredContentProvider {
		private ProjectNode project;
	
		public ConfigFilesContentProvider(ProjectNode project) {
			this.project = project;
		}
	
		public Object[] getElements(Object obj) {
			return project.getConfigs().toArray();
		}
	
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	
		public void dispose() {
		}
	}

	private class ConfigFilesSorter extends ViewerSorter {

	    // Categories
	    public static final int SUB_DIR = 0;
	    public static final int ROOT_DIR = 1;
	
		public int category(Object element) {
			return (((ConfigNode) element).getName().indexOf('/') == -1 ?
															ROOT_DIR : SUB_DIR);
		}
	}
}
