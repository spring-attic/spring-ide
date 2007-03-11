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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.dialogs.FilteredElementTreeSelectionDialog;
import org.springframework.ide.eclipse.ui.dialogs.StorageSelectionValidator;
import org.springframework.ide.eclipse.ui.viewers.JavaFileExtensionFilter;

/**
 * Property page tab for defining the list of beans config file extensions and
 * the selected beans config files.
 * 
 * @author Torsten Juergeleit
 */
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

	private PropertiesModel model;
	private PropertiesProject project;

	private Text extensionsText;
	private Table configsTable;
	private TableViewer configsViewer;
	private Label errorLabel;
	private Button addButton, removeButton;

	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private IModelChangeListener modelChangeListener =
			new IModelChangeListener() {
		public void elementChanged(ModelChangeEvent event) {
			if (configsViewer != null
					&& !configsViewer.getControl().isDisposed()) {
				configsViewer.refresh();
			}
		}
	};

	private boolean hasUserMadeChanges;

	public ConfigFilesTab(PropertiesModel model, IProject project) {
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

		// Create extension text field
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

		// Create table and viewer for Spring bean config files
		configsTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = TABLE_WIDTH;
		configsTable.setLayoutData(data);
		configsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		configsViewer = new TableViewer(configsTable);
		configsViewer
				.setContentProvider(new ConfigFilesContentProvider(project));
		configsViewer.setLabelProvider(new PropertiesModelLabelProvider());
		configsViewer.setInput(this); // activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());

		// Create error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent
				.getDisplay()));
		// Create button area
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
		model.addChangeListener(modelChangeListener);
		handleExtensionsTextModified();
		hasUserMadeChanges = false; // handleExtensionTextModified() has set
									// this to true
		return composite;
	}

	public void dispose() {
		model.removeChangeListener(modelChangeListener);
	}

	/**
	 * The user has modified the comma-separated list of config extensions.
	 * Validate the input and update the "Add" button enablement and error label
	 * accordingly.
	 */
	private void handleExtensionsTextModified() {
		String errorMessage = null;
		Set<String> extensions = new LinkedHashSet<String>();
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
			selDialog.setInput(project.getProject());
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
			selDialog.setInput(project.getProject());
			selDialog.setSorter(new JavaElementSorter());
			dialog = selDialog;
		}
		dialog.setTitle(BeansUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(BeansUIPlugin.getResourceString(DIALOG_MESSAGE));
		if (dialog.open() == Window.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (Object element : selection) {
					String config;
					if (element instanceof ZipEntryStorage) {
						ZipEntryStorage storage = (ZipEntryStorage) element;
						config = storage.getFullName();
					} else {
						IFile file = (IFile) element;
						config = file.getProjectRelativePath().toString();
					}
					project.addConfig(config);
				}
				configsViewer.refresh(false);
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
				IBeansConfig config = (IBeansConfig) elements.next();
				project.removeConfig(config.getElementName());
			}
			configsViewer.refresh(false);
			hasUserMadeChanges = true;
		}
	}

	private static class ConfigFilesContentProvider implements
			IStructuredContentProvider {

		private IBeansProject project;

		public ConfigFilesContentProvider(IBeansProject project) {
			this.project = project;
		}

		public Object[] getElements(Object obj) {
			return project.getConfigs().toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		public void dispose() {
		}
	}

	private static class ConfigFilesSorter extends ViewerSorter {

		private enum Category {
			SUB_DIR, ROOT_DIR, OTHER
		};

		@Override
		public int category(Object element) {
			if (element instanceof IBeansConfig) {
				if (((IBeansConfig) element).getElementName()
						.indexOf('/') == -1) {
					return Category.ROOT_DIR.ordinal();
				}
				return Category.SUB_DIR.ordinal();
			}
			return Category.OTHER.ordinal();
		}
	}
}
