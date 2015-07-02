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

import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class CloudFoundryRunTarget extends AbstractRunTarget {

	private CloudFoundryTargetProperties targetProperties;

	public CloudFoundryRunTarget(CloudFoundryTargetProperties targetProperties) {
		super(getId(targetProperties), getId(targetProperties));
		this.targetProperties = targetProperties;
	}

	private static final EnumSet<RunState> RUN_GOAL_STATES = EnumSet.of(INACTIVE, RUNNING);
	private static final BootDashColumn[] DEFAULT_COLUMNS = {RUN_STATE_ICN, APP};

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return RUN_GOAL_STATES;
	}

	@Override
	public List<ILaunchConfiguration> getLaunchConfigs(BootDashElement element) {
		return Collections.emptyList();
	}

	@Override
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		return null;
	}

	public static String getId(CloudFoundryTargetProperties properties) {
		return getId(properties.getUserName(), properties.getUrl(), properties.getSpace().getOrganization().getName(),
				properties.getSpace().getName());

	}

	public static String getId(String userName, String url, String orgName, String spaceName) {
		return userName + " : " + url + " : " + orgName + " : " + spaceName;
	}

	public CloudFoundryOperations getClient() throws CoreException {
		return CloudFoundryUiUtil.getClient(targetProperties);
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

}
