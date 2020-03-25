/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.path;
import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.pattern;
import static org.springframework.ide.eclipse.boot.wizard.starters.PathSelectors.rootFiles;
import static org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput.fromFile;
import static org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput.fromWorkspaceResource;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersCompareModel.AddStartersTrackerState;
import org.springframework.ide.eclipse.boot.wizard.starters.eclipse.ResourceCompareInput;
import org.springframework.ide.eclipse.maven.pom.PomPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class CompareGeneratedAndCurrentPage extends WizardPage {

	private final AddStartersWizardModel wizardModel;
	private Composite contentsContainer;
	private Control compareViewer = null;

	final private ValueListener<AddStartersCompareResult> compareResultListener = new ValueListener<AddStartersCompareResult>() {
		@Override
		public void gotValue(LiveExpression<AddStartersCompareResult> exp, AddStartersCompareResult compareResult) {
			Display.getDefault().asyncExec(() -> {
				if (compareResult != null) {
					setupCompareViewer();
				}
			});
		}
	};

	final private ValueListener<AddStartersTrackerState> downloadStateListener = new ValueListener<AddStartersTrackerState>() {
		@Override
		public void gotValue(LiveExpression<AddStartersTrackerState> exp, AddStartersTrackerState downloadState) {
			Display.getDefault().asyncExec(() -> {
				if (downloadState != null) {
					setMessage(downloadState.getMessage());
				} else {
					setMessage("");
				}
			});
		}
	};

	public CompareGeneratedAndCurrentPage(AddStartersWizardModel wizardModel) {
		super("Compare", "Compare local project with generated project from Spring Initializr", null);
		this.wizardModel = wizardModel;
	}

	@Override
	public void createControl(Composite parent) {
		contentsContainer = new Composite(parent, SWT.NONE);
		contentsContainer.setLayout(GridLayoutFactory.fillDefaults().create());
		setControl(contentsContainer);
	}

	private void connectModelToUi(AddStartersCompareModel compareModel) {
		compareModel.getCompareResult().addListener(compareResultListener);
		compareModel.getDownloadTracker().addListener(downloadStateListener);
	}

	private void disconnectFromUi(AddStartersCompareModel compareModel) {
		compareModel.getCompareResult().removeListener(compareResultListener);
		compareModel.getDownloadTracker().removeListener(downloadStateListener);
	}

	private void setupCompareViewer() {
		try {
			InitializrModel initializrModel = wizardModel.getInitializrFactoryModel().getModel().getValue();
			AddStartersCompareModel compareModel = initializrModel.getCompareModel();

			// Transform the compare result from the model into a compare editor input
			final CompareEditorInput editorInput = createCompareEditorInput(compareModel.getCompareResult().getValue());
			editorInput.getCompareConfiguration().setProperty(PomPlugin.POM_STRUCTURE_ADDITIONS_COMPARE_SETTING, true);
			editorInput.getCompareConfiguration().setProperty(ResourceCompareInput.OPEN_DIFF_NODE_COMPARE_SETTING, compareModel.diffFileToOpenInitially());

			// Save the editor on ok pressed
			wizardModel.onOkPressed(() -> {
				if (editorInput.isSaveNeeded()) {
					// This will save changes in the editor.
					editorInput.okPressed();
				}
			});

			compareViewer = editorInput.createContents(contentsContainer);
			compareViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			contentsContainer.layout();
		} catch (Exception e) {
			setErrorMessage("Failed to compare local project contents with generated project from the Initializr Service");
			Log.log(e);
		}
	}

	/**
	 * Creates the Eclipse compare editor input from the compare model results.
	 *
	 * @param resultFromModel
	 * @return
	 * @throws Exception
	 */
	private CompareEditorInput createCompareEditorInput(AddStartersCompareResult resultFromModel) throws Exception {

		ResourceCompareInput compareEditorInput = new ResourceCompareInput(resultFromModel.getConfiguration(),
				rootFiles()
				.or(path("HELP.md"))
				.or(path("src/main/resources/application.properties"))
				.or(path("src/main/resources/application.yml"))
				.or(path("src/main/resources/static/"))
				.or(path("src/main/resources/templates/"))
				.or(pattern("src/main/resources/static/*"))
				.or(pattern("src/main/resources/templates/*"))
		);
		setResources(compareEditorInput, resultFromModel);

		compareEditorInput.setTitle(
				"Compare local project on the left with generated project from Spring Initializr on the right");

		getWizard().getContainer().run(true, false, monitor -> {
			monitor.beginTask(
					"Calculating differences between project '"
							+ resultFromModel.getLocalResource().getProject().getName() + "' and 'starter.zip'",
					IProgressMonitor.UNKNOWN);
			compareEditorInput.run(monitor);
			monitor.done();
		});

		return compareEditorInput;
	}

	/**
	 * Sets the "left" and "right" resources to compare in the compare editor input
	 *
	 * @param input
	 * @param inputFromModel
	 * @throws Exception
	 */
	private void setResources(ResourceCompareInput input, AddStartersCompareResult inputFromModel) throws Exception {
		IProject leftProject = inputFromModel.getLocalResource().getProject();
		input.setSelection(fromFile(inputFromModel.getDownloadedProject()), fromWorkspaceResource(leftProject));
	}

	@Override
	public boolean isPageComplete() {
		return getWizard().getContainer().getCurrentPage() == this;
	}

	@Override
	public void setVisible(boolean visible) {
		// Connect the model to the UI only when the page becomes visible.
		// If this connection is done before, either the UI controls may not yet be created
		// or the model may not yet be available.
		InitializrModel initializrModel = wizardModel.getInitializrFactoryModel().getModel().getValue();
		AddStartersCompareModel compareModel = initializrModel.getCompareModel();

		if (visible) {
			compareModel.initTrackers();
			connectModelToUi(compareModel);
			try {
				getWizard().getContainer().run(true, false, monitor -> initializrModel.downloadProjectToCompare(monitor));
			} catch (InvocationTargetException | InterruptedException e) {
				setErrorMessage("Failed to download project from the Initializr Service");
				Log.log(e);
			}
		} else {
			disconnectFromUi(compareModel);
			compareModel.disposeTrackers();
			if (compareViewer != null) {
				compareViewer.dispose();
			}
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		super.dispose();
		wizardModel.dispose();
	}
}
