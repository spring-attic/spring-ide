/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel.ManifestType;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestEditorActivator;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp.LSBasedSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;
import org.springframework.ide.eclipse.editor.support.util.ShellProviders;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Cloud Foundry Application deployment properties dialog. Allows user to select
 * manifest YAML file or enter deployment manifest YAML manually.
 *
 * @author Alex Boyko
 *
 */
public class DeploymentPropertiesDialog extends TitleAreaDialog {

	final static private String DIALOG_LIST_HEIGHT_SETTING = "ManifestFileDialog.listHeight"; //$NON-NLS-1$
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

	private SourceViewerDecorationSupport fileYamlDecorationSupport;
	private SourceViewerDecorationSupport manualYamlDecorationSupport;
	private Label fileLabel;
	private Sash resizeSash;
	private SourceViewer fileYamlViewer;
	private SourceViewer manualYamlViewer;
	private AppNameAnnotationSupport fileYamlAppNameAnnotationSupport;
	private AppNameAnnotationSupport manualYamlAppNameAnnotationSupport;
	private TreeViewer workspaceViewer;
	private Button refreshButton;
	private Button buttonFileManifest;
	private Button buttonManualManifest;
	private Group fileGroup;
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

	private ISharedTextColors colorsCache = new ISharedTextColors() {

		private Map<RGB, Color> colors = new HashMap<>();

		@Override
		public Color getColor(RGB rgb) {
			Color color = colors.get(rgb);
			if (color == null) {
				color = new Color(getShell().getDisplay(), rgb);
				colors.put(rgb, color);
			}
			return color;
		}

		@Override
		public void dispose() {
			for (Color color : colors.values()) {
				if (!color.isDisposed()) {
					color.dispose();
				}
			}
			colors.clear();
		}

	};

	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(final SelectionChangedEvent e) {
			IResource resource = (IResource) getStructuredSelection(workspaceViewer).getFirstElement();
			model.setSelectedManifest(resource);
		}
	};

	final private DeploymentPropertiesDialogModel model;

	public DeploymentPropertiesDialog(Shell parentShell, DeploymentPropertiesDialogModel model) {
		super(parentShell);
		this.model = model;
		this.service = (IHandlerService) PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		this.activations = new ArrayList<>(handlers.length);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Select Deployment Manifest for project '" + model.getProjectName() + "'");
		Composite container = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(container, parent.getStyle());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(new GridLayout());

		createModeSwitchGroup(composite);

		createFileGroup(composite);

		createResizeSash(composite);

		createYamlContentsGroup(composite);

		activateHanlders();

		model.type.addListener(new ValueListener<ManifestType>() {
			@Override
			public void gotValue(LiveExpression<ManifestType> exp, ManifestType type) {
				GridData gridData;
				boolean isFile = type == ManifestType.FILE;
				buttonFileManifest.setSelection(isFile);
				buttonManualManifest.setSelection(!isFile);

				refreshButton.setEnabled(isFile && !workspaceViewer.getSelection().isEmpty());
				workspaceViewer.getControl().setEnabled(isFile);
				fileLabel.setEnabled(isFile);

				gridData = GridDataFactory.copyData((GridData) fileGroup.getLayoutData());
				gridData.exclude = !isFile;
				fileGroup.setVisible(isFile);
				fileGroup.setLayoutData(gridData);
				gridData = GridDataFactory.copyData((GridData) resizeSash.getLayoutData());
				gridData.exclude = !isFile;
				resizeSash.setVisible(isFile);
				resizeSash.setLayoutData(gridData);
				fileGroup.getParent().layout();

				fileYamlComposite.setVisible(isFile);
				gridData = GridDataFactory.copyData((GridData) fileYamlComposite.getLayoutData());
				gridData.exclude = !isFile;
				fileYamlComposite.setLayoutData(gridData);
				manualYamlComposite.setVisible(!isFile);
				gridData = GridDataFactory.copyData((GridData) manualYamlComposite.getLayoutData());
				gridData.exclude = isFile;
				manualYamlComposite.setLayoutData(gridData);
				yamlGroup.layout();
				yamlGroup.getParent().layout();
			}
		});

		model.getValidator().addListener(new UIValueListener<ValidationResult>() {
			@Override
			protected void uiGotValue(LiveExpression<ValidationResult> exp, ValidationResult value) {
				ValidationResult result = exp.getValue();
				if (getButton(IDialogConstants.OK_ID) != null) {
					getButton(IDialogConstants.OK_ID).setEnabled(result.status != IStatus.ERROR);
				}
				setMessage(result.msg, result.getMessageProviderStatus());
			}
		});

		parent.pack(true);

		/*
		 * Reveal the selected manifest file in the workspace viewer now when
		 * controls are created and laid out
		 */
		if (!workspaceViewer.getSelection().isEmpty()) {
			workspaceViewer.setSelection(workspaceViewer.getSelection(), true);
		}
		return container;
	}

	private void createModeSwitchGroup(Composite composite) {
		Group typeGroup = new Group(composite, SWT.NONE);
		typeGroup.setText("Manifest Type");
		typeGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		typeGroup.setLayout(new GridLayout(2, true));

		buttonFileManifest = new Button(typeGroup, SWT.RADIO);
		buttonFileManifest.setText("File");
		buttonFileManifest.setSelection(model.isFileManifestType());
		buttonFileManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buttonFileManifest.getSelection()) {
					model.setManifestType(ManifestType.FILE);
				}
			}
		});
		buttonFileManifest.setLayoutData(GridDataFactory.fillDefaults().create());

		buttonManualManifest = new Button(typeGroup, SWT.RADIO);
		buttonManualManifest.setText("Manual");
		buttonManualManifest.setSelection(model.isManualManifestType());
		buttonManualManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buttonManualManifest.getSelection()) {
					model.setManifestType(ManifestType.MANUAL);
				}
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

		workspaceViewer = new TreeViewer(fileGroup);
		workspaceViewer.setContentProvider(new BaseWorkbenchContentProvider());
		workspaceViewer.setLabelProvider(new WorkbenchLabelProvider());
		workspaceViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		workspaceViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		/*
		 * Do not set the selection based on manifest file changes outside of UI. Bad. SWT doesn't like it.
		 */
		if (model.getSelectedManifest() != null) {
			workspaceViewer.setSelection(new StructuredSelection(new Object[] { model.getSelectedManifest() }), true);
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
		refreshButton.setImage(BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.REFRESH_ICON));
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

		IResource manifestFile = model.getSelectedManifest();
		if (manifestFile != null) {
			selectionIndex = RESOURCE_FILTERS.length - 1;
			for (int i = 0; i < RESOURCE_FILTERS.length; i++) {
				boolean accept = true;
				for (ViewerFilter filter : RESOURCE_FILTERS[i]) {
					accept = filter.select(null, null, manifestFile);
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
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		fileYamlComposite.setLayout(layout);

		fileLabel = new Label(fileYamlComposite, SWT.WRAP);
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).span(3, SWT.DEFAULT).create());

		model.getFileLabel().addListener(new UIValueListener<String>() {
			@Override
			protected void uiGotValue(LiveExpression<String> exp, String value) {
				if (!fileLabel.isDisposed()) {
					fileLabel.setText(exp.getValue());
				}
			}
		});

		DefaultMarkerAnnotationAccess fileMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		OverviewRuler fileOverviewRuler = new OverviewRuler(fileMarkerAnnotationAccess, 10, colorsCache);
		String appName = model.getDeployedAppName();
		IVerticalRuler fileVerticalRuler = appName == null ? new CompositeRuler() : /*new VerticalRuler(16, fileMarkerAnnotationAccess)*/ null;
		fileYamlViewer = new SourceViewer(fileYamlComposite, fileVerticalRuler, fileOverviewRuler, true,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		SourceViewerConfiguration fileYamlSourceViewerConfiguration = null;

		if (ManifestEditorActivator.getDefault().isLanguageServerEnabled()) {
			fileYamlSourceViewerConfiguration = new LSBasedSourceViewerConfiguration(ShellProviders.from(composite)) {

				@Override
				protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer viewer) {
					return new AppNameReconcilingStrategy(viewer, getAstProvider(), appName);
				}
			};
		}
		else {
			fileYamlSourceViewerConfiguration = new ManifestYamlSourceViewerConfiguration(ShellProviders.from(composite)) {

				@Override
				protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer viewer) {
					CompositeReconcilingStrategy strategy = new CompositeReconcilingStrategy();
					strategy.setReconcilingStrategies(new IReconcilingStrategy[] { super.createReconcilerStrategy(viewer),
							new AppNameReconcilingStrategy(viewer, getAstProvider(), appName) });
					return strategy;
				}

				@Override
				protected ForceableReconciler createReconciler(ISourceViewer sourceViewer) {
					IReconcilingStrategy strategy = createReconcilerStrategy(sourceViewer);
					if (strategy!=null) {
						InstantForceableReconciler reconciler = new InstantForceableReconciler(strategy);
						reconciler.setDelay(500);
						return reconciler;
					}
					return null;
				}

			};

		}

		fileYamlViewer.configure(fileYamlSourceViewerConfiguration);
		fileYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());

		fileYamlDecorationSupport = new SourceViewerDecorationSupport(fileYamlViewer, fileOverviewRuler, fileMarkerAnnotationAccess, colorsCache);
		fileYamlAppNameAnnotationSupport = new AppNameAnnotationSupport(fileYamlViewer, fileMarkerAnnotationAccess, colorsCache);

		model.getFileDocument().addListener(new UIValueListener<IDocument>() {
			protected void uiGotValue(LiveExpression<IDocument> exp, IDocument value) {
				updateManifestFile();
			}
		});

		manualYamlComposite = new Composite(yamlGroup, SWT.NONE);
		manualYamlComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		layout = new GridLayout();
		layout.marginWidth = 0;
		manualYamlComposite.setLayout(layout);

		Label manualYamlDescriptionLabel = new Label(manualYamlComposite, SWT.WRAP);
		manualYamlDescriptionLabel.setText(model.isManualManifestReadOnly() ? "Preview of the contents of the auto-generated deployment manifest:" : "Edit deployment manifest contents:");
		manualYamlDescriptionLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());


		DefaultMarkerAnnotationAccess manualMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		OverviewRuler manualOverviewRuler = new OverviewRuler(manualMarkerAnnotationAccess, 10, colorsCache);
		IVerticalRuler manualVerticalRuler = appName == null ? new CompositeRuler() : /*new VerticalRuler(16, manualMarkerAnnotationAccess)*/ null;
		manualYamlViewer = new SourceViewer(manualYamlComposite, manualVerticalRuler,
				manualOverviewRuler, true,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		SourceViewerConfiguration manualSourceViewerConfiguration = null;
//		if (ManifestEditorActivator.getDefault().isLanguageServerEnabled()) {
//			manualSourceViewerConfiguration = new LSBasedSourceViewerConfiguration(ShellProviders.from(composite)) {
//
//				@Override
//				protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer viewer) {
//					return new AppNameReconcilingStrategy(viewer, getAstProvider(), appName);
//				}
//			};
//		}
//		else {
			manualSourceViewerConfiguration = new ManifestYamlSourceViewerConfiguration(ShellProviders.from(composite)) {

				@Override
				protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer viewer) {
					CompositeReconcilingStrategy strategy = new CompositeReconcilingStrategy();
					strategy.setReconcilingStrategies(new IReconcilingStrategy[] { super.createReconcilerStrategy(viewer),
							new AppNameReconcilingStrategy(viewer, getAstProvider(), appName) });
					return strategy;
				}

			};
//		}

		manualYamlViewer.configure(manualSourceViewerConfiguration);
		manualYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());

		if (model.isManualManifestReadOnly()) {
			manualYamlViewer.setEditable(false);
			manualYamlViewer.getTextWidget().setCaret(null);
			manualYamlViewer.getTextWidget().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		manualYamlDecorationSupport = new SourceViewerDecorationSupport(manualYamlViewer, manualOverviewRuler, manualMarkerAnnotationAccess, colorsCache);
		manualYamlAppNameAnnotationSupport = new AppNameAnnotationSupport(manualYamlViewer, manualMarkerAnnotationAccess, colorsCache);

		manualYamlViewer.setDocument(model.getManualDocument(), model.getManualAnnotationModel());

		/*
		 * Set preferences for viewers decoration support
		 */
		for (AnnotationPreference preference : (List<AnnotationPreference>) new MarkerAnnotationPreferences().getAnnotationPreferences()) {
			manualYamlDecorationSupport.setAnnotationPreference(preference);
			fileYamlDecorationSupport.setAnnotationPreference(preference);
		}
		manualYamlDecorationSupport.install(EditorsUI.getPreferenceStore());
		fileYamlDecorationSupport.install(EditorsUI.getPreferenceStore());

		/*
		 * Set the proper Font on the YAML viewers
		 */
		fileYamlViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		manualYamlViewer.getTextWidget().setFont(JFaceResources.getTextFont());

		/*
		 * Set App Name annotation model on the dialog's model
		 */
		model.setManualAppNameAnnotationModel(AppNameAnnotationSupport.getAppNameAnnotationModel(manualYamlViewer));

		/*
		 * Set YAML validation annotation model (resource marker annotation model)
		 */
		model.setManualResourceAnnotationModel(manualYamlViewer.getAnnotationModel());
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
		Job job = new Job("Refreshing resources for '" + resourceToRefresh.getName() + "'") {
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
		job.setRule(resourceToRefresh);
		job.schedule();
	}

	private void updateManifestFile() {
		IDocument document = model.getFileDocument().getValue();
		fileYamlViewer.getControl().setEnabled(document!=null);
		fileYamlViewer.setDocument(document, model.getFileAnnotationModel());
		/*
		 * Set New App Name annotation model on the dialog's model
		 */
		model.setFileAppNameAnnotationModel(AppNameAnnotationSupport.getAppNameAnnotationModel(fileYamlViewer));

		/*
		 * Set YAML validation annotation model (resource marker annotation model)
		 */
		model.setFileResourceAnnotationModel(fileYamlViewer.getAnnotationModel());
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogSettings.getOrCreateSection(BootDashActivator.getDefault().getDialogSettings(), "ManifestFileDialog");
	}

	@Override
	public boolean close() {
		if (getReturnCode() == IDialogConstants.CANCEL_ID) {
			model.cancelPressed();
		} else {
			if (!model.okPressed()) {
				return false;
			}
		}
		getDialogBoundsSettings().put(DIALOG_LIST_HEIGHT_SETTING, ((GridData)fileGroup.getLayoutData()).heightHint);
		boolean close = super.close();
		dispose();
		return close;
	}

	protected void dispose() {
		model.dispose();

		/*
		 * Deactivate handlers for key bindings
		 */
		deactivateHandlers();

		/*
		 * dispose SWT and JFace resources
		 */
		colorsCache.dispose();
		if (manualYamlDecorationSupport != null) {
			manualYamlDecorationSupport.dispose();
		}
		if (fileYamlDecorationSupport != null) {
			fileYamlDecorationSupport.dispose();
		}
		if (manualYamlAppNameAnnotationSupport != null) {
			manualYamlAppNameAnnotationSupport.dispose();
		}
		if (fileYamlAppNameAnnotationSupport != null) {
			fileYamlAppNameAnnotationSupport.dispose();
		}
	}

	// TODO: this should be replaced with TreeViewer.getStructuredSelection once we drop support for Eclipse 4.4
	// the TreeViewer.getStructuredSelection() API got introduced in Eclipse 4.5
	private ITreeSelection getStructuredSelection(TreeViewer treeViewer) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof ITreeSelection) {
			return (ITreeSelection) selection;
		}
		throw new ClassCastException("AbstractTreeViewer should return an instance of ITreeSelection from its getSelection() method."); //$NON-NLS-1$
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
				size.y += fileGroupLayoutData.heightHint;
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
					&& manualYamlViewer.getTextWidget().isFocusControl()) {
				manualYamlViewer.doOperation(operationId);
			} else if (fileYamlViewer.isEditable() && fileYamlViewer.getControl().isVisible()
					&& fileYamlViewer.getTextWidget().isFocusControl()) {
				fileYamlViewer.doOperation(operationId);
			}
			return null;
		}
	}

	public static IFile findManifestYamlFile(IProject project) {
		if (project == null) {
			return null;
		}
		IFile file = project.getFile("manifest.yml");
		if (file.exists()) {
			return file;
		}
		IFile yamlFile = null;
		try {
			for (IResource r : project.members()) {
				if (r instanceof IFile) {
					file = (IFile) r;
					if (MANIFEST_YAML_FILE_FILTER.select(null, project, file)) {
						return file;
					} else if (YAML_FILE_FILTER.select(null, project, file)) {
						yamlFile = file;
					}
				}
			}
		} catch (CoreException e) {
			// ignore
		}
		return yamlFile;
	}

}
