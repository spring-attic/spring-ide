/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.viewers.JavaFileSuffixFilter;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;
import org.springframework.ide.eclipse.webflow.ui.navigator.WebflowNavigatorLabelProvider;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class WebflowConfigTab {

	private static final int TABLE_WIDTH = 250;

	private static final String DESCRIPTION = "ConfigurationPropertyPage.tabConfigFiles.description";

	private static final String ADD_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.addButton";

	private static final String REMOVE_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.removeButton";

	private static final String DIALOG_TITLE = "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.title";

	private static final String DIALOG_MESSAGE = "ConfigurationPropertyPage.tabConfigFiles.addConfigDialog.message";

	private static final String EDIT_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.editButton";

	private static final String SCAN_BUTTON = "ConfigurationPropertyPage.tabConfigFiles.scanButton";

	private static final String SCAN_NOTE_LABEL = "ConfigurationPropertyPage.tabConfigFiles.scan.note.label";

	private IAdaptable element;

	private Table configsTable;

	private TableViewer configsViewer;

	private Button addButton, removeButton, scanButton;

	private SelectionListener buttonListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private boolean hasUserMadeChanges;

	private IWebflowProject project;

	private Set<IWebflowConfig> configFiles;

	private Map<IWebflowConfig, Set<IModelElement>> configFilesToBeansConfigs;

	private Map<IWebflowConfig, String> configFilesToNames;

	private IModelElement selectedElement;

	private Button editButton;

	public WebflowConfigTab(IWebflowProject project, IAdaptable element,
			IModelElement selectedModelElement) {
		this.project = project;
		this.element = element;
		this.configFiles = new LinkedHashSet<IWebflowConfig>();
		this.configFilesToBeansConfigs = new HashMap<IWebflowConfig, Set<IModelElement>>();
		this.configFilesToNames = new HashMap<IWebflowConfig, String>();

		if (project.getConfigs() != null) {
			for (IWebflowConfig config : project.getConfigs()) {
				this.configFiles.add(config);
				this.configFilesToBeansConfigs.put(config,
						config.getBeansConfigs());
				this.configFilesToNames.put(config, config.getName());
			}
		}

		if (selectedModelElement != null) {
			for (IWebflowConfig config : configFiles) {
				if (config.getElementName().equals(
						selectedModelElement.getElementName())) {
					this.selectedElement = config;
				}
			}
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
		configsViewer.setLabelProvider(new WebflowNavigatorLabelProvider());
		configsViewer.setInput(this.configFiles); // activate content provider
		configsViewer.setSorter(new ConfigFilesSorter());
		configsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		if (this.selectedElement != null) {
			configsViewer.setSelection(
					new StructuredSelection(selectedElement), true);
		}

		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = SpringUIUtils.createButton(buttonArea,
				Activator.getResourceString(ADD_BUTTON), buttonListener, 0,
				true);
		editButton = SpringUIUtils.createButton(buttonArea,
				Activator.getResourceString(EDIT_BUTTON), buttonListener, 0,
				false);
		removeButton = SpringUIUtils.createButton(buttonArea,
				Activator.getResourceString(REMOVE_BUTTON), buttonListener, 0,
				false);
		scanButton = SpringUIUtils.createButton(buttonArea,
				Activator.getResourceString(SCAN_BUTTON), buttonListener, 0,
				true);

		handleTableSelectionChanged();

		return composite;
	}

	private void handleDoubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof IWebflowConfig) {
				// Edit corresponding config
				handleEditButtonPressed();
			}
		}
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
		} else {
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
		} else if (button == removeButton) {
			handleRemoveButtonPressed();
		} else if (button == editButton) {
			handleEditButtonPressed();
		} else if (button == scanButton) {
			handleScanButtonPressed();
		}
		handleTableSelectionChanged();
		configsTable.setFocus();
	}

	private void handleEditButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (!selection.isEmpty()) {
			IWebflowConfig file = (IWebflowConfig) selection.getFirstElement();
			Set<IModelElement> configs = new HashSet<IModelElement>();
			String name = file.getName();
			List<String> names = new ArrayList<String>();
			names.add(name);
			if (this.configFilesToBeansConfigs.containsKey(file)) {
				Set<IModelElement> oldConfigs = this.configFilesToBeansConfigs
						.get(file);
				configs.addAll(oldConfigs);
			}
			WebflowConfigDialog dialog = new WebflowConfigDialog(SpringUIUtils
					.getStandardDisplay().getActiveShell(),
					project.getProject(), configs, names, file.getResource());
			if (dialog.open() == Dialog.OK) {
				this.configFilesToBeansConfigs.put(file, configs);
				this.configFilesToNames.put(file, names.get(0));
				hasUserMadeChanges = true;
			}
		}
		this.configsViewer.refresh();
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
		dialog.addFilter(new ConfigFileFilter(new String[] { "xml" }));
		dialog.setValidator(new FileSelectionValidator(true));
		dialog.setInput(element);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (int i = 0; i < selection.length; i++) {
					IFile file = (IFile) selection[i];
					IWebflowConfig config = new WebflowConfig(project);
					config.setResource(file);
					int j = file.getName().lastIndexOf('.');
					if (j > 0) {
						config.setName(file.getName().substring(0, j));
					} else {
						config.setName(file.getName());
					}
					configFiles.add(config);
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
				configFiles.remove((IWebflowConfig) elements.next());
			}
			configsViewer.refresh();
			hasUserMadeChanges = true;
		}
	}

	private void handleScanButtonPressed() {
		ScannedFilesContentProvider contentProvider = new ScannedFilesContentProvider(
				"xml");
		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
				SpringUIUtils.getStandardDisplay().getActiveShell(),
				new ScannedFilesLabelProvider(), contentProvider) {

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super
						.createDialogArea(parent);
				Label note = new Label(composite, SWT.WRAP);
				note.setText(Activator.getResourceString(SCAN_NOTE_LABEL));
				note.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				return composite;
			}
		};
		dialog.setTitle(Activator.getResourceString(DIALOG_TITLE));
		dialog.setMessage(Activator.getResourceString(DIALOG_MESSAGE));
		dialog.addFilter(new ConfigFileFilter(new String[] { "xml" }));
		dialog.setValidator(new FileSelectionValidator(true));
		dialog.setInput(element);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				for (int i = 0; i < selection.length; i++) {
					IFile file = (IFile) selection[i];
					IWebflowConfig config = new WebflowConfig(project);
					config.setResource(file);
					int j = file.getName().lastIndexOf('.');
					if (j > 0) {
						config.setName(file.getName().substring(0, j));
					} else {
						config.setName(file.getName());
					}
					configFiles.add(config);
				}
				hasUserMadeChanges = true;
				configsViewer.refresh();
			}
		}
	}

	private class ConfigFilesContentProvider implements
			IStructuredContentProvider {

		private Set<IWebflowConfig> files;

		public ConfigFilesContentProvider(Set<IWebflowConfig> files) {
			this.files = files;
		}

		public Object[] getElements(Object obj) {
			return files.toArray();
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
			return (((IWebflowConfig) element).getResource().getName()
					.indexOf('/') == -1 ? ROOT_DIR : SUB_DIR);
		}
	}

	public Map<IWebflowConfig, Set<IModelElement>> getConfigFilesToBeansConfigs() {
		return configFilesToBeansConfigs;
	}

	public void setConfigFiles(Set<IWebflowConfig> configFiles) {
		this.configFiles = configFiles;
	}

	public Set<IWebflowConfig> getConfigFiles() {
		return configFiles;
	}

	public Map<IWebflowConfig, String> getConfigFilesToNames() {
		return this.configFilesToNames;
	}

	private static class ConfigFileFilter extends JavaFileSuffixFilter {

		public ConfigFileFilter(String[] allowedFileExtensions) {
			super(allowedFileExtensions);
		}

		@Override
		protected boolean selectFile(IFile element) {
			return !WebflowModelUtils.isWebflowConfig(element);
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

	private final class ScannedFilesContentProvider implements
			ITreeContentProvider {

		private Object[] scannedFiles = null;

		public ScannedFilesContentProvider(final String fileSuffixes) {
			final Set<IFile> files = new LinkedHashSet<IFile>();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					// TODO Auto-generated method stub

				}
			};

			try {
				IRunnableContext context = new ProgressMonitorDialog(
						SpringUIUtils.getStandardDisplay().getActiveShell());
				context.run(true, true, runnable);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
			scannedFiles = files.toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		public Object[] getElements(Object inputElement) {
			return scannedFiles;
		}

		public Object[] getChildren(Object parentElement) {
			return IWebflowModelElement.NO_CHILDREN;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

	}

}
