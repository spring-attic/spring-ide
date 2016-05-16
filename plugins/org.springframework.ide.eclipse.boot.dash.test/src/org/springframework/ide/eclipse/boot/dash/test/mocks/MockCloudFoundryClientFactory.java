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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.CloudFoundryClient;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import reactor.core.flow.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {

	private Map<String, CFOrganization> orgsByName = new LinkedHashMap<>();
	private Map<String, MockCFSpace> spacesByName = new LinkedHashMap<>();
	private Map<String, MockCFDomain> domainsByName = new LinkedHashMap<>();
	private Map<String, MockCFBuildpack> buildpacksByName = new LinkedHashMap<>();
	private Map<String, MockCFStack> stacksByName = new LinkedHashMap<>();

	/**
	 * Becomes non-null if notImplementedStub is called, used to check that the tests
	 * only use parts of the mocking harness that are actually implemented.
	 */
	private Exception notImplementedStubCalled = null;
	private long startDelay = 0;

	public MockCloudFoundryClientFactory() {
		defDomain("cfmockapps.io"); //Lost of functionality may assume there's at least one domain so make sure we have one.
		defBuildpacks("java-buildpack", "ruby-buildpack", "funky-buildpack", "another-buildpack");
		defStacks("cflinuxfs2", "windows2012R2");
	}

	public void defStacks(String... names) {
		for (String n : names) {
			defStack(n);
		}
	}

	public MockCFStack defStack(String name) {
		MockCFStack stack = new MockCFStack(name);
		stacksByName.put(name, stack);
		return stack;
	}

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return new MockClient(params);
	}

	public MockCFDomain defDomain(String name) {
		MockCFDomain it = new MockCFDomain(name);
		domainsByName.put(name, it);
		return it;
	}

	public String getDefaultDomain() {
		return domainsByName.keySet().iterator().next();
	}

	public MockCFSpace defSpace(String orgName, String spaceName) {
		String key = orgName+"/"+spaceName;
		MockCFSpace existing = spacesByName.get(key);
		if (existing==null) {
			CFOrganization org = defOrg(orgName);
			spacesByName.put(key, existing= new MockCFSpace(this,
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
		private boolean connected = true;
		private Version apiVersion = new Version(CloudFoundryClient.SUPPORTED_API_VERSION);
		private Version supportedApiVersion = apiVersion;

		public MockClient(CFClientParams params) throws Exception {
			checkCredentials(params.getUsername(), params.getPassword());
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
		public Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception {
			MockCFSpace space = getSpace();
			return Flux.fromIterable(appsToLookUp)
			.flatMap((app) -> {
				return Mono.justOrEmpty(space.getApplication(app.getGuid()).getDetailedInfo());
			});
		}

		@Override
		public Cancellation streamLogs(String appName, IApplicationLogConsole logConsole) {
			//TODO: This 'log streamer' is a total dummy for now. It doesn't stream any data and canceling it does nothing.
           return Flux.empty().subscribe();
		}

		@Override
		public void stopApplication(String appName) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.stop();
		}

		@Override
		public void restartApplication(String appName, CancelationToken cancelationToken) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.restart(cancelationToken);
		}

		@Override
		public void logout() {
			connected = false;
		}

		@Override
		public SshClientSupport getSshClientSupport() throws Exception {
			notImplementedStub();
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<CFSpace> getSpaces() throws Exception {
			checkConnected();
			@SuppressWarnings("rawtypes")
			List hack = ImmutableList.copyOf(spacesByName.values());
			return hack;
		}

		@Override
		public List<CFServiceInstance> getServices() throws Exception {
			checkConnected();
			return getSpace().getServices();
		}

		private MockCFSpace getSpace() throws IOException {
			checkConnected();
			if (params.getOrgName()==null) {
				throw errorNoOrgSelected();
			}
			if (params.getSpaceName()==null) {
				throw errorNoSpaceSelected();
			}
			MockCFSpace space = spacesByName.get(params.getOrgName()+"/"+params.getSpaceName());
			if (space==null) {
				throw errorSpaceNotFound(params.getOrgName()+"/"+params.getSpaceName());
			}
			return space;
		}

		@Override
		public List<CFCloudDomain> getDomains() throws Exception {
			checkConnected();
			return ImmutableList.<CFCloudDomain>copyOf(domainsByName.values());
		}

		@Override
		public List<CFBuildpack> getBuildpacks() throws Exception {
			checkConnected();
			return ImmutableList.<CFBuildpack>copyOf(buildpacksByName.values());
		}

		@Override
		public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
			checkConnected();
			return getSpace().getApplicationsWithBasicInfo();
		}

		@Override
		public CFApplicationDetail getApplication(String appName) throws Exception {
			checkConnected();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app!=null) {
				return app.getDetailedInfo();
			}
			return null;
		}

		@Override
		public Version getApiVersion() {
			return apiVersion;
		}

		@Override
		public Version getSupportedApiVersion() {
			return supportedApiVersion;
		}

		@Override
		public void deleteApplication(String name) throws Exception {
			if (!getSpace().removeApp(name)) {
				throw errorAppNotFound(name);
			}
		}

		@Override
		public String getHealthCheck(UUID appGuid) throws IOException {
			checkConnected();
			MockCFApplication app = getApplication(appGuid);
			if (app == null) {
				throw errorAppNotFound("GUID: "+appGuid.toString());
			} else {
				return app.getHealthCheck();
			}
		}

		private MockCFApplication getApplication(UUID appGuid) throws IOException {
			return getSpace().getApplication(appGuid);
		}

		private void checkConnected() throws IOException {
			if (!connected) {
				throw errorClientNotConnected();
			}
		}

		private void checkCredentials(String username, String password) throws Exception {
			if (password.startsWith("wrong")) {
				throw errorInvalidCredentials();
			}
		}

		@Override
		public void setHealthCheck(UUID guid, String hcType) {
			notImplementedStub();
		}

		@Override
		public List<CFStack> getStacks() throws Exception {
			checkConnected();
			return ImmutableList.<CFStack>copyOf(stacksByName.values());
		}

		@Override
		public boolean applicationExists(String appName) throws IOException {
			return getSpace().getApplication(appName) !=null;
		}

		@Override
		public void push(CFPushArguments args, CancelationToken cancelationToken) throws Exception {
			System.out.println("Pushing: "+args);
			//TODO: should check services exist and raise an error because non-existant services cannot be bound.
			MockCFSpace space = getSpace();
			MockCFApplication app = new MockCFApplication(MockCloudFoundryClientFactory.this, space, args.getAppName());
			app.setBuildpackUrlMaybe(args.getBuildpack());
			app.setUris(args.getRoutes());
			app.setCommandMaybe(args.getCommand());
			app.setDiskQuotaMaybe(args.getDiskQuota());
			app.setEnvMaybe(args.getEnv());
			app.setMemoryMaybe(args.getMemory());
			app.setServicesMaybe(args.getServices());
			app.setStackMaybe(args.getStack());
			app.setTimeoutMaybe(args.getTimeout());
			app.setBits(IOUtil.toBytes(args.getApplicationData()));
			space.put(app);
			space.getPushCount(app.getName()).increment();

			app.start(cancelationToken);
		}

		@Override
		public Map<String, String> getApplicationEnvironment(String appName) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			return ImmutableMap.copyOf(app.getEnv());
		}

		@Override
		public void deleteService(String serviceName) throws Exception {
			getSpace().deleteService(serviceName);
		}

	}

	public void defBuildpacks(String... names) {
		for (String n : names) {
			defBuildpack(n);
		}
	}

	public MockCFBuildpack defBuildpack(String n) {
		MockCFBuildpack it = new MockCFBuildpack(n);
		buildpacksByName.put(n, it);
		return it;
	}

	//////////////////////////////////////////////////
	// Exception creation methods

	protected IOException errorAppNotFound(String detailMessage) throws IOException {
		return new IOException("App not found: "+detailMessage);
	}

	protected IOException errorClientNotConnected() {
		return new IOException("CF Client not Connected");
	}

	protected IOException errorNoOrgSelected() {
		return new IOException("No org selected");
	}

	protected IOException errorNoSpaceSelected() {
		return new IOException("No space selected");
	}

	protected  IOException errorSpaceNotFound(String detail) {
		return new IOException("Space not found: "+detail);
	}

	protected IOException errorAppAlreadyExists(String detail) {
		return new IOException("App already exists: "+detail);
	}

	protected Exception errorInvalidCredentials() {
		return new Exception("Cannot connect to CF. Invalid credentials.");
	}

	public void setAppStartDelay(TimeUnit timeUnit, int howMany) {
		startDelay = timeUnit.toMillis(howMany);
	}

	/**
	 * @return The delay that a simulated 'start' of an app should take before returning. Given in milliseconds.
	 */
	public long getStartDelay() {
		return startDelay;
	}

}
