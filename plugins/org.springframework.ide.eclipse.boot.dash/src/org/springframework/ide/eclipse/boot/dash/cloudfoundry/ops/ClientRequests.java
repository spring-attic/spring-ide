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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.Staging;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class ClientRequests {

	protected final CloudFoundryBootDashModel model;

	public ClientRequests(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public void createApplication(final CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
		new BasicRequest(model, deploymentProperties.getAppName(), "Creating application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.createApplication(deploymentProperties.getAppName(),
						new Staging(null, deploymentProperties.getBuildpack()), deploymentProperties.getMemory(),
						deploymentProperties.getUrls(), deploymentProperties.getServices());
			}
		}.run();
	}

	public CloudApplication getApplication(final String appName) throws Exception {

		return new ApplicationRequest<CloudApplication>(model, appName) {
			@Override
			protected CloudApplication doRun(CloudFoundryOperations client) throws Exception {
				try {
					return client.getApplication(appName);
				} catch (Exception e) {
					if (CloudErrors.is503Error(e)) {
						// Alternate way to fetch applications that does not
						// fetch instances and
						// may not throw 503 due to fetching stats on app
						// instances if app is not running
						List<CloudApplication> apps = client.getApplicationsWithBasicInfo();
						if (apps != null) {
							for (CloudApplication app : apps) {
								if (app.getName().equals(appName)) {
									return app;
								}
							}
						}
						return null;
					} else {
						throw e;
					}
				}
			}
		}.run();
	}

	public CloudApplication getApplication(final UUID appUUID) throws Exception {

		return new ApplicationRequest<CloudApplication>(model, appUUID.toString()) {
			@Override
			protected CloudApplication doRun(CloudFoundryOperations client) throws Exception {
				try {
					return client.getApplication(appUUID);
				} catch (Exception e) {
					if (CloudErrors.is503Error(e)) {
						// Alternate way to fetch applications that does not
						// fetch instances and
						// may not throw 503 due to fetching stats on app
						// instances if app is not running
						List<CloudApplication> apps = client.getApplicationsWithBasicInfo();
						if (apps != null) {
							for (CloudApplication app : apps) {
								if (app.getMeta().getGuid().equals(appUUID)) {
									return app;
								}
							}
						}
						return null;
					} else {
						throw e;
					}
				}
			}
		}.run();
	}

	public ApplicationStats getApplicationStats(final String appName) throws Exception {
		return new ApplicationRequest<ApplicationStats>(model, appName) {
			@Override
			protected ApplicationStats doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationStats(appName);
			}
		}.run();
	}

	public Map<CloudApplication, ApplicationStats> getApplicationStats(final List<CloudApplication> appsToLookUp)
			throws Exception {
		return new ClientRequest<Map<CloudApplication, ApplicationStats>>(model,
				"Getting stats for instances of all applications") {
			@Override
			protected Map<CloudApplication, ApplicationStats> doRun(CloudFoundryOperations client) throws Exception {
				try {
					return client.getApplicationStats(appsToLookUp);
				} catch (Exception e) {

					if (CloudErrors.is503Error(e)) {
						// Fetch each stat individually even though this is more
						// inefficient, as to avoid the case where failure
						// fetching
						// stats for one app
						// fails the entire operation
						Map<CloudApplication, ApplicationStats> allStats = new HashMap<CloudApplication, ApplicationStats>();

						for (CloudApplication app : appsToLookUp) {
							ApplicationStats stats = getApplicationStats(app.getName());
							allStats.put(app, stats);
						}

						return allStats;
					} else {
						throw e;
					}
				}
			}
		}.run();
	}

	public List<CloudApplication> getApplicationsWithBasicInfo() throws Exception {
		return new ClientRequest<List<CloudApplication>>(model, "Getting all Cloud applications") {
			@Override
			protected List<CloudApplication> doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationsWithBasicInfo();
			}
		}.run();
	}

	public void uploadApplication(final String appName, final ApplicationArchive archive) throws Exception {

		new BasicRequest(model, appName, "Uploading application archive") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.uploadApplication(appName, archive);
			}
		}.run();
	}

	public void stopApplication(final String appName) throws Exception {
		new ApplicationRequest<Void>(model, appName) {
			@Override
			protected Void doRun(CloudFoundryOperations client) throws Exception {
				client.stopApplication(appName);
				return null;
			}
		}.run();
	}

	public void restartApplication(final String appName) throws Exception {
		new BasicRequest(model, appName, "Restarting application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.restartApplication(appName);
			}
		}.run();
	}

	public void updateApplicationEnvironment(final String appName, final Map<String, String> varsToUpdate)
			throws Exception {
		new BasicRequest(model, appName, "Updating application environment variables") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationEnv(appName, varsToUpdate);
			}
		}.run();
	}

	public void updateApplicationStaging(final String appName, final Staging staging) throws Exception {
		new BasicRequest(model, appName, "Updating application buildpack") {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationStaging(appName, staging);
			}
		}.run();
	}

	public void updateApplicationServices(final String appName, final List<String> services) throws Exception {

		new BasicRequest(model, appName, "Updating application service bindings") {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationServices(appName, services);
			}
		}.run();
	}

	public void updateApplicationMemory(final String appName, final int memory) throws Exception {

		new BasicRequest(model, appName, "Updating application memory") {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationMemory(appName, memory);
			}
		}.run();
	}

	public void updateApplicationInstances(final String appName, final int instances) throws Exception {

		new BasicRequest(model, appName, "Updating application instances") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationInstances(appName, instances);
			}
		}.run();
	}

	public void updateApplicationUris(final String appName, final List<String> urls) throws Exception {
		new BasicRequest(model, appName, "Updating application URLs") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationUris(appName, urls);
			}
		}.run();
	}

	public void deleteApplication(final String appName) throws Exception {
		new BasicRequest(model, appName, "Deleting application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.deleteApplication(appName);
			}
		}.run();
	}

	public List<CloudDomain> getDomains() throws Exception {
		return new ClientRequest<List<CloudDomain>>(model, "Getting Cloud domains") {

			@Override
			protected List<CloudDomain> doRun(CloudFoundryOperations client) throws Exception {
				return client.getDomains();
			}
		}.run();
	}

	public List<CloudSpace> getSpaces() throws Exception {
		return new ClientRequest<List<CloudSpace>>(model, "Getting Cloud spaces") {

			@Override
			protected List<CloudSpace> doRun(CloudFoundryOperations client) throws Exception {
				return client.getSpaces();
			}
		}.run();
	}

	/**
	 * A more efficient API that fetches instances based on app {@link UUID}
	 *
	 * @param guid
	 * @return app instances for the specified app guid, or null if the app does
	 *         not exist
	 * @throws Exception
	 */
	public CloudAppInstances getExistingAppInstances(final UUID guid) throws Exception {

		return new ClientRequest<CloudAppInstances>(model, "Getting application instances") {
			@Override
			protected CloudAppInstances doRun(CloudFoundryOperations client) throws Exception {
				CloudApplication app = getApplication(guid);
				if (app != null) {
					ApplicationStats stats = getApplicationStats(app.getName());
					return new CloudAppInstances(app, stats);
				}
				return null;
			}
		}.run();
	}

	public CloudAppInstances getExistingAppInstances(final String appName) throws Exception {

		return new ClientRequest<CloudAppInstances>(model, appName, "Getting application instances") {
			@Override
			protected CloudAppInstances doRun(CloudFoundryOperations client) throws Exception {
				CloudApplication app = getApplication(appName);
				if (app != null) {
					ApplicationStats stats = getApplicationStats(appName);
					return new CloudAppInstances(app, stats);
				}
				return null;

			}
		}.run();
	}

}
