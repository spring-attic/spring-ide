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

import java.util.LinkedHashSet;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

public class CloudDashElement implements BootDashElement {

	private final CloudFoundryRunTarget cloudTarget;

	private final CloudApplication app;

	private final BootDashModel context;

	public CloudDashElement(CloudFoundryRunTarget cloudTarget, CloudApplication app, BootDashModel context) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return app.getName();
	}

	@Override
	public LinkedHashSet<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {
		// TODO Auto-generated method stub

	}

	@Override
	public IJavaProject getJavaProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLiveHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultRequestMappingPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultRequestMapingPath(String defaultPath) {
		// TODO Auto-generated method stub

	}

	/*
	 * Runnable Ops
	 */
	protected Operation getRestart() {
		return new Operation("Starting application: " + app.getName()) {

			@Override
			protected void runOp(IProgressMonitor monitor) throws Exception {
				cloudTarget.getClient().startApplication(app.getName());
			}
		};
	}

	protected Operation getStop() {
		return new Operation("Stopping application: " + app.getName()) {

			@Override
			protected void runOp(IProgressMonitor monitor) throws Exception {
				cloudTarget.getClient().stopApplication(app.getName());
			}
		};
	}

	protected void runOp(Operation runnable, UserInteractions ui) throws Exception {
		try {
			Operation.runForked(runnable);
			context.notifyElementChanged(this);
		} catch (Exception e) {
			if (ui != null) {
				ui.errorPopup("Error performing Cloud operation: ", e.getMessage());
			}
			throw e;
		}
	}

}
