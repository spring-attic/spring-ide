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
import java.util.Map;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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

	private IProject project;

	private LiveVariable<IPath> pathModel = new LiveVariable<IPath>();

	private static String NO_MANIFEST_SELECETED_LABEL = "(Deployment Manifest file NOT selected)";

	private Label fileLabel;
	private SourceViewer viewer;
	private FileDialog fileDialog;

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
		composite.setLayout(new GridLayout(3, false));

		fileLabel = new Label(composite, SWT.NONE);
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create());

		Button clearButton = new Button(composite, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pathModel.setValue(null);
			}
		});
		clearButton.setLayoutData(GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create());

		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		fileDialog = new FileDialog (getParentShell(), SWT.OPEN);
		fileDialog.setFilterNames (new String [] {"All YAML Files", "All Files (*)"});
		fileDialog.setFilterPath(project.getLocation().toString());
		if (SWT.getPlatform().equals("win32")) {
			fileDialog.setFilterExtensions(new String [] {"*.yml", "*.*"});
		} else {
			fileDialog.setFilterExtensions(new String [] {"*.yml", "*"});
		}
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = fileDialog.open();
				if (filePath != null) {
					pathModel.setValue(new Path(filePath).makeRelativeTo(project.getLocation()));
				}
			}
		});
		browseButton.setLayoutData(GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create());

		viewer = new SourceViewer(composite, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.configure(new YEditSourceViewerConfiguration());

		GridData data = GridDataFactory.fillDefaults().grab(true, true).span(3, SWT.DEFAULT).create();
		viewer.getControl().setLayoutData(data);

		pathModel.addListener(new ValueListener<IPath>() {
			@Override
			public void gotValue(LiveExpression<IPath> exp, final IPath value) {
				updateVisuals();
			}
		});

		updateVisuals();

		return container;
	}

	private void updateVisuals() {
		viewer.setDocument(null);
		viewer.setEditable(false);
		viewer.getControl().setVisible(false);
		setErrorMessage(null);
		IPath value = pathModel.getValue();
		if (value == null || value.toString().isEmpty()) {
			fileLabel.setText(NO_MANIFEST_SELECETED_LABEL);
		} else {
			fileDialog.setFilterPath(project.getLocation().append(value.removeLastSegments(1)).toString());
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
										viewer.setDocument(new Document(yaml.dump(parsedYaml)));
										// viewer.setEditable(true);
										viewer.getControl().setVisible(true);
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

}
