/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;

/**
 * 
 */
@SuppressWarnings("deprecation")
public class ConfigFilesTab {

	/**
	 * 
	 */
	private static final int TABLE_WIDTH = 250;

	/**
	 * 
	 */
	private static final String DESCRIPTION = "ConfigurationPropertyPage.tabConfigFiles.description";

	/**
	 * 
	 */
	private static final String ADD_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.addButton";

	/**
	 * 
	 */
	private static final String REMOVE_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.removeButton";

	/**
	 * 
	 */
	private static final String DIALOG_TITLE = "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.title";

	/**
	 * 
	 */
	private static final String DIALOG_MESSAGE = "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.message";

	/**
	 * 
	 */
	private static final String EDIT_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.editButton";

	/**
	 * 
	 */
	private IAdaptable element;

	/**
	 * 
	 */
	private Table configsTable;

	/**
	 * 
	 */
	private TableViewer configsViewer;

	/**
	 * 
	 */
	private Button addButton, removeButton;

	/**
	 * 
	 */
	private SelectionListener buttonListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	/**
	 * 
	 */
	private boolean hasUserMadeChanges;

	/**
	 * 
	 */
	private IWebflowProject project;

	/**
	 * 
	 */
	private Set<IFile> configFiles;

	/**
	 * 
	 */
	private Map<IFile, Set<IBeansConfig>> configFilesToBeansConfigs;

	/**
	 * 
	 */
	private Button editButton;

	/**
	 * 
	 * 
	 * @param element 
	 * @param project 
	 */
	public ConfigFilesTab(IWebflowProject project, IAdaptable element) {
		this.project = project;
		this.element = element;
		this.configFiles = new HashSet<IFile>();
		this.configFilesToBeansConfigs = new HashMap<IFile, Set<IBeansConfig>>();

		if (project.getConfigs() != null) {
			for (IWebflowConfig config : project.getConfigs()) {
				this.configFiles.add(config.getResource());
				this.configFilesToBeansConfigs.put(config.getResource(), config
						.getBeansConfigs());
			}
		}

	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public boolean hasUserMadeChanges() {
		return hasUserMadeChanges;
	}

	/**
	 * 
	 * 
	 * @param parent 
	 * 
	 * @return 
	 */
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(composite, SWT.WRAP);
		description.setText(Activator.getResourceString(DESCRIPTION));
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(composite, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		tableAndButtons.setLayout(layout);

		// table and viewer for Spring bean configurations
		configsTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
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
				this.configFiles));
		configsViewer.setLabelProvider(new WorkbenchLabelProvider());
		configsViewer.setInput(this.configFiles); // activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());

		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = SpringUIUtils.createButton(buttonArea, Activator
				.getResourceString(ADD_BUTTON), buttonListener, 0, true);
		editButton = SpringUIUtils.createButton(buttonArea, Activator
				.getResourceString(EDIT_BUTTON), buttonListener, 0, false);
		removeButton = SpringUIUtils.createButton(buttonArea, Activator
				.getResourceString(REMOVE_BUTTON), buttonListener, 0, false);
		return composite;
	}

	/**
	 * The user has selected a different configuration in table. Update button
	 * enablement.
	 */
	private void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		}
		else {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		}
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 * 
	 * @param button 
	 */
	private void handleButtonPressed(Button button) {
		if (button == addButton) {
			handleAddButtonPressed();
		}
		else if (button == removeButton) {
			handleRemoveButtonPressed();
		}
		else if (button == editButton) {
			handleEditButtonPressed();
		}
		handleTableSelectionChanged();
		configsTable.setFocus();
	}

	/**
	 * 
	 */
	private void handleEditButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (!selection.isEmpty()) {
			IFile file = (IFile) selection.getFirstElement();
			Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
			if (this.configFilesToBeansConfigs.containsKey(file)) {
				Set<IBeansConfig> oldConfigs = this.configFilesToBeansConfigs
						.get(file);
				configs.addAll(oldConfigs);
			}
			else {
			}
			Dialog dialog = new ConfigSetDialog(SpringUIUtils
					.getStandardDisplay().getActiveShell(), project
					.getProject(), configs);
			if (dialog.open() == Dialog.OK) {
				this.configFilesToBeansConfigs.put(file, configs);
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the add button. Opens the configuration selection
	 * dialog and adds the selected configuration.
	 */
	private void handleAddButtonPressed() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				SpringUIUtils.getStandardDisplay().getActiveShell(),
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setTitle(Activator.getResourceString(DIALOG_TITLE));
		dialog.setMessage(Activator.getResourceString(DIALOG_MESSAGE));
		dialog.addFilter(new FileFilter(new String[] { "xml" }));
		dialog.setValidator(new FileSelectionValidator(true));
		dialog.setInput(element);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (int i = 0; i < selection.length; i++) {
					IFile file = (IFile) selection[i];
					configFiles.add(file);
				}
				hasUserMadeChanges = true;
				configsViewer.refresh();
			}
		}
	}

	/**
	 * The user has pressed the remove button. Delete the selected
	 * configuration.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (!selection.isEmpty()) {
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				configFiles.remove((IFile) elements.next());
			}
			configsViewer.refresh();
			hasUserMadeChanges = true;
		}
	}

	/**
	 * 
	 */
	private class ConfigFilesContentProvider implements
			IStructuredContentProvider {

		/**
		 * 
		 */
		private Set<IFile> files;

		/**
		 * 
		 * 
		 * @param files 
		 */
		public ConfigFilesContentProvider(Set<IFile> files) {
			this.files = files;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object obj) {
			return files.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}
	}

	/**
	 * 
	 */
	private class ConfigFilesSorter extends ViewerSorter {

		// Categories
		/**
		 * 
		 */
		public static final int SUB_DIR = 0;

		/**
		 * 
		 */
		public static final int ROOT_DIR = 1;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
		 */
		public int category(Object element) {
			return (((IFile) element).getName().indexOf('/') == -1 ? ROOT_DIR
					: SUB_DIR);
		}
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public Map<IFile, Set<IBeansConfig>> getConfigFilesToBeansConfigs() {
		return configFilesToBeansConfigs;
	}

	/**
	 * 
	 * 
	 * @param configFiles 
	 */
	public void setConfigFiles(Set<IFile> configFiles) {
		this.configFiles = configFiles;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public Set<IFile> getConfigFiles() {
		return configFiles;
	}
}
