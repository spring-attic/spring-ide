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
import static org.springframework.ide.eclipse.boot.dash.model.RunState.STARTING;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.APP;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.HOST;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class CloudFoundryRunTarget extends AbstractRunTarget implements RunTargetWithProperties {

	private CloudFoundryTargetProperties targetProperties;

	// Cache these to avoid frequent client calls
	private List<CloudDomain> domains;
	private List<CloudSpace> spaces;

	private CloudFoundryOperations cachedClient;

	public CloudFoundryRunTarget(CloudFoundryTargetProperties targetProperties) {
		super(RunTargetTypes.CLOUDFOUNDRY, CloudFoundryTargetProperties.getId(targetProperties),
				CloudFoundryTargetProperties.getName(targetProperties));
		this.targetProperties = targetProperties;
	}

	private static final EnumSet<RunState> RUN_GOAL_STATES = EnumSet.of(INACTIVE, STARTING, RUNNING);
	private static final BootDashColumn[] DEFAULT_COLUMNS = { RUN_STATE_ICN, APP, PROJECT, INSTANCES, DEFAULT_PATH, TAGS };
	private static final BootDashColumn[] ALL_COLUMNS = { RUN_STATE_ICN, APP, PROJECT, INSTANCES, HOST, DEFAULT_PATH, TAGS };

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

	public CloudFoundryOperations getClient() throws Exception {
		if (cachedClient == null) {
			cachedClient = createClient();
		}
		return cachedClient;
	}

	protected CloudFoundryOperations createClient() throws Exception {
		return CloudFoundryUiUtil.getClient(this);
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashColumn[] getAllColumns() {
		return ALL_COLUMNS;
	}

	@Override
	public BootDashModel createElementsTabelModel(BootDashModelContext context) {
		return new CloudFoundryBootDashModel(this, context);
	}

	@Override
	public TargetProperties getTargetProperties() {
		return targetProperties;
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsTo() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public void validate() throws Exception {
		// Fetching a client always validates the CF credentials
		cachedClient = createClient();
	}

	@Override
	public boolean requiresCredentials() {
		return true;
	}

	public synchronized List<CloudDomain> getDomains(IProgressMonitor monitor) throws Exception {
		if (domains == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
			subMonitor.beginTask("Refreshing list of domains for " + getName(), 5);

			domains = getClient().getDomains();

			subMonitor.worked(5);
		}
		return domains;
	}

	public synchronized List<CloudSpace> getSpaces(IProgressMonitor monitor) throws Exception {
		if (spaces == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);

			subMonitor.beginTask("Refreshing list of spaces for " + getName(), 5);
			spaces = getClient().getSpaces();
			subMonitor.worked(5);
		}
		return spaces;
	}

	public CloudSpace getCloudSpace(String org, String space) {
		CloudSpace cloudSpace = null;
		synchronized (this) {
			for (CloudSpace cSpace : spaces) {
				if (cSpace.getName().equals(space) && cSpace.getOrganization().getName().equals(org)) {
					cloudSpace = cSpace;
					break;
				}
			}
		}
		return cloudSpace;
	}

}
