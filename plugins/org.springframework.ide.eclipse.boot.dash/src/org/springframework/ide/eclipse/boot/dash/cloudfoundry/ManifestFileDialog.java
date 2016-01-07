/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.ManifestDescriptorResolver;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.yaml.snakeyaml.Yaml;

/**
 * Dialog for selecting deployment manifest YAML file
 *
 * @author Alex Boyko
 *
 */
public class ManifestFileDialog extends TitleAreaDialog {

	final static private String DIALOG_LIST_HEIGHT_SETTING = "ManifestFileDialog.listHeight"; //$NON-NLS-1$

	private IProject project;

	private LiveVariable<IPath> pathModel = new LiveVariable<IPath>();

	private static String NO_MANIFEST_SELECETED_LABEL = "(Deployment Manifest file NOT selected)";

	private Label fileLabel;
	private SourceViewer yamlViewer;
	private ListViewer manifestsViewer;

	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent e) {
			pathModel.setValue(e.getSelection().isEmpty() ? null : (IPath) ((IStructuredSelection) e.getSelection()).toArray()[0]);
		}
	};

	public ManifestFileDialog(Shell parentShell, IProject project, IPath manifest) {
		super(parentShell);
		this.project = project;
		pathModel.setValue(manifest);
	}

	@Override
	public void create() {
	    super.create();
	    setTitle("Select Deployment Manifest File");
	    setMessage("Choose an existing deployment manifest YAML file from the local file system.", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(container, parent.getStyle());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(new GridLayout(2, false));

		fileLabel = new Label(composite, SWT.NONE);
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create());

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		buttonsComposite.setLayout(layout);

		Button clearButton = new Button(buttonsComposite, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pathModel.setValue(null);
				manifestsViewer.setSelection(StructuredSelection.EMPTY);
			}
		});
		clearButton.setLayoutData(GridDataFactory.fillDefaults().create());

		Button refreshButton = new Button(buttonsComposite, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshManifests();
			}
		});
		refreshButton.setLayoutData(GridDataFactory.fillDefaults().create());

		Composite viewsComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		viewsComposite.setLayout(layout);
		viewsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, SWT.DEFAULT).create());

		manifestsViewer = new ListViewer(viewsComposite);
		int height = 100;
		try {
			height = getDialogBoundsSettings().getInt(DIALOG_LIST_HEIGHT_SETTING);
		} catch (NumberFormatException e) {
			// ignore exception
		}
		manifestsViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, height).create());
		manifestsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IPath)element).toOSString();
			}
		});
		manifestsViewer.setContentProvider(ArrayContentProvider.getInstance());

		final Sash resizeSash = new Sash(viewsComposite, SWT.HORIZONTAL);
		resizeSash.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 4).grab(true, false).create());
		resizeSash.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData listLayoutData = (GridData)manifestsViewer.getControl().getLayoutData();
				int newHeight = listLayoutData.heightHint + e.y - resizeSash.getBounds().y;
				if (newHeight < listLayoutData.minimumHeight) {
					newHeight = listLayoutData.minimumHeight;
					e.doit = false;
				}
				manifestsViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, newHeight).create());
				manifestsViewer.getControl().getParent().layout();
			}
		});

		yamlViewer = new SourceViewer(viewsComposite, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		yamlViewer.configure(new YEditSourceViewerConfiguration());

		yamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		pathModel.addListener(new ValueListener<IPath>() {
			@Override
			public void gotValue(LiveExpression<IPath> exp, final IPath value) {
				updateVisuals();
			}
		});

		refreshManifests();

		return container;
	}

	private void refreshManifests() {
		Job job = new Job("Find all YAML files for project '" + project.getName() + "'") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				IStatus status = Status.OK_STATUS;
				List<IPath> paths = new ArrayList<>();
				try {
					ManifestDescriptorResolver.findFiles(project, "", "yml", paths);
				} catch (CoreException e) {
					status = e.getStatus();
				}
				final IPath[] array = new IPath[paths.size()];
				int i = 0;
				for (IPath path : paths) {
					array[i++] = path.makeRelativeTo(project.getLocation());
				}
				getParentShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						// Remove selection listener to set selection from current pathModel value
						manifestsViewer.removeSelectionChangedListener(selectionListener);
						manifestsViewer.setInput(array);
						manifestsViewer.setSelection(pathModel.getValue() == null ? StructuredSelection.EMPTY : new StructuredSelection(Collections.singletonList(pathModel.getValue())));
						// Add the selection listener back after the initial value has been set
						manifestsViewer.addSelectionChangedListener(selectionListener);
					}
				});
				return status;
			}
		};
		job.setRule(project);
		job.schedule();
	}

	private void updateVisuals() {
		yamlViewer.setDocument(null);
		yamlViewer.setEditable(false);
		yamlViewer.getControl().setVisible(false);
		setErrorMessage(null);
		IPath value = pathModel.getValue();
		if (value == null || value.toString().isEmpty()) {
			fileLabel.setText(NO_MANIFEST_SELECETED_LABEL);
		} else {
			fileLabel.setText(value.toOSString());
			final File file = project.getLocation().append(value).toFile();
			if (!file.exists()) {
				setErrorMessage("Selected deployment manifest file does not exist!");
			} else if (file.isDirectory()) {
				setErrorMessage("Selected deployment manifest file is a folder!");
			} else {
				Job job = new Job("Loading YAML manifest file") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final Yaml yaml = new Yaml();
						FileInputStream fs = null;
						try {
							fs = new FileInputStream(file);
						} catch (FileNotFoundException e) {
							// Ignore. Check if file exists is above
						}
						try {
							final Object parsedYaml = yaml.load(fs);
							if (parsedYaml instanceof Map<?, ?>) {
								getShell().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										yamlViewer.setDocument(new Document(yaml.dump(parsedYaml)));
										// viewer.setEditable(true);
										yamlViewer.getControl().setVisible(true);
										setErrorMessage(null);
									}
								});
							} else {
								getShell().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										setErrorMessage("Unable to load deployment manifest file contents.");
									}
								});
							}
						} catch (Throwable t) {
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									setErrorMessage("Unable to load deployment manifest file contents.");
								}
							});
						}
						return Status.OK_STATUS;
					}

				};
				job.setRule(project.getFile(value.toString()));
				job.schedule();
			}
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		if (getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(newErrorMessage == null);
		}
	}

	public IPath getManifest() {
		return pathModel.getValue();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogSettings.getOrCreateSection(BootDashActivator.getDefault().getDialogSettings(), "ManifestFileDialog");
	}

	@Override
	public boolean close() {
		getDialogBoundsSettings().put(DIALOG_LIST_HEIGHT_SETTING, ((GridData)manifestsViewer.getControl().getLayoutData()).heightHint);
		return super.close();
	}

}
