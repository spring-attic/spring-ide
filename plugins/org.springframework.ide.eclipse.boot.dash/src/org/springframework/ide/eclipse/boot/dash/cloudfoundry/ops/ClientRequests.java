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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.Staging;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class ClientRequests {

	protected final CloudFoundryBootDashModel model;

	public ClientRequests(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public void createApplication(final CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.createApplication(deploymentProperties.getAppName(),
						new Staging(null, deploymentProperties.getBuildpackUrl()), deploymentProperties.getMemory(),
						deploymentProperties.getUrls(), deploymentProperties.getServices());
			}
		}.run();
	}

	public CloudApplication getApplication(final String appName) throws Exception {

		try {
			return new ClientRequest<CloudApplication>(model) {
				@Override
				protected CloudApplication doRun(CloudFoundryOperations client) throws Exception {
					return client.getApplication(appName);
				}
			}.run();
		} catch (Exception e) {
			// Ignore if 404
			if (!CloudErrors.isNotFoundException(e)) {
				throw e;
			}
		}
		return null;
	}

	public ApplicationStats getApplicationStats(final CloudApplication app) throws Exception {
		return new ClientRequest<ApplicationStats>(model) {
			@Override
			protected ApplicationStats doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationStats(app);
			}
		}.run();
	}

	public ApplicationStats getApplicationStats(final String appName) throws Exception {
		return new ClientRequest<ApplicationStats>(model) {
			@Override
			protected ApplicationStats doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationStats(appName);
			}
		}.run();
	}

	public Map<CloudApplication, ApplicationStats> getApplicationStats(final List<CloudApplication> appsToLookUp)
			throws Exception {
		return new ClientRequest<Map<CloudApplication, ApplicationStats>>(model) {
			@Override
			protected Map<CloudApplication, ApplicationStats> doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationStats(appsToLookUp);
			}
		}.run();
	}

	public List<CloudApplication> getApplicationsWithBasicInfo() throws Exception {
		return new ClientRequest<List<CloudApplication>>(model) {
			@Override
			protected List<CloudApplication> doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationsWithBasicInfo();
			}
		}.run();
	}

	public void uploadApplication(final String appName, final ApplicationArchive archive) throws Exception {

		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.uploadApplication(appName, archive);
			}
		}.run();
	}

	public void stopApplication(final String appName) throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.stopApplication(appName);
			}
		}.run();
	}

	public void restartApplication(final String appName) throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.restartApplication(appName);
			}
		}.run();
	}

	public Map<String, Object> getApplicationEnvironment(final UUID guid) throws Exception {
		return new ClientRequest<Map<String, Object>>(model) {
			@Override
			protected Map<String, Object> doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationEnvironment(guid);
			}
		}.run();
	}

	public void updateApplicationEnvironment(final String appName, final Map<String, String> varsToUpdate)
			throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationEnv(appName, varsToUpdate);
			}
		}.run();
	}

	public void updateApplicationStaging(final String appName, final Staging staging) throws Exception {
		new BaseClientRequest(model) {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationStaging(appName, staging);
			}
		}.run();
	}

	public void updateApplicationServices(final String appName, final List<String> services) throws Exception {

		new BaseClientRequest(model) {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationServices(appName, services);
			}
		}.run();
	}

	public void updateApplicationMemory(final String appName, final int memory) throws Exception {

		new BaseClientRequest(model) {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationMemory(appName, memory);
			}
		}.run();
	}

	public void updateApplicationInstances(final String appName, final int instances) throws Exception {

		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationInstances(appName, instances);
			}
		}.run();
	}

	public void updateApplicationUris(final String appName, final List<String> urls) throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationUris(appName, urls);
			}
		}.run();
	}

	public void deleteApplication(final String appName) throws Exception {
		new BaseClientRequest(model) {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.deleteApplication(appName);
			}
		}.run();
	}
}
