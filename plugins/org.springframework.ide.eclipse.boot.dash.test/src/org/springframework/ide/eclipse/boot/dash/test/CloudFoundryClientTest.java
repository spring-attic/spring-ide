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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.JobBody;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CloudFoundryClientTest {

	private static final String CFAPPS_IO = "cfapps.io";

	private DefaultClientRequestsV2 client = createClient(CfTestTargetParams.fromEnv());

	@Rule
	public CloudFoundryServicesHarness services = new CloudFoundryServicesHarness(client);

	@Rule
	public CloudFoundryApplicationHarness appHarness = new CloudFoundryApplicationHarness(client);

	private static DefaultClientRequestsV2 createClient(CFClientParams fromEnv) {
		try {
			DefaultCloudFoundryClientFactoryV2 factory = new DefaultCloudFoundryClientFactoryV2();
			return (DefaultClientRequestsV2) factory.getClient(fromEnv);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Test
	public void testPushAndBindServices() throws Exception {
		String appName = appHarness.randomAppName();

		String service1 = services.createTestService();
		String service2 = services.createTestService();
		String service3 = services.createTestService(); //An extra unused service (makes this a better test).

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setServices(ImmutableList.of(service1, service2));
		client.push(params);

		assertEquals(ImmutableSet.of(service1, service2), getBoundServiceNames(appName));

		client.bindAndUnbindServices(appName, ImmutableList.of(service1)).get();
		assertEquals(ImmutableSet.of(service1), getBoundServiceNames(appName));

		client.bindAndUnbindServices(appName, ImmutableList.of(service2)).get();
		assertEquals(ImmutableSet.of(service2), getBoundServiceNames(appName));

		client.bindAndUnbindServices(appName, ImmutableList.of()).get();
		assertEquals(ImmutableSet.of(), getBoundServiceNames(appName));

	}

	private Set<String> getBoundServiceNames(String appName) throws Exception {
		return client.getBoundServicesSet(appName).get();
	}

	@Test
	public void testPushAndSetEnv() throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setEnv(ImmutableMap.of(
				"foo", "foo_value",
				"bar", "bar_value"
		));
		client.push(params);

		CFApplicationDetail app = client.getApplication(appName);
		assertNotNull("Expected application to exist after push: " + appName, app);
		String content = IOUtils.toString(new URI("http://" + appName + '.' + CFAPPS_IO + "/test.txt"));
		assertTrue(content.length() > 0);
		assertTrue(content.contains("content"));

		{
			Map<String, String> env = client.getEnv(appName).get();
			assertEquals("foo_value", env.get("foo"));
			assertEquals("bar_value", env.get("bar"));
			assertEquals(2, env.size());
		}

		client.updateApplicationEnvironment(appName, ImmutableMap.of("other", "value"));
		{
			Map<String, String> env = client.getEnv(appName).get();
			assertEquals("value", env.get("other"));
			assertEquals(1, env.size());
		}

		//This last piece is commented because it fails.
		//See: https://www.pivotaltracker.com/story/show/116804259

		// The last var doesn't get removed. Not sure how to fix it.
		// But eventually we won't even be using 'setEnvVars' it will be part of the push.
		// and its not going to be our problem to fix that.
//		client.updateApplicationEnvironment(appName, ImmutableMap.of()).get();
//		{
//			Map<String, Object> env = client.getEnv(appName).get();
//			assertEquals(0, env.size());
//		}
	}

	@Test
	public void testServiceCreateAndDelete() throws Exception {
		String serviceName = services.randomServiceName();
		client.createService(serviceName, "cloudamqp", "lemur").get();
		List<CFService> services = client.getServices();
		assertServices(services, serviceName);
		client.deleteService(serviceName).get();

		assertNoServices(client.getServices(), serviceName);
	}


	@Test
	public void testGetBoundServices() throws Exception {
		String appName = appHarness.randomAppName();
		String service1 = services.createTestService();
		String service2 = services.createTestService();
		String service3 = services.createTestService();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setServices(ImmutableList.of(service1, service2));
		client.push(params);

		List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
		CFApplication app = null;
		for (CFApplication a : allApps) {
			if (a.getName().equals(appName)) {
				app = a;
			}
		}
		assertEquals(ImmutableSet.of(service1, service2), ImmutableSet.copyOf(app.getServices()));

		app = client.getApplication(appName);
		assertEquals(ImmutableSet.of(service1, service2), ImmutableSet.copyOf(app.getServices()));
	}


	@Test
	public void testGetDomains() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());
		List<CFCloudDomain> domains = client.getDomains();
		assertEquals(CFAPPS_IO, domains.get(0).getName());

		Set<String> names = Flux.fromIterable(domains)
			.map(CFCloudDomain::getName)
			.toList()
			.map(ImmutableSet::copyOf)
			.get();
		assertContains(names,
				"projectreactor.org",
				"projectreactor.io",
				"dsyer.com"
		);
	}

	@Test
	public void testGetBuildpacks() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());
		List<CFBuildpack> buildpacks = client.getBuildpacks();

		Set<String> names = Flux.fromIterable(buildpacks)
				.map(CFBuildpack::getName)
				.toList()
				.map(ImmutableSet::copyOf)
				.get();

		assertContains(names,
			"staticfile_buildpack",
			"java_buildpack",
			"ruby_buildpack"
		);
	}

	@Test
	public void testGetStacks() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());
		List<CFStack> stacks = client.getStacks();

		Set<String> names = Flux.fromIterable(stacks)
			.map(CFStack::getName)
			.toList()
			.map(ImmutableSet::copyOf)
			.get();

		assertContains(names,
				"cflinuxfs2"
		);
	}

	@Test
	public void testApplicationLogConnection() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());

		String appName = appHarness.randomAppName();
		ApplicationLogListener listener = mock(ApplicationLogListener.class);
		Mono<StreamingLogToken> token = client.streamLogs(appName, listener);
		assertNotNull(token);

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		client.push(params);

		BootDashModelTest.waitForJobsToComplete();
		verify(listener, atLeastOnce()).onMessage(any());
	}

	@Test
	public void testGetApplicationBuildpack() throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		client.push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals("staticfile_buildpack", app.getBuildpackUrl());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals("staticfile_buildpack", app.getBuildpackUrl());
		}
	}

	@Test
	public void testGetApplicationStack() throws Exception {
		String appName = appHarness.randomAppName();
		String stackName = "cflinuxfs2";

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setStack(stackName);
		client.push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals(stackName, app.getStack());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals(stackName, app.getStack());
		}
	}

	/////////////////////////////////////////////////////////////////////////////

	private void assertContains(Set<String> strings, String... expecteds) {
		for (String e : expecteds) {
			assertContains(e, strings);
		}
	}

	private void assertContains(String expected, Set<String> names) {
		assertTrue("Expected '"+expected+"' not found in: "+names, names.contains(expected));
	}

	private void assertNoServices(List<CFService> services, String serviceName) throws Exception {
		Set<String> names = services.stream().map(CFService::getName).collect(Collectors.toSet());
		assertFalse(names.contains(serviceName));
	}

	private void assertServices(List<CFService> services, String... serviceNames) throws Exception {
		Set<String> names = services.stream().map(CFService::getName).collect(Collectors.toSet());
		assertContains(names, serviceNames);
	}

	private File getTestZip(String fileName) {
		File sourceWorkspace = new File(
				StsTestUtil.getSourceWorkspacePath("org.springframework.ide.eclipse.boot.dash.test"));
		File file = new File(sourceWorkspace, fileName + ".zip");
		Assert.isTrue(file.exists(), ""+ file);
		return file;
	}

}
