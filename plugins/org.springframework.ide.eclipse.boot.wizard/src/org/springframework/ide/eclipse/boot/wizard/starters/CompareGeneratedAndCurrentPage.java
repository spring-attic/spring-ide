/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class CompareGeneratedAndCurrentPage extends WizardPage {

	private final InitializrFactoryModel<AddStartersModel> factoryModel;
	private Composite contentsContainer;
	private Control compareViewer = null;

	public CompareGeneratedAndCurrentPage(InitializrFactoryModel<AddStartersModel> factoryModel) {
		super("Compare", "Compare Generated POM with the current POM", null);
		this.factoryModel = factoryModel;
	}

	@Override
	public void createControl(Composite parent) {
		contentsContainer = new Composite(parent, SWT.NONE);
		contentsContainer.setLayout(GridLayoutFactory.fillDefaults().create());
		setControl(contentsContainer);
	}

	private void setupCompareViewer() {
		try {

			AddStartersModel model = factoryModel.getModel().getValue();
			AddStartersDiff diff = model.getDiff();

			AddStartersCompareInput compareInput = diff.getCompareInput();

			// Wire the model compare input with the UI editor input
			final CompareEditorInput editorInput = createCompareEditorInput(compareInput);

			// Save the editor on ok pressed
			model.onOkPressed(() -> {
				// IMPORTANT: save the contents of the local pom via
				// the Eclipse compare input API. The reason is that
				// Eclipse compare will first flush the left and right
				// input before saving and mark the editor as dirty .
				// Without this flush, nothing will be saved.
				if (editorInput.isSaveNeeded()) {
					// Use this API instead of the save API
					// This will ensure the editor is flushed and saved
					// in the UI thread before the controls are disposed
					// Not doing this can result in NPEs if a direct
					// save is performed asynchronously as the editor
					// controls may be disposed when the wizard closes
					// but before save can be performed
					editorInput.okPressed();
				}
			});

			compareViewer = editorInput.createContents(contentsContainer);
			compareViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			contentsContainer.layout();
		} catch (Exception e) {
			Log.log(e);
		}
	}

	private CompareEditorInput createCompareEditorInput(AddStartersCompareInput inputFromModel) {

		final CompareEditorInput currentCompareInput = new CompareEditorInput(inputFromModel.getConfiguration()) {
			@Override
			protected Object prepareInput(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
				return new DiffNode(inputFromModel.getLocalResource().getWrappedResource(),
						inputFromModel.getGeneratedResource());
			}

			@Override
			public void saveChanges(IProgressMonitor monitor) throws CoreException {
				// IMPORTANT Delegate to Eclipse compare to flush the viewer BEFORE commiting
				// local changes
				super.saveChanges(monitor);
				inputFromModel.getLocalResource().commit(monitor);
			}

		};
		currentCompareInput.setTitle("Merge Local File");

		new Job("Comparing project file with generated file from Spring Initializr.") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					currentCompareInput.run(monitor);
					return Status.OK_STATUS;
				} catch (InvocationTargetException | InterruptedException e) {
					return ExceptionUtil.coreException(e).getStatus();
				}
			}

		}.schedule();
		return currentCompareInput;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			adjustCompareViewer();
		}
		super.setVisible(visible);
	}

	private void adjustCompareViewer() {
		if (compareViewer != null) {
			compareViewer.dispose();
		}
		setupCompareViewer();
	}

	@Override
	public boolean isPageComplete() {
		return getWizard().getContainer().getCurrentPage() == this;
	}

}
