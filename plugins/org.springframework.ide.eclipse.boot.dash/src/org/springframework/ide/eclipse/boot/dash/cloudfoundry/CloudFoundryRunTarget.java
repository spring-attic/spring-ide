/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import static org.springframework.ide.eclipse.boot.dash.model.RunState.DEBUGGING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.INACTIVE;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.RUNNING;
import static org.springframework.ide.eclipse.boot.dash.model.RunState.STARTING;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.HOST;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.NAME;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class CloudFoundryRunTarget extends AbstractRunTarget implements RunTargetWithProperties {

	private CloudFoundryTargetProperties targetProperties;

	// Cache these to avoid frequent client calls
	private List<CFCloudDomain> domains;
	private List<CFSpace> spaces;
	private List<CFBuildpack> buildpacks;
	private List<CFStack> stacks;

	private LiveVariable<ClientRequests> cachedClient;
	private CloudFoundryClientFactory clientFactory;

	public CloudFoundryRunTarget(CloudFoundryTargetProperties targetProperties, RunTargetType runTargetType, CloudFoundryClientFactory clientFactory) {
		super(runTargetType, CloudFoundryTargetProperties.getId(targetProperties),
				CloudFoundryTargetProperties.getName(targetProperties));
		this.targetProperties = targetProperties;
		this.clientFactory = clientFactory;
		this.cachedClient = new LiveVariable<>();
	}

	private static final EnumSet<RunState> RUN_GOAL_STATES = EnumSet.of(INACTIVE, STARTING, RUNNING, DEBUGGING);
	private static final BootDashColumn[] DEFAULT_COLUMNS = { RUN_STATE_ICN, NAME, PROJECT, INSTANCES, DEFAULT_PATH, TAGS };
	private static final BootDashColumn[] ALL_COLUMNS = { RUN_STATE_ICN, NAME, PROJECT, INSTANCES, HOST, DEFAULT_PATH, TAGS };

	private static final String APPS_MANAGER_HOST = "APPS_MANAGER_HOST";

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return RUN_GOAL_STATES;
	}

	@Override
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		return null;
	}

	public ClientRequests getClient() {
		return cachedClient.getValue();
	}

	public void connect() throws Exception {
		try {
			this.domains = null;
			this.spaces = null;
			this.buildpacks = null;
			this.stacks = null;
			cachedClient.setValue(createClient());
		} catch (Exception e) {
			cachedClient.setValue(null);
			throw e;
		}
	}

	public void disconnect() {
		this.domains = null;
		this.spaces = null;
		this.buildpacks = null;
		this.stacks = null;
		if (getClient() != null) {
			getClient().logout();
			cachedClient.setValue(null);
		}
	}

	public boolean isConnected() {
		return cachedClient.getValue() != null;
	}

	public void addConnectionStateListener(ValueListener<ClientRequests> l) {
		cachedClient.addListener(l);
	}

	public void removeConnectionStateListener(ValueListener<ClientRequests> l) {
		cachedClient.removeListener(l);
	}

	public Version getCCApiVersion() {
		try {
			ClientRequests client = getClient();
			if (client!=null) {
				return client.getApiVersion();
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	protected ClientRequests createClient() throws Exception {
		return clientFactory.getClient(this);
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
	public AbstractBootDashModel createElementsTabelModel(BootDashModelContext context, BootDashViewModel parent) {
		return new CloudFoundryBootDashModel(this, context, parent);
	}

	@Override
	public CloudFoundryTargetProperties getTargetProperties() {
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
	public void refresh() throws Exception {
		// Fetching a new client always validates the CF credentials
		disconnect();
		connect();
	}

	@Override
	public boolean requiresCredentials() {
		return true;
	}

	public synchronized List<CFCloudDomain> getDomains( IProgressMonitor monitor)
			throws Exception {
		if (domains == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
			subMonitor.beginTask("Refreshing list of domains for " + getName(), 5);

			domains = getClient().getDomains();

			subMonitor.worked(5);
		}
		return domains;
	}

	public synchronized List<CFStack> getStacks(IProgressMonitor monitor)
			throws Exception {
		if (stacks == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
			subMonitor.beginTask("Refreshing list of stacks for " + getName(), 5);

			stacks = getClient().getStacks();

			subMonitor.worked(5);
		}
		return stacks;
	}

	public synchronized List<CFSpace> getSpaces(ClientRequests requests, IProgressMonitor monitor) throws Exception {
		if (spaces == null) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 10);

			subMonitor.beginTask("Refreshing list of spaces for " + getName(), 5);
			spaces = requests.getSpaces();
			subMonitor.worked(5);
		}
		return spaces;
	}

	public boolean isPWS() {
		return "https://api.run.pivotal.io".equals(getUrl());
	}

	private String getUrl() {
		String url = targetProperties.getUrl();
		while (url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}

	public SshClientSupport getSshClientSupport() throws Exception {
		ClientRequests client = getClient();
		return client.getSshClientSupport();
	}

	public String getBuildpack(IProject project) {
		// Only support Java project for now
		IJavaProject javaProject = JavaCore.create(project);

		if (javaProject != null) {
			try {
				if (this.buildpacks == null) {
					// Cache it to avoid frequent calls to CF
					this.buildpacks = getClient().getBuildpacks();
				}

				if (this.buildpacks != null) {
					String javaBuildpack = null;
					// Only chose a java build iff ONE java buildpack exists
					// that contains the java_buildpack pattern.

					for (CFBuildpack bp : this.buildpacks) {
						// iterate through all buildpacks to make sure only
						// ONE java buildpack exists
						if (bp.getName().contains("java_buildpack")) {
							if (javaBuildpack == null) {
								javaBuildpack = bp.getName();
							} else {
								// If more than two buildpacks contain
								// "java_buildpack", do not chose any. Let CF buildpack
								// detection decided which one to chose.
								javaBuildpack = null;
								break;
							}
						}
					}
					return javaBuildpack;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}

		return null;
	}

	@Override
	public String getTemplateVar(char name) {
		switch (name) {
		case 'o':
			return getTargetProperties().getOrganizationName();
		case 's':
			return getTargetProperties().getSpaceName();
		case 'a':
			return getTargetProperties().getUrl();
		case 'u':
			return getTargetProperties().getUsername();
		default:
			return super.getTemplateVar(name);
		}
	}

	public String getAppsManagerHost() {
		PropertyStoreApi props = getPersistentProperties();
		if (props != null) {
			String appsManagerURL = props.get(APPS_MANAGER_HOST);
			if (appsManagerURL != null) {
				return appsManagerURL;
			}
		}
		return getAppsManagerHostDefault();
	}

	public void setAppsManagerHost(String appsManagerURL) throws Exception {
		getPersistentProperties().put(APPS_MANAGER_HOST, appsManagerURL);
	}

	public String getAppsManagerURL() {
		String host = getAppsManagerHost();
		CloudFoundryTargetProperties targetProperties = getTargetProperties();

		String org = targetProperties.getOrganizationGuid();
		String space = targetProperties.getSpaceGuid();

		if (host != null && host.length() > 0 && org != null && org.length() > 0 && space != null && space.length() > 0) {
			return host + "/organizations/" + org + "/spaces/" + space;
		} else {
			return null;
		}
	}

	public String getAppsManagerHostDefault() {
		String url = getUrl();
		if (url != null && url.contains("//api.")) {
			return url.replace("//api.", "//console.");
		}
		else {
			return null;
		}
	}

	public LiveExpression<ClientRequests> getClientExp() {
		return cachedClient;
	}

}
