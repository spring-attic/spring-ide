/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.ide.eclipse.internal.uaa.monitor.CommandUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.LibraryUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.PartUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.monitor.ProjectContributionUsageMonitor;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;

/**
 * Plug-in entry point for the UAA Plugin.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class UaaPlugin extends AbstractUIPlugin {

	public static final int FULL_DATA = PrivacyLevel.ENABLE_UAA.getNumber();

	public static final int LIMITED_DATA = PrivacyLevel.LIMITED_DATA.getNumber();

	public static final int NO_DATA = PrivacyLevel.DISABLE_UAA.getNumber();

	public static final int DEFAULT_PRIVACY_LEVEL = LIMITED_DATA;

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.uaa"; //$NON-NLS-1$

	private static UaaPlugin plugin;

	private List<IUsageMonitor> monitors = new ArrayList<IUsageMonitor>();

	private UaaManager usageMonitorManager;

	public int getPrivacyLevel() {
		return this.usageMonitorManager.getPrivacyLevel();
	}

	public String getUserAgentContents(String value) {
		return this.usageMonitorManager.getUserAgentContents(value);
	}

	public String getUserAgentHeader() {
		if (this.usageMonitorManager != null) {
			return usageMonitorManager.getUserAgentHeader();
		}
		return DefaultHttpParams.getDefaultParams().getParameter(HttpClientParams.USER_AGENT).toString();
	}

	public void setPrivacyLevel(int level) {
		this.usageMonitorManager.setPrivacyLevel(level);
	}

	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		monitors.clear();
		monitors.add(new PartUsageMonitor());
		monitors.add(new CommandUsageMonitor());
		monitors.add(new LibraryUsageMonitor());
		monitors.add(new ProjectContributionUsageMonitor());

		UIJob startupJob = new UIJob("Initializing Spring UAA") { //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor progressMonitor) {
				usageMonitorManager = new UaaManager();
				for (IUsageMonitor monitor : monitors) {
					monitor.startMonitoring(usageMonitorManager);
				}

				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule(2500);

		Job updateJob = new Job("Checking for updates") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ServiceReference ref = context.getServiceReference(IProvisioningAgent.class.getName());
				IProvisioningAgent agent = (IProvisioningAgent) context.getService(ref);

				IProfile profile = getProfileRegistry(agent).getProfile(IProfileRegistry.SELF);

				ArrayList<IInstallableUnit> iusWithUpdates = new ArrayList<IInstallableUnit>();
				ProvisioningContext provisioningContext = new ProvisioningContext(agent);
				provisioningContext.setMetadataRepositories(new URI[] { URI
						.create("http://dist.springsource.com/snapshot/TOOLS/nightly/e3.6") });

				IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
				Iterator<IInstallableUnit> iter = profile.query(query, null).iterator();
				while (iter.hasNext()) {
					IInstallableUnit iu = iter.next();
					IQueryResult<IInstallableUnit> replacements = getPlanner(agent).updatesFor(iu, provisioningContext,
							null);
					if (!replacements.isEmpty())
						iusWithUpdates.add(iu);
				}

				context.ungetService(ref);
				return Status.OK_STATUS;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule(500);
	}

	private IProfileRegistry getProfileRegistry(IProvisioningAgent agent) {
		return (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
	}

	private IPlanner getPlanner(IProvisioningAgent agent) {
		return (IPlanner) agent.getService(IPlanner.SERVICE_NAME);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		for (IUsageMonitor monitor : monitors) {
			monitor.stopMonitoring();
		}
		super.stop(context);
	}

	public static UaaPlugin getDefault() {
		return plugin;
	}

}
