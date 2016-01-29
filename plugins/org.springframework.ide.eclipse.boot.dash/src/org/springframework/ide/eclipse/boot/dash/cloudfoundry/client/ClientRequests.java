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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.ApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.BuildpackSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.CloudInfoV2;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

public class ClientRequests {

	protected final CloudFoundryOperations client;
	private CloudInfoV2 cachedCloudInfo;
	private CloudFoundryTargetProperties targetProperties;

	public ClientRequests(CloudFoundryOperations client, CloudFoundryTargetProperties targetProperties) {
		Assert.isNotNull(client, "ClientRequests needs a non-null client");
		this.client = client;
		this.targetProperties = targetProperties;
	}

	public void logout() {
		client.logout();
	}

	public void createApplication(final CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
		new BasicRequest(this.client, deploymentProperties.getAppName(), "Creating application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.createApplication(deploymentProperties.getAppName(),
						new Staging(null, deploymentProperties.getBuildpack()), deploymentProperties.getMemory(),
						new ArrayList<>(deploymentProperties.getUris()), deploymentProperties.getServices());
			}
		}.call();
	}

	protected HttpProxyConfiguration getProxyConf() {
		//TODO: get this right!!! (But the client in the rest of boot dahs also doesn't do this.
		return null;
	}

	private CloudInfoV2 getCloudInfoV2() throws Exception {
		//cached cloudInfo as it doesn't really change and is more like a bunch of static info about how a target is configured.
		if (this.cachedCloudInfo==null) {
			CloudCredentials creds = new CloudCredentials(targetProperties.getUsername(), targetProperties.getPassword());
			HttpProxyConfiguration proxyConf = getProxyConf();
			this.cachedCloudInfo = new CloudInfoV2(creds, client.getCloudControllerUrl(), proxyConf, targetProperties.isSelfsigned());
		}
		return this.cachedCloudInfo;
	}

	public CloudApplication getApplication(final String appName) throws Exception {

		return new ApplicationRequest<CloudApplication>(this.client, appName) {
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
		}.call();
	}

	public CloudApplication getApplication(final UUID appUUID) throws Exception {

		return new ApplicationRequest<CloudApplication>(this.client, appUUID.toString()) {
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
		}.call();
	}

	public ApplicationStats getApplicationStats(final String appName) throws Exception {
		return new ApplicationInstanceRequest(this.client, appName).call();
	}

	public Map<CloudApplication, ApplicationStats> waitForApplicationStats(final List<CloudApplication> appsToLookUp,
			final long timeout) throws Exception {
		Callable<Map<CloudApplication, ApplicationStats>> task = new AllApplicationInstancesRequest(this.client,
				appsToLookUp);
		return RetryUtil.retry(2000, timeout, task);
	}

	public List<CloudApplication> getApplicationsWithBasicInfo() throws Exception {
		return new ClientRequest<List<CloudApplication>>(this.client, "Getting all Cloud applications") {
			@Override
			protected List<CloudApplication> doRun(CloudFoundryOperations client) throws Exception {
				return client.getApplicationsWithBasicInfo();
			}
		}.call();
	}

	public void uploadApplication(final String appName, final ApplicationArchive archive) throws Exception {

		new BasicRequest(this.client, appName, "Uploading application archive") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.uploadApplication(appName, archive);
			}
		}.call();
	}

	public void stopApplication(final String appName) throws Exception {
		new ApplicationRequest<Void>(this.client, appName) {
			@Override
			protected Void doRun(CloudFoundryOperations client) throws Exception {
				client.stopApplication(appName);
				return null;
			}
		}.call();
	}

	public void restartApplication(final String appName) throws Exception {
		new BasicRequest(this.client, appName, "Restarting application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.restartApplication(appName);
			}
		}.call();
	}

	public void updateApplicationEnvironment(final String appName, final Map<String, String> varsToUpdate)
			throws Exception {
		new BasicRequest(this.client, appName, "Updating application environment variables") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationEnv(appName, varsToUpdate);
			}
		}.call();
	}

	public void updateApplicationStaging(final String appName, final Staging staging) throws Exception {
		new BasicRequest(this.client, appName, "Updating application buildpack") {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationStaging(appName, staging);
			}
		}.call();
	}

	public void updateApplicationServices(final String appName, final List<String> services) throws Exception {

		new BasicRequest(this.client, appName, "Updating application service bindings") {

			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationServices(appName, services);
			}
		}.call();
	}

	public void updateApplicationMemory(final String appName, final int memory) throws Exception {
		new BasicRequest(this.client, appName, "Updating application memory") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationMemory(appName, memory);
			}
		}.call();
	}

	public void updateApplicationInstances(final String appName, final int instances) throws Exception {
		new BasicRequest(this.client, appName, "Updating application instances") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationInstances(appName, instances);
			}
		}.call();
	}

	public void updateApplicationUris(final String appName, final List<String> urls) throws Exception {
		new BasicRequest(this.client, appName, "Updating application URLs") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.updateApplicationUris(appName, urls);
			}
		}.call();
	}

	public void deleteApplication(final String appName) throws Exception {
		new BasicRequest(this.client, appName, "Deleting application") {
			@Override
			protected void runRequest(CloudFoundryOperations client) throws Exception {
				client.deleteApplication(appName);
			}
		}.call();
	}

	public List<CloudDomain> getDomains() throws Exception {
		return new ClientRequest<List<CloudDomain>>(this.client, "Getting Cloud domains") {

			@Override
			protected List<CloudDomain> doRun(CloudFoundryOperations client) throws Exception {
				return client.getDomains();
			}
		}.call();
	}

	public List<CloudService> getServices() throws Exception {
		return new ClientRequest<List<CloudService>>(this.client, "Getting Cloud Services") {

			@Override
			protected List<CloudService> doRun(CloudFoundryOperations client) throws Exception {
				return client.getServices();
			}
		}.call();
	}


	public List<CloudSpace> getSpaces() throws Exception {
		return new ClientRequest<List<CloudSpace>>(this.client, "Getting Cloud spaces") {
			@Override
			protected List<CloudSpace> doRun(CloudFoundryOperations client) throws Exception {
				return client.getSpaces();
			}
		}.call();
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
		return new ClientRequest<CloudAppInstances>(this.client, "Getting application instances") {
			@Override
			protected CloudAppInstances doRun(CloudFoundryOperations client) throws Exception {
				CloudApplication app = getApplication(guid);
				if (app != null) {
					ApplicationStats stats = getApplicationStats(app.getName());
					return new CloudAppInstances(app, stats);
				}
				return null;
			}
		}.call();
	}

	public CloudAppInstances getExistingAppInstances(final String appName) throws Exception {
		return new ClientRequest<CloudAppInstances>(this.client, appName, "Getting application instances", null) {
			@Override
			protected CloudAppInstances doRun(CloudFoundryOperations client) throws Exception {
				CloudApplication app = getApplication(appName);
				if (app != null) {
					ApplicationStats stats = getApplicationStats(appName);
					return new CloudAppInstances(app, stats);
				}
				return null;

			}
		}.call();
	}

	public Version getApiVersion() {
		CloudInfo info = client.getCloudInfo();
		if (info!=null) {
			String versionString = info.getApiVersion();
			if (versionString!=null) {
				return new Version(versionString);
			}
		}
		return null;
	}

	public HealthCheckSupport getHealthCheckSupport() throws Exception {
		return new HealthCheckSupport(client, getCloudInfoV2(), targetProperties.isSelfsigned(), getProxyConf());
	}

	public SshClientSupport getSshClientSupport() throws Exception {
		HttpProxyConfiguration proxyConf = getProxyConf();
		return new SshClientSupport(client, getCloudInfoV2(), targetProperties.isSelfsigned(), proxyConf);
	}

	public BuildpackSupport getBuildpackSupport() throws Exception {
		CloudCredentials creds = new CloudCredentials(targetProperties.getUsername(),
				targetProperties.getPassword());
		HttpProxyConfiguration proxyConf = getProxyConf();
		return BuildpackSupport.create(client, creds, proxyConf,
				targetProperties.isSelfsigned());
	}

	public StreamingLogToken streamLogs(String appName, ApplicationLogConsole logConsole) {
		return client.streamLogs(appName, logConsole);
	}
}
