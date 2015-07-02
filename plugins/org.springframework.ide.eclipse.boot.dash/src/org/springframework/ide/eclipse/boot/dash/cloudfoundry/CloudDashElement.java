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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

public class CloudDashElement implements BootDashElement {

	private final CloudFoundryRunTarget cloudTarget;

	private final CloudApplication app;

	public CloudDashElement(CloudFoundryRunTarget cloudTarget, CloudApplication app) {
		this.cloudTarget = cloudTarget;
		this.app = app;
	}

	@Override
	public void stopAsync() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

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

}
