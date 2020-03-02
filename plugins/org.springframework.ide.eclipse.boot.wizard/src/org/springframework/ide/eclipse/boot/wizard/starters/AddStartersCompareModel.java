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

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Model that generates two sides to compare: a local project and a project
 * downloaded from Spring Initializr. The model  will download the project from initializr as well.
 *
 */
public class AddStartersCompareModel implements Disposable {

	private final ISpringBootProject bootProject;
	private final InitializrProjectDownloader projectDownloader;
	private LiveVariable<AddStartersCompareResult> compareInputModel = new LiveVariable<>(null);
	private LiveVariable<AddStartersTrackerState> downloadTracker = new LiveVariable<>(
			AddStartersTrackerState.NOT_STARTED);

	public AddStartersCompareModel(InitializrProjectDownloader projectDownloader, ISpringBootProject bootProject) {
		this.bootProject = bootProject;
		this.projectDownloader = projectDownloader;
	}

	public void initTrackers() {
		compareInputModel = new LiveVariable<>(null);
		downloadTracker = new LiveVariable<>(AddStartersTrackerState.NOT_STARTED);
	}

	public void disposeTrackers() {
		this.compareInputModel.dispose();
		this.downloadTracker.dispose();
	}

	public LiveExpression<AddStartersCompareResult> getCompareResult() {
		return compareInputModel;
	}

	public LiveExpression<AddStartersTrackerState> getDownloadTracker() {
		return downloadTracker;
	}

	@Override
	public void dispose() {
		disposeTrackers();
		this.projectDownloader.dispose();
	}

	public void downloadProject(List<Dependency> dependencies) {
		Job job = new Job("Downloading project from Initializr") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					downloadTracker.setValue(AddStartersTrackerState.IS_DOWNLOADING);
					File generatedProject = projectDownloader.getProject(dependencies, bootProject);
					generateCompareResult(generatedProject, dependencies);
					downloadTracker.setValue(AddStartersTrackerState.DOWNLOADING_COMPLETED);
					return Status.OK_STATUS;
				} catch (Exception e) {
					downloadTracker.setValue(AddStartersTrackerState.error(e));
					Log.log(e);
					return ExceptionUtil.status(e);
				}
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
	}

	protected void generateCompareResult(File projectFromInitializr, List<Dependency> dependencies) throws Exception {
		IProject project = bootProject.getProject();
		boolean editable = true;
		LocalProject localProject = new LocalProject(project, editable);

		AddStartersCompareResult inputModel = new AddStartersCompareResult(localProject, projectFromInitializr);
		this.compareInputModel.setValue(inputModel);
	}

	static class AddStartersTrackerState {

		public static final AddStartersTrackerState NOT_STARTED = new AddStartersTrackerState("");

		public static final AddStartersTrackerState IS_DOWNLOADING = new AddStartersTrackerState(
				"Downloading project from Spring Initializr. Please wait....");

		public static final AddStartersTrackerState DOWNLOADING_COMPLETED = new AddStartersTrackerState(
				"Project downloaded from Spring Initializr successfully.");

		private final String message;
		private final Exception error;

		public AddStartersTrackerState(String message, Exception error) {
			this.message = message;
			this.error = error;
		}

		public AddStartersTrackerState(String message) {
			this(message, null);
		}

		public Exception getError() {
			return this.error;
		}

		public String getMessage() {
			return this.message;
		}

		public static AddStartersTrackerState error(Exception error) {
			String message = "Failed to download project from Spring Initializr";
			if (error != null) {
				message += ": " + error.getMessage();
			}
			return new AddStartersTrackerState(message, error);
		}
	}

}
