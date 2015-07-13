/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

public class CloudDashElement extends WrappingBootDashElement<String> {

	private final CloudFoundryRunTarget cloudTarget;

	private CloudApplication app;

	private final BootDashModel context;

	public CloudDashElement(CloudFoundryRunTarget cloudTarget, CloudApplication app, BootDashModel context) {
		super(app.getName());
		this.cloudTarget = cloudTarget;
		this.app = app;
		this.context = context;
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		runOp(getStop(), ui);
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		runOp(getRestart(), ui);
	}

	@Override
	public void openConfig(UserInteractions ui) {

	}

	@Override
	public String getName() {
		return app.getName();
	}

	@Override
	public LinkedHashSet<String> getTags() {
		return new LinkedHashSet<String>(0);
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {

	}

	@Override
	public IJavaProject getJavaProject() {
		return null;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		if (app != null && app.getState() != null) {
			switch (this.app.getState()) {
			case STARTED:
				return RunState.RUNNING;
			case STOPPED:
				return RunState.INACTIVE;
			case UPDATING:
				return RunState.STARTING;
			}
		}

		return RunState.INACTIVE;
	}

	@Override
	public RunTarget getTarget() {
		return cloudTarget;
	}

	@Override
	public int getLivePort() {
		return 0;
	}

	@Override
	public String getLiveHost() {
		return null;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		return new ArrayList<RequestMapping>(0);
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return null;
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		return null;
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {

	}

	@Override
	public String getDefaultRequestMappingPath() {
		return null;
	}

	@Override
	public void setDefaultRequestMapingPath(String defaultPath) {

	}

	/*
	 * Runnable Ops
	 */
	protected CloudApplicationOperation getRestart() throws Exception {
		return new CloudApplicationOperation(app.getName(), cloudTarget.getClient(),
				"Starting application: " + app.getName()) {

			@Override
			protected void doAppOperation(CloudFoundryOperations operations, IProgressMonitor monitor)
					throws Exception {
				operations.startApplication(appName);
			}

		};
	}

	protected CloudApplicationOperation getStop() throws Exception {
		return new CloudApplicationOperation(app.getName(), cloudTarget.getClient(),
				"Stopping application: " + app.getName()) {

			@Override
			protected void doAppOperation(CloudFoundryOperations operations, IProgressMonitor monitor)
					throws Exception {
				operations.stopApplication(appName);
			}
		};
	}

	protected void runOp(final CloudApplicationOperation runnable, final UserInteractions ui) throws Exception {

		Job job = new Job(runnable.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Be sure to update the app after the app operation is
					// performed so
					// that it
					// reflects changes done by the op on the app
					app = runnable.run(monitor);
					context.notifyElementChanged(CloudDashElement.this);

				} catch (Exception e) {
					final String message = e.getMessage();
					Display.getCurrent().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (ui != null) {
								ui.errorPopup("Error performing Cloud operation: ", message);
							}
						}
					});
				}
				return Status.OK_STATUS;
			}

		};

		job.schedule();

	}

	@Override
	public int getActualInstances() {
		return 1;
	}

	@Override
	public int getDesiredInstances() {
		return 1;
	}
}
