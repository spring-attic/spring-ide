/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlSourceViewerConfiguration;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.yaml.snakeyaml.Yaml;

/**
 * Cloud Foundry Application deployment properties dialog. Allows user to select
 * manifest YAML file or enter deployment manifest YAML manually.
 *
 * @author Alex Boyko
 *
 */
public class DeploymentPropertiesDialog extends TitleAreaDialog {

	final static private String DIALOG_LIST_HEIGHT_SETTING = "ManifestFileDialog.listHeight"; //$NON-NLS-1$
	final static private String NO_MANIFEST_SELECETED_LABEL = "Deployment manifest file not selected"; //$NON-NLS-1$
	final static private String YML_EXTENSION = "yml"; //$NON-NLS-1$
	final static private String[] FILE_FILTER_NAMES = new String[] {"Manifest YAML files - *manifest*.yml", "YAML files - *.yml", "All files - *.*"};

	private static abstract class DeepFileFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			if (element instanceof IResource && !((IResource)element).isDerived()) {
				if (element instanceof IFile) {
					return acceptFile((IFile)element);
				}
				if (element instanceof IContainer) {
					try {
						IContainer container = (IContainer) element;
						for (IResource resource : container.members()) {
							boolean select = select(viewer, container, resource);
							if (select) {
								return true;
							}
						}
					} catch (CoreException e) {
						// ignore
					}
				}
			}
			return false;
		}

		abstract protected boolean acceptFile(IFile file);

	}

	final static private ViewerFilter YAML_FILE_FILTER = new DeepFileFilter() {
		@Override
		protected boolean acceptFile(IFile file) {
			return YML_EXTENSION.equals(file.getFileExtension());
		}
	};
	final static private ViewerFilter MANIFEST_YAML_FILE_FILTER = new DeepFileFilter() {
		@Override
		protected boolean acceptFile(IFile file) {
			return file.getName().toLowerCase().contains("manifest") && YML_EXTENSION.equals(file.getFileExtension());
		}
	};
	final static private ViewerFilter ALL_FILES = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			return (element instanceof IResource) && !((IResource)element).isDerived();
		}
	};
	final static private ViewerFilter[][] RESOURCE_FILTERS = new ViewerFilter[][] {
		{MANIFEST_YAML_FILE_FILTER},
		{YAML_FILE_FILTER},
		{ALL_FILES}
	};

	final static private int DEFAULT_WORKSPACE_GROUP_HEIGHT = 200;

	private IProject project;
	private boolean readOnly;
	private boolean noModeSwitch;
	private List<CloudDomain> domains;
	private CloudApplicationDeploymentProperties deploymentProperties;
	private String defaultYaml;
	private LiveVariable<IFile> fileModel;
	private LiveVariable<Boolean> manifestTypeModel;
	private LiveVariable<String> fileYamlContents;

	private Label fileLabel;
	private Sash resizeSash;
	private SourceViewer fileYamlViewer;
	private SourceViewer manualYamlViewer;
	private TreeViewer workspaceViewer;
	private Button refreshButton;
	private Button buttonFileManifest;
	private Button buttonManualManifest;
	protected Group fileGroup;
	private Group yamlGroup;
	private Composite fileYamlComposite;
	private Composite manualYamlComposite;
	private Combo fileFilterCombo;
	private IHandlerService service;
	private List<IHandlerActivation> activations;
	private EditorActionHandler[] handlers = new EditorActionHandler[] {
			new EditorActionHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, SourceViewer.CONTENTASSIST_PROPOSALS),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_UNDO, SourceViewer.UNDO),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_REDO, SourceViewer.REDO),
	};


	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent e) {
			IResource resource = e.getSelection().isEmpty() ? null : (IResource) ((IStructuredSelection) e.getSelection()).toArray()[0];
			fileModel.setValue(resource instanceof IFile ? (IFile) resource : null);
			refreshButton.setEnabled(manifestTypeModel.getValue() && !workspaceViewer.getSelection().isEmpty());
		}
	};

	public DeploymentPropertiesDialog(Shell parentShell, List<CloudDomain> domains, IProject project, IFile manifest, String defaultYaml, boolean readOnly, boolean noModeSwitch) {
		super(parentShell);
		this.domains = domains;
		this.project = project;
		this.defaultYaml = defaultYaml;
		this.readOnly = readOnly;
		this.noModeSwitch = noModeSwitch;
		this.service = (IHandlerService) PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		this.activations = new ArrayList<IHandlerActivation>(handlers.length);
		manifestTypeModel = new LiveVariable<>();
		manifestTypeModel.setValue(manifest != null);
		fileModel = new LiveVariable<>();
		fileModel.setValue(manifest);
		fileYamlContents = new LiveVariable<>();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    setTitle("Select Deployment Manifest");
		Composite container = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(container, parent.getStyle());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(new GridLayout());

		createModeSwitchGroup(composite);

		createFileGroup(composite);

		createResizeSash(composite);

		createYamlContentsGroup(composite);

		activateHanlders();

		fileModel.addListener(new ValueListener<IFile>() {
			@Override
			public void gotValue(LiveExpression<IFile> exp, final IFile value) {
				updateManifestFile();
				validate();
			}
		});

		manifestTypeModel.addListener(new ValueListener<Boolean>() {
			@Override
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				GridData gridData;
				buttonFileManifest.setSelection(value);
				buttonManualManifest.setSelection(!value);

				refreshButton.setEnabled(value && !workspaceViewer.getSelection().isEmpty());
				workspaceViewer.getControl().setEnabled(value);
				fileLabel.setEnabled(value);

//				if (!readOnly) {
					gridData = GridDataFactory.copyData((GridData) fileGroup.getLayoutData());
					gridData.exclude = !value;
					fileGroup.setVisible(value);
					fileGroup.setLayoutData(gridData);
					gridData = GridDataFactory.copyData((GridData) resizeSash.getLayoutData());
					gridData.exclude = !value;
					resizeSash.setVisible(value);
					resizeSash.setLayoutData(gridData);
					fileGroup.getParent().layout();
//				}

				fileYamlComposite.setVisible(value);
				gridData = GridDataFactory.copyData((GridData) fileYamlComposite.getLayoutData());
				gridData.exclude = !value;
				fileYamlComposite.setLayoutData(gridData);
				manualYamlComposite.setVisible(!value);
				gridData = GridDataFactory.copyData((GridData) manualYamlComposite.getLayoutData());
				gridData.exclude = value;
				manualYamlComposite.setLayoutData(gridData);
				yamlGroup.layout();
				yamlGroup.getParent().layout();

				validate();
			}
		});

		fileYamlContents.addListener(new ValueListener<String>() {
			@Override
			public void gotValue(LiveExpression<String> exp, String value) {
				if (value == null) {
					fileYamlViewer.setDocument(new Document(""));
				} else {
					fileYamlViewer.setDocument(new Document(value));
				}
				validate();
			}
		});

		return container;
	}

	private void createModeSwitchGroup(Composite composite) {
		Group typeGroup = new Group(composite, SWT.NONE);
		typeGroup.setVisible(!noModeSwitch);
		typeGroup.setText("Manifest Type");
		typeGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).exclude(noModeSwitch).create());
		typeGroup.setLayout(new GridLayout(2, true));

		buttonFileManifest = new Button(typeGroup, SWT.RADIO);
		buttonFileManifest.setText("File");
		buttonFileManifest.setSelection(manifestTypeModel.getValue());
		buttonFileManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manifestTypeModel.setValue(buttonFileManifest.getSelection());;
			}
		});
		buttonFileManifest.setLayoutData(GridDataFactory.fillDefaults().create());

		buttonManualManifest = new Button(typeGroup, SWT.RADIO);
		buttonManualManifest.setText("Manual");
		buttonManualManifest.setSelection(!manifestTypeModel.getValue());
		buttonManualManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manifestTypeModel.setValue(!buttonManualManifest.getSelection());;
			}
		});
		buttonManualManifest.setLayoutData(GridDataFactory.fillDefaults().create());
	}

	private void createFileGroup(Composite composite) {
		fileGroup = new Group(composite, SWT.NONE);
		fileGroup.setText("Workspace File");
		fileGroup.setLayout(new GridLayout(2, false));
		int height = DEFAULT_WORKSPACE_GROUP_HEIGHT;
		try {
			height = getDialogBoundsSettings().getInt(DIALOG_LIST_HEIGHT_SETTING);
		} catch (NumberFormatException e) {
			// ignore exception
		}
		fileGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, height).create());

		fileLabel = new Label(fileGroup, SWT.NONE);
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).span(2, SWT.DEFAULT).create());

		workspaceViewer = new TreeViewer(fileGroup);
		workspaceViewer.setContentProvider(new BaseWorkbenchContentProvider());
		workspaceViewer.setLabelProvider(new WorkbenchLabelProvider());
		workspaceViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		workspaceViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		if (fileModel.getValue() == null) {
			workspaceViewer.reveal(project.getFile("manifest.yml"));
		} else {
			workspaceViewer.reveal(fileModel.getValue());
			workspaceViewer.setSelection(new StructuredSelection(new IResource[] { fileModel.getValue() }), false);
		}
		workspaceViewer.addSelectionChangedListener(selectionListener);

		Composite fileButtonsComposite = new Composite(fileGroup, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		fileButtonsComposite.setLayout(layout);
		fileButtonsComposite.setLayoutData(GridDataFactory.fillDefaults().create());

		refreshButton = new Button(fileButtonsComposite, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.setEnabled(false);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshManifests();
			}
		});
		refreshButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		fileFilterCombo = new Combo(fileGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		fileFilterCombo.setItems(FILE_FILTER_NAMES);
		int selectionIndex = 0;
		IFile manifestFile = fileModel.getValue();
		if (manifestFile != null) {
			selectionIndex = RESOURCE_FILTERS.length - 1;
			for (int i = 0; i < RESOURCE_FILTERS.length; i++) {
				boolean accept = true;
				for (ViewerFilter filter : RESOURCE_FILTERS[i]) {
					accept = filter.select(null, null, fileModel.getValue());
					if (!accept) {
						break;
					}
				}
				if (accept) {
					selectionIndex = i;
					break;
				}
			}
		}
		workspaceViewer.setFilters(RESOURCE_FILTERS[selectionIndex]);
		fileFilterCombo.select(selectionIndex);
		fileFilterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove selection listener to set selection from current pathModel value
				workspaceViewer.removeSelectionChangedListener(selectionListener);
				workspaceViewer.setFilters(RESOURCE_FILTERS[fileFilterCombo.getSelectionIndex()]);
				// Add the selection listener back after the initial value has been set
				workspaceViewer.addSelectionChangedListener(selectionListener);
			}
		});
	}

	private void createResizeSash(Composite composite) {
		resizeSash = new Sash(composite, SWT.HORIZONTAL);
		resizeSash.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 4).grab(true, false).create());
		resizeSash.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData listLayoutData = (GridData) fileGroup.getLayoutData();
				int newHeight = listLayoutData.heightHint + e.y - resizeSash.getBounds().y;
				if (newHeight < listLayoutData.minimumHeight) {
					newHeight = listLayoutData.minimumHeight;
					e.doit = false;
				}
				listLayoutData.heightHint = newHeight;
				fileGroup.setLayoutData(listLayoutData);
				fileGroup.getParent().layout();
			}
		});
	}

	private void createYamlContentsGroup(Composite composite) {
		yamlGroup = new Group(composite, SWT.NONE);
		yamlGroup.setText("YAML Content");
		yamlGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		yamlGroup.setLayout(new GridLayout());

		fileYamlComposite = new Composite(yamlGroup, SWT.NONE);
		fileYamlComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		fileYamlComposite.setLayout(layout);

		Label fileYamlDescriptionLabel = new Label(fileYamlComposite, SWT.NONE);
		fileYamlDescriptionLabel.setText("Preview of the contents of selected deployment manifest YAML file:");
		fileYamlDescriptionLabel.setLayoutData(GridDataFactory.fillDefaults().create());

		fileYamlViewer = new SourceViewer(fileYamlComposite, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fileYamlViewer.configure(new ManifestYamlSourceViewerConfiguration());
		fileYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
		fileYamlViewer.setEditable(false);
		fileYamlViewer.getTextWidget().setBackground(composite.getBackground());

		manualYamlComposite = new Composite(yamlGroup, SWT.NONE);
		manualYamlComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		layout = new GridLayout();
		layout.marginWidth = 0;
		manualYamlComposite.setLayout(layout);

		Label manualYamlDescriptionLabel = new Label(manualYamlComposite, SWT.NONE);
		manualYamlDescriptionLabel.setText(readOnly ? "Preview of the contents of the auto-generated deployment manifest:" : "Edit deployment manifest contents:");
		manualYamlDescriptionLabel.setLayoutData(GridDataFactory.fillDefaults().create());

		manualYamlViewer = new SourceViewer(manualYamlComposite, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		manualYamlViewer.configure(new ManifestYamlSourceViewerConfiguration());
		manualYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
		manualYamlViewer.setEditable(!readOnly);
		if (readOnly) {
			manualYamlViewer.getTextWidget().setBackground(composite.getBackground());
		}
		manualYamlViewer.setDocument(new Document(defaultYaml == null ? "" : defaultYaml));
	}

	private void activateHanlders() {
		if (service != null) {
			for (EditorActionHandler handler : handlers) {
				activations.add(service.activateHandler(handler.getActionId(), handler));
			}
		}
	}

	private void deactivateHandlers() {
		if (service != null) {
			for (IHandlerActivation activation : activations) {
				service.deactivateHandler(activation);
			}
			activations.clear();
		}
	}

	private void refreshManifests() {
		IResource selectedResource = (IResource) ((IStructuredSelection) workspaceViewer.getSelection()).getFirstElement();
		final IResource resourceToRefresh = selectedResource instanceof IFile ? selectedResource.getParent() : selectedResource;
		Job job = new Job("Find all YAML files for project '" + project.getName() + "'") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					resourceToRefresh.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					status = e.getStatus();
				}
				getParentShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						// Remove selection listener to set selection from current pathModel value
						workspaceViewer.removeSelectionChangedListener(selectionListener);
						workspaceViewer.refresh(resourceToRefresh);
						updateManifestFile();
						// Add the selection listener back after the initial value has been set
						workspaceViewer.addSelectionChangedListener(selectionListener);
					}
				});
				return status;
			}
		};
		job.setRule(project);
		job.schedule();
	}

	private void updateManifestFile() {
		fileYamlContents.setValue(null);
		final IFile file = (IFile) fileModel.getValue();
		if (file == null) {
			fileLabel.setText(NO_MANIFEST_SELECETED_LABEL);
		} else {
			fileLabel.setText(file.getFullPath().toOSString());
			Job job = new Job("Loading YAML manifest file") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final Yaml yaml = new Yaml();
						final Object parsedYaml = yaml.load(file.getContents());
						final String text = IOUtil.toString(file.getContents());
						getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								fileYamlContents.setValue(parsedYaml instanceof Map<?, ?> ? text : null);
							}
						});
					} catch (final Throwable t) {
						getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								fileYamlContents.setValue(null);
							}
						});
					}
					return Status.OK_STATUS;
				}
			};
			job.setRule(file);
			job.schedule();
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void validate() {
		if (manifestTypeModel.getValue()) {
			setMessage("Choose an existing deployment manifest YAML file from the local file system.", IMessageProvider.INFORMATION);
		} else {
			if (readOnly) {
				setMessage("Current generated deployment manifest", IMessageProvider.INFORMATION);
			} else {
				setMessage("Enter deployment manifest YAML manually", IMessageProvider.INFORMATION);
			}
		}

		String error = null;
		if (manifestTypeModel.getValue()) {
			if (fileModel.getValue() == null) {
				error = "Deployment manifest file not selected";
			} else if (fileYamlContents.getValue() == null) {
				error = "Unable to load deployment manifest YAML file";
			}
		}
		setErrorMessage(error);
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		if (getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(newErrorMessage == null);
		}
	}

	public IFile getManifest() {
		return manifestTypeModel.getValue() ? fileModel.getValue() : null;
	}

	public String getManifestContents() {
		return manifestTypeModel.getValue() ? fileYamlViewer.getDocument().get() : manualYamlViewer.getDocument().get();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogSettings.getOrCreateSection(BootDashActivator.getDefault().getDialogSettings(), "ManifestFileDialog");
	}

	@Override
	public boolean close() {
		deactivateHandlers();
		getDialogBoundsSettings().put(DIALOG_LIST_HEIGHT_SETTING, ((GridData)fileGroup.getLayoutData()).heightHint);
		return super.close();
	}

	@Override
	protected void okPressed() {
		try {
			deploymentProperties = new ApplicationManifestHandler(project, domains, getManifest()) {
				@Override
				protected InputStream getInputStream() throws Exception {
					return new ByteArrayInputStream(getManifestContents().getBytes());
				}
			}.load(new NullProgressMonitor()).get(0);
			super.okPressed();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Invalid YAML content", e.getMessage());
		}
	}

	public CloudApplicationDeploymentProperties getCloudApplicationDeploymentProperties() {
		return deploymentProperties;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		/*
		 * If manual mode is selected fileGroup is missing and not accounted in
		 * the size of the dialog shell. Add its height here manually if dialog
		 * size was not persisted previously
		 */
		GridData fileGroupLayoutData = (GridData)fileGroup.getLayoutData();
		if (fileGroupLayoutData.exclude) {
			try {
				/*
				 * Hack: check if dialog width/height was persisted. If
				 * persisted then no need to calculate dialog size
				 */
				getDialogBoundsSettings().getInt("DIALOG_WIDTH");
			} catch (NumberFormatException e) {
				/*
				 * Exception is thrown if dialog width/height cannot be read
				 * from storage
				 */
				size.y += ((GridData) fileGroup.getLayoutData()).heightHint;
			}
		}
		return size;
	}

	private class EditorActionHandler extends AbstractHandler {

		private String actionId;
		private int operationId;

		public EditorActionHandler(String actionId, int operationId) {
			super();
			this.actionId = actionId;
			this.operationId = operationId;
		}

		public String getActionId() {
			return actionId;
		}

		@Override
		public Object execute(ExecutionEvent arg0) throws ExecutionException {
			if (manualYamlViewer.isEditable() && manualYamlViewer.getControl().isVisible()
					&& manualYamlViewer.getControl().isFocusControl()) {
				manualYamlViewer.doOperation(operationId);
			} else if (fileYamlViewer.isEditable() && fileYamlViewer.getControl().isVisible()
					&& fileYamlViewer.getControl().isFocusControl()) {
				fileYamlViewer.doOperation(operationId);
			}
			return null;
		}
	}

}
