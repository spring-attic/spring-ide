/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.ApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.BuildpackSupport.Buildpack;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import com.google.common.collect.ImmutableList;

public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {

	private Map<String, CFOrganization> orgsByName = new HashMap<>();
	private Map<String, MockCFSpace> spacesByName = new HashMap<>();

	/**
	 * Becomes non-null if notImplementedStub is called, used to check that the tests
	 * only use parts of the mocking harness that are actually implemented.
	 */
	private Exception notImplementedStubCalled = null;

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return new MockClient(params);
	}

	public MockCFSpace defSpace(String orgName, String spaceName) {
		String key = orgName+"/"+spaceName;
		MockCFSpace existing = spacesByName.get(key);
		if (existing==null) {
			CFOrganization org = defOrg(orgName);
			spacesByName.put(key, existing= new MockCFSpace(
					spaceName,
					UUID.randomUUID(),
					org
			));
		}
		return existing;
	}

	public CFOrganization defOrg(String orgName) {
		CFOrganization existing = orgsByName.get(orgName);
		if (existing==null) {
			orgsByName.put(orgName, existing = new CFOrganizationData(
					orgName,
					UUID.randomUUID()
			));
		}
		return existing;
	}

	public void assertOnlyImplementedStubsCalled() throws Exception {
		if (notImplementedStubCalled!=null) {
			throw notImplementedStubCalled;
		}
	}

	private class MockClient implements ClientRequests {

		private CFClientParams params;

		public MockClient(CFClientParams params) {
			this.params = params;
		}

		private void notImplementedStub() {
			IllegalStateException e = new IllegalStateException("CF Client Stub Not Yet Implemented");
			if (notImplementedStubCalled==null) {
				notImplementedStubCalled = e;
			}
			throw e;
		}

		@Override
		public Map<CloudApplication, ApplicationStats> waitForApplicationStats(List<CloudApplication> appsToLookUp,
				long timeToWait) throws Exception {
			notImplementedStub();
			return null;
		}


		@Override
		public void uploadApplication(String appName, ApplicationArchive archive) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationUris(String appName, List<String> urls) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationStaging(String appName, Staging staging) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationServices(String appName, List<String> services) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationMemory(String appName, int memory) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationInstances(String appName, int instances) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables)
				throws Exception {
			notImplementedStub();

		}

		@Override
		public StreamingLogToken streamLogs(String appName, ApplicationLogConsole logConsole) {
			notImplementedStub();
			return null;
		}

		@Override
		public void stopApplication(String appName) throws Exception {
			notImplementedStub();

		}

		@Override
		public void restartApplication(String appName) throws Exception {
			notImplementedStub();

		}

		@Override
		public void logout() {
			notImplementedStub();

		}

		@Override
		public SshClientSupport getSshClientSupport() throws Exception {
			notImplementedStub();
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<CFSpace> getSpaces() throws Exception {
			@SuppressWarnings("rawtypes")
			List hack = ImmutableList.copyOf(spacesByName.values());
			return hack;
		}

		@Override
		public List<CFService> getServices() throws Exception {
			return getSpace().getServices();
		}

		private MockCFSpace getSpace() {
			return spacesByName.get(params.getOrgName()+"/"+params.getSpaceName());
		}
		@Override
		public HealthCheckSupport getHealthCheckSupport() throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public CloudAppInstances getExistingAppInstances(UUID guid) throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public CloudAppInstances getExistingAppInstances(String appName) throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public List<CloudDomain> getDomains() throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public List<Buildpack> getBuildpacks() throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public List<CloudApplication> getApplicationsWithBasicInfo() throws Exception {
			return null;
		}

		@Override
		public CloudApplication getApplication(String appName) throws Exception {
			notImplementedStub();
			return null;
		}

		@Override
		public Version getApiVersion() {
			notImplementedStub();
			return null;
		}

		@Override
		public void deleteApplication(String name) throws Exception {
			notImplementedStub();

		}

		@Override
		public void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
			notImplementedStub();

		}
	};

}
