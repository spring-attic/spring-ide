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
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.ApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.BuildpackSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.BuildpackSupport.Buildpack;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.CloudInfoV2;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import static org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFWrapping.*;

public class DefaultClientRequests implements ClientRequests {

	protected final CloudFoundryOperations client;
	private CloudInfoV2 cachedCloudInfo;
	private CFClientParams clientParams;

	public DefaultClientRequests(CloudFoundryOperations client, CFClientParams params) {
		Assert.isNotNull(client, "ClientRequests needs a non-null client");
		this.client = client;
		this.clientParams = params;
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
						deploymentProperties.getUrls(), deploymentProperties.getServices());
			}
		}.call();
	}

	protected HttpProxyConfiguration getProxyConf() {
		return clientParams.getProxyConf();
	}

	private CloudInfoV2 getCloudInfoV2() throws Exception {
		//cached cloudInfo as it doesn't really change and is more like a bunch of static info about how a target is configured.
		if (this.cachedCloudInfo==null) {
			CloudCredentials creds = new CloudCredentials(clientParams.getUsername(), clientParams.getPassword());
			HttpProxyConfiguration proxyConf = getProxyConf();
			this.cachedCloudInfo = new CloudInfoV2(creds, client.getCloudControllerUrl(), proxyConf, clientParams.isSelfsigned());
		}
		return this.cachedCloudInfo;
	}

	public CFApplication getApplication(final String appName) throws Exception {

		return new ApplicationRequest<CFApplication>(this.client, appName) {
			@Override
			protected CFApplication doRun(CloudFoundryOperations client) throws Exception {
				try {
					return wrap(client.getApplication(appName));
				} catch (Exception e) {
					if (CloudErrors.is503Error(e)) {
						// Alternate way to fetch applications that does not
						// fetch instances and
						// may not throw 503 due to fetching stats on app
						// instances if app is not running
						List<CloudApplication> apps =client.getApplicationsWithBasicInfo();
						if (apps != null) {
							for (CloudApplication app : apps) {
								if (app.getName().equals(appName)) {
									return wrap(app);
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

	public CFApplication getApplication(final UUID appUUID) throws Exception {

		return new ApplicationRequest<CFApplication>(this.client, appUUID.toString()) {
			@Override
			protected CFApplication doRun(CloudFoundryOperations client) throws Exception {
				try {
					return wrap(client.getApplication(appUUID));
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
									return wrap(app);
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

	public Map<CFApplication, ApplicationStats> waitForApplicationStats(final List<CFApplication> appsToLookUp,
			final long timeout) throws Exception {
		Callable<Map<CFApplication, ApplicationStats>> task = new AllApplicationInstancesRequest(this.client,
				appsToLookUp);
		return RetryUtil.retry(2000, timeout, task);
	}

	public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
		return new ClientRequest<List<CFApplication>>(this.client, "Getting all Cloud applications") {
			@Override
			protected List<CFApplication> doRun(CloudFoundryOperations client) throws Exception {
				return wrapApps(client.getApplicationsWithBasicInfo());
			}
		}.call();
	}

	@Override
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

	public List<CFService> getServices() throws Exception {
		return new ClientRequest<List<CFService>>(this.client, "Getting Cloud Services") {

			@Override
			protected List<CFService> doRun(CloudFoundryOperations client) throws Exception {
				return CFWrapping.wrapServices(client.getServices());
			}
		}.call();
	}


	public List<CFSpace> getSpaces() throws Exception {
		return new ClientRequest<List<CFSpace>>(this.client, "Getting Cloud spaces") {
			@Override
			protected List<CFSpace> doRun(CloudFoundryOperations client) throws Exception {
				return CFWrapping.wrapSpaces(client.getSpaces());
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
				CFApplication app = getApplication(guid);
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
				CFApplication app = getApplication(appName);
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
		return new HealthCheckSupport(client, getCloudInfoV2(), clientParams.isSelfsigned(), getProxyConf());
	}

	public SshClientSupport getSshClientSupport() throws Exception {
		HttpProxyConfiguration proxyConf = getProxyConf();
		return new SshClientSupport(client, getCloudInfoV2(), clientParams.isSelfsigned(), proxyConf);
	}

	private BuildpackSupport getBuildpackSupport() throws Exception {
		CloudCredentials creds = new CloudCredentials(clientParams.getUsername(),
				clientParams.getPassword());
		HttpProxyConfiguration proxyConf = getProxyConf();
		return BuildpackSupport.create(client, creds, proxyConf,
				clientParams.isSelfsigned());
	}

	public StreamingLogToken streamLogs(String appName, ApplicationLogConsole logConsole) {
		return client.streamLogs(appName, logConsole);
	}

	@Override
	public List<Buildpack> getBuildpacks() throws Exception {
		return getBuildpackSupport().getBuildpacks();
	}

	@Override
	public String getHealthCheck(UUID appGuid) {
		try {
			return getHealthCheckSupport().getHealthCheck(appGuid);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	@Override
	public void setHealthCheck(UUID guid, String hcType) {
		try {
			getHealthCheckSupport().setHealthCheck(guid, hcType);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}
}
