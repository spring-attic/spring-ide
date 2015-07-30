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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.cfeclipse;

import java.util.List;
import java.util.Set;

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryPlugin;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudServerUtil;
import org.cloudfoundry.ide.eclipse.server.core.internal.client.CloudFoundryApplicationModule;
import org.cloudfoundry.ide.eclipse.server.core.internal.client.CloudFoundryServerBehaviour;
import org.cloudfoundry.ide.eclipse.server.core.internal.spaces.CloudFoundrySpace;
import org.cloudfoundry.ide.eclipse.server.ui.internal.Messages;
import org.cloudfoundry.ide.eclipse.server.ui.internal.ServerHandlerCallback;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.util.ServerLifecycleAdapter;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class ServersIntegration implements ValueListener<Set<RunTarget>> {

	private final BootDashViewModel viewModel;

	public ServersIntegration(BootDashViewModel viewModel) {
		this.viewModel = viewModel;
		// TODO: Work still needed to automatically create/connect a WTP server
		// instance associated with a Boot Dash view run target
		// this.viewModel.getRunTargets().addListener(this);
	}

	// THese are constants as defined in CF Eclipse WTP extension point
	public static final String CF_RUNTYPE_ID = "org.cloudfoundry.appcloudserver.runtime.10";
	public static final String CF_RUNTYPE_NAME = "Cloud Foundry (Runtime) v1.0";

	static ISchedulingRule SERVER_UPDATE_RULE = new ISchedulingRule() {

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return this == rule;
		}
	};

	@Override
	public void gotValue(LiveExpression<Set<RunTarget>> exp, final Set<RunTarget> value) {
		Job job = new Job("Updating Cloud Foundry server instances in Servers view") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (value != null) {
					List<CloudFoundryServer> existingCloudServers = CloudServerUtil.getCloudServers();

					for (RunTarget target : value) {
						if (target instanceof CloudFoundryRunTarget) {
							CloudFoundryRunTarget runTarget = (CloudFoundryRunTarget) target;
							CloudFoundryServer cloudServer = getServer(runTarget, existingCloudServers);
							if (cloudServer == null) {
								try {
									createServer(runTarget, monitor);
								} catch (Exception e) {
									BootDashActivator.log(e);
								}
							}
						}
					}
				}
				return Status.OK_STATUS;
			}

		};
		job.setRule(SERVER_UPDATE_RULE);
		job.schedule();

	}

	public CloudFoundryServer getServer(CloudFoundryRunTarget runTarget,
			List<CloudFoundryServer> existingCloudServers) {
		CloudFoundryTargetProperties targetProperties = (CloudFoundryTargetProperties) runTarget.getTargetProperties();

		for (CloudFoundryServer cloudServer : existingCloudServers) {
			String url = cloudServer.getUrl();
			String org = cloudServer.getCloudFoundrySpace().getOrgName();
			String space = cloudServer.getCloudFoundrySpace().getSpaceName();
			if (url.equals(targetProperties.getUrl()) && org.equals(targetProperties.getOrganizationName())
					&& space.equals(targetProperties.getSpaceName())) {
				return cloudServer;
			}
		}
		return null;

	}

	public CloudFoundryServer getServer(CloudFoundryRunTarget runTarget) {
		CloudFoundryTargetProperties targetProperties = (CloudFoundryTargetProperties) runTarget.getTargetProperties();
		List<CloudFoundryServer> existingCloudServers = CloudServerUtil.getCloudServers();

		for (CloudFoundryServer cloudServer : existingCloudServers) {
			String url = cloudServer.getUrl();
			String org = cloudServer.getCloudFoundrySpace().getOrgName();
			String space = cloudServer.getCloudFoundrySpace().getSpaceName();
			if (url.equals(targetProperties.getUrl()) && org.equals(targetProperties.getOrganizationName())
					&& space.equals(targetProperties.getSpaceName())) {
				return cloudServer;
			}
		}
		return null;

	}

	public void createServer(CloudFoundryRunTarget runTarget, final IProgressMonitor monitor) throws Exception {
		CloudFoundryTargetProperties targetProperties = (CloudFoundryTargetProperties) runTarget.getTargetProperties();

		final String password = targetProperties.getPassword();
		final String userName = targetProperties.getUserName();
		final String url = targetProperties.getUrl();
		final boolean selfSignedCert = targetProperties.isSelfsigned();
		String org = targetProperties.getOrganizationName();
		String space = targetProperties.getSpaceName();
		final CloudSpace cloudSpace = runTarget.getCloudSpace(org, space);

		ServerHandler serverHandler = new ServerHandler(CF_RUNTYPE_ID, CF_RUNTYPE_NAME, runTarget.getName(), true);

		final IServer server = serverHandler.createServer(monitor, ServerHandler.NEVER_OVERWRITE,
				new ServerHandlerCallback() {

					@Override
					public void configureServer(IServerWorkingCopy wc) throws CoreException {
						CloudFoundryServer cloudServer = (CloudFoundryServer) wc.loadAdapter(CloudFoundryServer.class,
								monitor);

						if (cloudServer != null) {
							cloudServer.setPassword(password);
							cloudServer.setUsername(userName);
							cloudServer.setUrl(url);
							cloudServer.setSpace(cloudSpace);
							cloudServer.setSelfSignedCertificate(selfSignedCert);
							cloudServer.saveConfiguration(monitor);
						}
					}
				});

		final CloudFoundryServer cloudServer = (CloudFoundryServer) server.loadAdapter(CloudFoundryServer.class,
				monitor);

		if (cloudServer != null) {
			ServerLifecycleAdapter listener = new ServerLifecycleAdapter() {
				@Override
				public void serverAdded(IServer server) {
					ServerCore.removeServerLifecycleListener(this);

					Job job = new ConnectJob(cloudServer, server);
					// this is getting called before
					// CloudFoundryServer.saveConfiguration() has
					// flushed the
					// configuration therefore delay job
					job.schedule(500L);
				}
			};
			ServerCore.addServerLifecycleListener(listener);
		}

	}

	private static class ConnectJob extends Job {

		private final CloudFoundryServer originalServer;

		private final IServer server;

		public ConnectJob(CloudFoundryServer originalServer, IServer server) {
			super("Connecting to: " + originalServer.getServer().getName());
			this.originalServer = originalServer;
			this.server = server;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			CloudFoundryServer cf = (CloudFoundryServer) server.loadAdapter(CloudFoundryServer.class, monitor);
			if (cf.getPassword() == null) {
				// configuration has not been saved, yet, ignore
				return Status.CANCEL_STATUS;
			}

			if (cf != null && cf.getUsername().equals(originalServer.getUsername())
					&& cf.getPassword().equals(originalServer.getPassword())
					&& cf.getUrl().equals(originalServer.getUrl())) {

				boolean connect = false;

				if (cf.hasCloudSpace() && originalServer.hasCloudSpace()) {
					CloudFoundrySpace originalSpace = originalServer.getCloudFoundrySpace();
					CloudFoundrySpace space = cf.getCloudFoundrySpace();
					connect = space.getOrgName().equals(originalSpace.getOrgName())
							&& space.getSpaceName().equals(originalSpace.getSpaceName());
				}

				if (connect) {
					CloudFoundryServerBehaviour behaviour = cf.getBehaviour();
					if (behaviour != null) {
						try {
							behaviour.connect(monitor);
						} catch (CoreException e) {
							BootDashActivator.log(e);
						}
					}
				}

			}
			return Status.OK_STATUS;
		}
	}

	public void openConsole(CloudDashElement element, IProgressMonitor monitor) throws Exception {

		CloudFoundryServer cloudServer = getServer((CloudFoundryRunTarget) element.getParent().getRunTarget());
		if (cloudServer != null) {
			CloudFoundryApplicationModule appModule = cloudServer.getExistingCloudModule(element.getName());
			if (appModule != null) {

				CloudFoundryPlugin.getCallback().printToConsole(cloudServer, appModule,
						"Fetching log contents for " + element.getName() + ". Please wait...\n", true, false);

				CloudFoundryPlugin.getCallback().showCloudFoundryLogs(cloudServer, appModule, 0, monitor);
			}
		}
	}

}
