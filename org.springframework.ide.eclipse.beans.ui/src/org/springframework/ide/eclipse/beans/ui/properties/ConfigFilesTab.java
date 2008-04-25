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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.beans.core.model.locate.ProjectScanningBeansConfigLocator;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.dialogs.FilteredElementTreeSelectionDialog;
import org.springframework.ide.eclipse.ui.dialogs.StorageSelectionValidator;
import org.springframework.ide.eclipse.ui.viewers.JavaFileSuffixFilter;

/**
 * Property page tab for defining the list of beans config file extensions and the selected beans
 * config files.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("deprecation")
public class ConfigFilesTab {

	private static final String PREFIX = "ConfigurationPropertyPage." + "tabConfigFiles.";

	private static final String DESCRIPTION = PREFIX + "description";

	private static final String SUFFIXES_LABEL = PREFIX + "suffixes.label";

	private static final String ERROR_NO_SUFFIXES = PREFIX + "error.noSuffixes";

	private static final String ERROR_INVALID_SUFFIXES = PREFIX + "error.invalidSuffixes";

	private static final String ADD_BUTTON = PREFIX + "addButton";

	private static final String REMOVE_BUTTON = PREFIX + "removeButton";

	private static final String DIALOG_TITLE = PREFIX + "addConfigDialog.title";

	private static final String DIALOG_MESSAGE = PREFIX + "addConfigDialog.message";

	private static final int TABLE_WIDTH = 250;

	private static final String ENABLE_IMPORT_LABEL = PREFIX + "enableImports.label";

	private static final String NOTE_LABEL = PREFIX + "note.label";

	private PropertiesModel model;

	private PropertiesProject project;

	private Text suffixesText;

	private Table configsTable;

	private TableViewer configsViewer;

	private Label errorLabel;

	private Button addButton, removeButton, scanButton;

	private Button enableImportText;

	private IModelElement selectedElement;

	private Color grayColor = new Color(Display.getDefault(), 150, 150, 150);

	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private IModelChangeListener modelChangeListener = new IModelChangeListener() {
		public void elementChanged(ModelChangeEvent event) {
			if (configsViewer != null && !configsViewer.getControl().isDisposed()) {
				configsViewer.refresh();
			}
		}
	};

	private boolean hasUserMadeChanges;

	public ConfigFilesTab(PropertiesModel model, PropertiesProject project,
			IModelElement selectedModelElement) {
		this.model = model;
		this.project = project;
		calculateSelectedElement(selectedModelElement);
	}

	private void calculateSelectedElement(IModelElement modelElement) {
		if (modelElement != null && this.project != null) {
			this.selectedElement = this.project.getConfig(modelElement.getElementName());
		}
	}

	public boolean hasUserMadeChanges() {
		return hasUserMadeChanges;
	}

	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 3;
		layout.marginWidth = 3;
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

		// Create table and viewer for Spring bean config files
		configsTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
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
		configsViewer.setContentProvider(new ConfigFilesContentProvider(project));
		configsViewer.setLabelProvider(new PropertiesModelLabelProvider());
		configsViewer.setInput(this); // activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());

		if (this.selectedElement != null) {
			configsViewer.setSelection(new StructuredSelection(selectedElement), true);
		}

		if (BeansConfigLocatorFactory.hasEnabledBeansConfigLocatorDefinitions(project.getProject())) {
			Label note = new Label(composite, SWT.WRAP);
			note.setText(BeansUIPlugin.getResourceString(NOTE_LABEL));
			note.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		Label options = new Label(composite, SWT.WRAP);
		options.setText("Options:");
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create enable import checkbox
		enableImportText = SpringUIUtils.createCheckBox(composite, BeansUIPlugin
				.getResourceString(ENABLE_IMPORT_LABEL));
		enableImportText.setSelection(project.isImportsEnabled());
		enableImportText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleImportEnabledChanged();
			}
		});

		// Create suffix text field
		suffixesText = SpringUIUtils.createTextField(composite, BeansUIPlugin
				.getResourceString(SUFFIXES_LABEL));
		suffixesText.setText(StringUtils.collectionToDelimitedString(project.getConfigSuffixes(),
				","));
		suffixesText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleSuffixesTextModified();
			}
		});

		// Create error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent.getDisplay()));
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
		scanButton = SpringUIUtils.createButton(buttonArea, "Scan...", buttonListener);
		model.addChangeListener(modelChangeListener);
		handleSuffixesTextModified();
		hasUserMadeChanges = false;
		// handleSuffixTextModified() has set this to true

		handleTableSelectionChanged();
		return composite;
	}

	private void handleImportEnabledChanged() {
		if (project.isImportsEnabled() && !enableImportText.getSelection()) {
			hasUserMadeChanges = true;
			project.setImportsEnabled(enableImportText.getSelection());
		}
		else if (!project.isImportsEnabled() && enableImportText.getSelection()) {
			hasUserMadeChanges = true;
			project.setImportsEnabled(enableImportText.getSelection());
		}
	}

	public void dispose() {
		model.removeChangeListener(modelChangeListener);
	}

	/**
	 * The user has modified the comma-separated list of config suffixes. Validate the input and
	 * update the "Add" button enablement and error label accordingly.
	 */
	private void handleSuffixesTextModified() {
		String errorMessage = null;
		Set<String> suffixes = new LinkedHashSet<String>();
		String extText = suffixesText.getText().trim();
		if (extText.length() == 0) {
			errorMessage = BeansUIPlugin.getResourceString(ERROR_NO_SUFFIXES);
		}
		else {
			StringTokenizer tokenizer = new StringTokenizer(extText, ",");
			while (tokenizer.hasMoreTokens()) {
				String suffix = tokenizer.nextToken().trim();
				if (isValidSuffix(suffix)) {
					suffixes.add(suffix);
				}
				else {
					errorMessage = BeansUIPlugin.getResourceString(ERROR_INVALID_SUFFIXES);
					break;
				}
			}
			if (errorMessage == null) {
				project.setConfigSuffixes(suffixes);
				hasUserMadeChanges = true;
			}
		}
		if (errorMessage != null) {
			errorLabel.setText(errorMessage);
			addButton.setEnabled(false);
		}
		else {
			errorLabel.setText("");
			addButton.setEnabled(true);
		}
		errorLabel.getParent().update();
	}

	private boolean isValidSuffix(String suffix) {
		if (suffix.length() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * The user has selected a different configuration in table. Update button enablement.
	 */
	private void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer.getSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
		}
		else {
			if (selection.getFirstElement() instanceof IBeansConfig
					&& ((IBeansConfig) selection.getFirstElement()).getType() == IBeansConfig.Type.MANUAL) {
				removeButton.setEnabled(true);
			}
			else {
				removeButton.setEnabled(false);
			}
		}
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 */
	private void handleButtonPressed(Button button) {
		if (button == addButton) {
			handleAddButtonPressed();
		}
		else if (button == removeButton) {
			handleRemoveButtonPressed();
		}
		else if (button == scanButton) {
			handleScanButtonPressed();
		}
		handleTableSelectionChanged();
		configsTable.setFocus();
	}

	protected void handleScanButtonPressed() {
		ScannedFilesContentProvider contentProvider = new ScannedFilesContentProvider();
		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(SpringUIUtils
				.getStandardDisplay().getActiveShell(), new ScannedFilesLabelProvider(),
				contentProvider);
		dialog.setTitle(BeansUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(BeansUIPlugin.getResourceString(DIALOG_MESSAGE));
		dialog.addFilter(new ConfigFileFilter(project.getConfigSuffixes()));
		dialog.setValidator(new StorageSelectionValidator(true));
		dialog.setInput(project.getProject());
		dialog.setSorter(new JavaElementSorter());
		dialog.setInitialSelections(contentProvider.getElements(project.getProject()));
		
		if (dialog.open() == Window.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (Object element : selection) {
					String config;
					if (element instanceof ZipEntryStorage) {
						ZipEntryStorage storage = (ZipEntryStorage) element;
						config = storage.getFullName();
					}
					else {
						IFile file = (IFile) element;
						config = file.getProjectRelativePath().toString();
					}
					project.addConfig(config, IBeansConfig.Type.MANUAL);
				}
				configsViewer.refresh(false);
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the add button. Opens the configuration selection dialog and adds the
	 * selected configuration.
	 */
	private void handleAddButtonPressed() {
		SelectionStatusDialog dialog;
		if (SpringCoreUtils.isEclipseSameOrNewer(3, 2)) {
			FilteredElementTreeSelectionDialog selDialog = new FilteredElementTreeSelectionDialog(
					SpringUIUtils.getStandardDisplay().getActiveShell(), new LabelProvider(),
					new NonJavaResourceContentProvider());
			selDialog.addFilter(new ConfigFileFilter(project.getConfigSuffixes()));
			selDialog.setValidator(new StorageSelectionValidator(true));
			selDialog.setInput(project.getProject());
			selDialog.setSorter(new JavaElementSorter());
			dialog = selDialog;
		}
		else {
			ElementTreeSelectionDialog selDialog = new ElementTreeSelectionDialog(SpringUIUtils
					.getStandardDisplay().getActiveShell(), new LabelProvider(),
					new NonJavaResourceContentProvider());
			selDialog.addFilter(new ConfigFileFilter(project.getConfigSuffixes()));
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
					}
					else {
						IFile file = (IFile) element;
						config = file.getProjectRelativePath().toString();
					}
					project.addConfig(config, IBeansConfig.Type.MANUAL);
				}
				configsViewer.refresh(false);
				hasUserMadeChanges = true;
			}
		}
	}

	/**
	 * The user has pressed the remove button. Delete the selected configuration.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer.getSelection();
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

	private final class ScannedFilesContentProvider implements ITreeContentProvider {
		
		private Object[] scannedFiles = null;
		
		public ScannedFilesContentProvider() {
			final Set<IFile> files = new LinkedHashSet<IFile>();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					ProjectScanningBeansConfigLocator locator = new ProjectScanningBeansConfigLocator();
					files.addAll(locator.locateBeansConfigs(project.getProject(), monitor));
				}
			};
			
			try {
				IRunnableContext context = new ProgressMonitorDialog(SpringUIUtils
						.getStandardDisplay().getActiveShell());
				context.run(true, true, runnable);
			}
			catch (InvocationTargetException e) {
			}
			catch (InterruptedException e) {
			}
			scannedFiles = files.toArray();
			
		}
		
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return IModelElement.NO_CHILDREN;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return scannedFiles;
		}
	}

	private class ConfigFilesContentProvider implements IStructuredContentProvider {

		private IBeansProject project;

		public ConfigFilesContentProvider(IBeansProject project) {
			this.project = project;
		}

		public Object[] getElements(Object obj) {
			return new LinkedHashSet<IBeansConfig>(project.getConfigs()).toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
			grayColor.dispose();
		}
	}

	private static class ConfigFilesSorter extends ViewerSorter {

		private enum Category {
			SUB_DIR, ROOT_DIR, OTHER
		};

		@Override
		public int category(Object element) {
			if (element instanceof IBeansConfig) {
				if (((IBeansConfig) element).getElementName().indexOf('/') == -1) {
					return Category.ROOT_DIR.ordinal();
				}
				return Category.SUB_DIR.ordinal();
			}
			return Category.OTHER.ordinal();
		}
	}

	private static class ConfigFileFilter extends JavaFileSuffixFilter {

		public ConfigFileFilter(Set<String> allowedFileExtensions) {
			super(allowedFileExtensions);
		}

		@Override
		protected boolean selectFile(IFile element) {
			IBeansProject project = BeansCorePlugin.getModel().getProject(element.getProject());
			if (project != null) {
				IBeansConfig beansConfig = project.getConfig(element);
				return beansConfig == null;
			}
			return false;
		}
	}

	private class LabelProvider extends JavaElementLabelProvider implements IColorProvider {

		@Override
		public String getText(Object element) {
			String label = super.getText(element);
			if (element instanceof IFile) {
				IBeansConfig bc = BeansCorePlugin.getModel().getConfig((IFile) element, true);
				if (bc instanceof IImportedBeansConfig) {
					label += " [imported]";
				}
				if (bc != null && bc.getType() == IBeansConfig.Type.AUTO_DETECTED) {
					label += " [auto detected]";
				}
			}
			return label;
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			if (element instanceof IBeansConfig
					&& ((IBeansConfig) element).getType() == IBeansConfig.Type.AUTO_DETECTED) {
				return grayColor;
			}
			return null;
		}
	}

	private class ScannedFilesLabelProvider extends JavaElementLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof IFile) {
				return ((IFile) element).getProjectRelativePath().toString();
			}
			return super.getText(element);
		}

	}

}
