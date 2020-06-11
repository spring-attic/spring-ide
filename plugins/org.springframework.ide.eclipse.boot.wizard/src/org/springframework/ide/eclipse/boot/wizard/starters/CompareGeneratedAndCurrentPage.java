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
import java.util.function.Predicate;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.preferences.PreferenceConstants;
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
			getWizard().getContainer().getShell().getDisplay().asyncExec(() -> {
				if (compareResult != null) {
					setupCompareViewer();
				}
			});
		}
	};

	final private ValueListener<AddStartersTrackerState> downloadStateListener = new ValueListener<AddStartersTrackerState>() {
		@Override
		public void gotValue(LiveExpression<AddStartersTrackerState> exp, AddStartersTrackerState downloadState) {
			getWizard().getContainer().getShell().getDisplay().asyncExec(() -> {
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
			AddStartersCompareModel compareModel = wizardModel.getCompareModel().getValue();

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

	private String[] excludeGlobPatterns() {
		String globStr = BootWizardActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ADD_STARTERS_EXCLUDE_RESOURCES_FROM_COMPARE).trim();
		return globStr.split("\\s*,\\s*");
	}

	/**
	 * Creates the Eclipse compare editor input from the compare model results.
	 *
	 * @param resultFromModel
	 * @return
	 * @throws Exception
	 */
	private CompareEditorInput createCompareEditorInput(AddStartersCompareResult resultFromModel) throws Exception {

		Predicate<String> filter = s -> true;
		for (String glob : excludeGlobPatterns()) {
			filter = filter.and(pattern(glob).negate());
		}

		filter.and(rootFiles()
				.or(path("HELP.md"))
				.or(path("src/main/resources/application.properties"))
				.or(path("src/main/resources/application.yml"))
				.or(path("src/main/resources/static/"))
				.or(path("src/main/resources/templates/"))
				.or(pattern("src/main/resources/static/*"))
				.or(pattern("src/main/resources/templates/*")));

		ResourceCompareInput compareEditorInput = new ResourceCompareInput(resultFromModel.getConfiguration(), filter);
		setResources(compareEditorInput, resultFromModel);

		compareEditorInput.setTitle(
				"Compare local project on the left with generated project from Spring Initializr on the right");

		getWizard().getContainer().run(true, false, monitor -> {
			monitor.beginTask(
					"Calculating differences between project '"
							+ resultFromModel.getLocalResource().getProject().getName() + "' and 'starter.zip'",
					IProgressMonitor.UNKNOWN);
			compareEditorInput.run(monitor);
			Display display = getWizard().getContainer().getShell().getDisplay();
			if (compareEditorInput.hasDiffs()) {
				display.asyncExec(() -> setMessage(
						"Differences detected between local project and project from Initializr Service",
						IMessageProvider.INFORMATION));
			} else {
				display.asyncExec(() -> setMessage(
						"No differences found between local project and project from Initializr Service",
						IMessageProvider.WARNING));
			}
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
		AddStartersCompareModel compareModel = wizardModel.getCompareModel().getValue();

		if (visible) {
			compareModel.initTrackers();
			connectModelToUi(compareModel);
			try {
				getWizard().getContainer().run(true, false, monitor -> compareModel.createComparison(monitor));
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
