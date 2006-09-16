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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.dialogs.FilteredElementTreeSelectionDialog;
import org.springframework.ide.eclipse.ui.dialogs.StorageSelectionValidator;
import org.springframework.ide.eclipse.ui.viewers.JavaFileExtensionFilter;

public class ConfigFilesTab {

	private static final String PREFIX = "ConfigurationPropertyPage."
			+ "tabConfigFiles.";

	private static final String DESCRIPTION = PREFIX + "description";

	private static final String EXTENSIONS_LABEL = PREFIX + "extensions.label";

	private static final String ERROR_NO_EXTENSIONS = PREFIX
			+ "error.noExtensions";

	private static final String ERROR_INVALID_EXTENSIONS = PREFIX
			+ "error.invalidExtensions";

	private static final String ADD_BUTTON = PREFIX + "addButton";

	private static final String REMOVE_BUTTON = PREFIX + "removeButton";

	private static final String DIALOG_TITLE = PREFIX + "addConfigDialog.title";

	private static final String DIALOG_MESSAGE = PREFIX
			+ "addConfigDialog.message";

	private static final int TABLE_WIDTH = 250;

	private ProjectNode project;

	private IAdaptable element;

	private Text extensionsText;

	private Table configsTable;

	private TableViewer configsViewer;

	private Label errorLabel;

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

	public ConfigFilesTab(ProjectNode project, IAdaptable element) {
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

		// extension text field
		extensionsText = SpringUIUtils.createTextField(composite, BeansUIPlugin
				.getResourceString(EXTENSIONS_LABEL));
		extensionsText.setText(StringUtils.collectionToDelimitedString(project
				.getConfigExtensions(), ","));
		extensionsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleExtensionsTextModified();
			}
		});

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
		configsViewer
				.setContentProvider(new ConfigFilesContentProvider(project));
		configsViewer.setLabelProvider(new ModelLabelProvider());
		configsViewer.setInput(this); // activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());

		// error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent
				.getDisplay()));
		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(ADD_BUTTON), buttonListener);
		removeButton = SpringUIUtils.createButton(buttonArea, BeansUIPlugin
				.getResourceString(REMOVE_BUTTON), buttonListener, 0, false);
		handleExtensionsTextModified();
		hasUserMadeChanges = false; // handleExtensionTextModified() has set
									// this to true
		return composite;
	}

	/**
	 * The user has modified the comma-separated list of config extensions.
	 * Validate the input and update the "Add" button enablement and error label
	 * accordingly .
	 */
	private void handleExtensionsTextModified() {
		String errorMessage = null;
		List extensions = new ArrayList();
		String extText = extensionsText.getText().trim();
		if (extText.length() == 0) {
			errorMessage = BeansUIPlugin.getResourceString(ERROR_NO_EXTENSIONS);
		} else {
			StringTokenizer tokenizer = new StringTokenizer(extText, ",");
			while (tokenizer.hasMoreTokens()) {
				String extension = tokenizer.nextToken().trim();
				if (isValidExtension(extension)) {
					extensions.add(extension);
				} else {
					errorMessage = BeansUIPlugin
							.getResourceString(ERROR_INVALID_EXTENSIONS);
					break;
				}
			}
			if (errorMessage == null) {
				project.setConfigExtensions(extensions);
				hasUserMadeChanges = true;
			}
		}
		if (errorMessage != null) {
			errorLabel.setText(errorMessage);
			addButton.setEnabled(false);
		} else {
			errorLabel.setText("");
			addButton.setEnabled(true);
		}
		errorLabel.getParent().update();
	}

	private boolean isValidExtension(String extension) {
		if (extension.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < extension.length(); i++) {
				char c = extension.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					return false;
				}
			}
		}
		return true;
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
		SelectionStatusDialog dialog;
		if (SpringCoreUtils.isEclipseSameOrNewer(3, 2)) {
			FilteredElementTreeSelectionDialog selDialog =
				new FilteredElementTreeSelectionDialog(
					SpringUIUtils.getStandardDisplay().getActiveShell(),
					new JavaElementLabelProvider(),
					new NonJavaResourceContentProvider());
			selDialog.addFilter(new JavaFileExtensionFilter(project
					.getConfigExtensions()));
			selDialog.setValidator(new StorageSelectionValidator(true));
			selDialog.setInput(element);
			selDialog.setSorter(new JavaElementSorter());
			dialog = selDialog;
		} else {
			ElementTreeSelectionDialog selDialog =
				new ElementTreeSelectionDialog(
					SpringUIUtils.getStandardDisplay().getActiveShell(),
					new JavaElementLabelProvider(),
					new NonJavaResourceContentProvider());
			selDialog.addFilter(new JavaFileExtensionFilter(project
					.getConfigExtensions()));
			selDialog.setValidator(new StorageSelectionValidator(true));
			selDialog.setInput(element);
			selDialog.setSorter(new JavaElementSorter());
			dialog = selDialog;
		}
		dialog.setTitle(BeansUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(BeansUIPlugin.getResourceString(DIALOG_MESSAGE));
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (int i = 0; i < selection.length; i++) {
					String config;
					if (selection[i] instanceof ZipEntryStorage) {
						ZipEntryStorage storage = (ZipEntryStorage)
								selection[i];
						config = storage.getZipResource()
								.getProjectRelativePath()
								+ ZipEntryStorage.DELIMITER
								+ storage.getFullPath();
					} else {
						IFile file = (IFile) selection[i];
						config = file.getProjectRelativePath().toString();
					}
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
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
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

	private class ConfigFilesContentProvider implements
			IStructuredContentProvider {
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
			return (((ConfigNode) element).getName().indexOf('/') == -1 ? ROOT_DIR
					: SUB_DIR);
		}
	}
}
