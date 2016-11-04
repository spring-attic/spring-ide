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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DELETE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DEPLOY_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_IS_VISIBLE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.FETCH_REQUEST_MAPPINGS_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.SERVICE_DELETE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import static org.springframework.ide.eclipse.boot.dash.test.BootDashModelTest.waitForJobsToComplete;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudServiceInstanceDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCredentials.CFCredentialType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFSpace;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CloudFoundryBootDashModelIntegrationTest {

	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private CloudFoundryTestHarness harness;

	////////////////////////////////////////////////////////////
	private CFClientParams clientParams = CfTestTargetParams.fromEnv();
	private DefaultClientRequestsV2 client = CloudFoundryClientTest.createClient(clientParams);

	public CloudFoundryApplicationHarness appHarness = new CloudFoundryApplicationHarness(client);

	@Rule
	public AutobuildingEnablement disableAutoBuild = new AutobuildingEnablement(false);

	@Rule
	public TestBracketter testBracketter = new TestBracketter();

	public CloudFoundryServicesHarness services = new CloudFoundryServicesHarness(clientParams, client);

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.harness = CloudFoundryTestHarness.create(context);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
	}

	@After
	public void tearDown() throws Exception {
		appHarness.deleteOwnedApps();
		services.dispose();
		client.logout();
		harness.dispose();
	}

	////////////////////////////////////////////////////////////

	@Test
	public void testCreateCfTarget() throws Exception {
		CloudFoundryBootDashModel target =  harness.createCfTarget(CfTestTargetParams.fromEnv());
		CFCredentials credentials = target.getRunTarget().getTargetProperties().getCredentials();
		assertNotNull(target);
		assertEquals(CFCredentialType.PASSWORD, credentials.getType());
		assertNotNull(credentials.getSecret());
		assertEquals(1, harness.getCfRunTargetModels().size());
	}

	@Test public void testCreateCfTargetAndStoreToken() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		CloudFoundryBootDashModel target =  harness.createCfTarget(targetParams, StoreCredentialsMode.STORE_TOKEN);
		assertNotNull(target);
		assertTrue(target.isConnected());
		CFCredentials credentials = target.getRunTarget().getTargetProperties().getCredentials();
		assertEquals(CFCredentialType.REFRESH_TOKEN, credentials.getType());
		assertNotNull(credentials.getSecret());
		assertEquals(1, harness.getCfRunTargetModels().size());
	}


	/**
	 * Test that tests a bunch of things.
	 * TODO: It isn't good practice to create 'test everything' tests...
	 * but we do it anyway because ramping up a test that deploys an app takes about 90 seconds...
	 * Maybe we can factor this better somehow so we have separate tests, but only deploy app once?
	 */
	@Test
	public void testDeployAppAndDeleteAndStuff() throws Exception {
		harness.createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		final BootProjectDashElement project = harness.getElementFor(
				projects.createBootProject("to-deploy", withStarters("actuator", "web"))
		);
		final String appName = appHarness.randomAppName();

		harness.answerDeploymentPrompt(ui, appName, appName);
		model.add(ImmutableList.<Object>of(project), ui);

		//The resulting deploy is asynchronous
		new ACondition("wait for app '"+ appName +"'to appear", APP_IS_VISIBLE_TIMEOUT) {
			public boolean test() throws Exception {
				assertNotNull(model.getApplication(appName));
				return true;
			}
		};

		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};

		//Try to get request mappings
		new ACondition("wait for request mappings", FETCH_REQUEST_MAPPINGS_TIMEOUT) {
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				List<RequestMapping> mappings = element.getLiveRequestMappings();
				assertNotNull(mappings); //Why is the test sometimes failing here?
				assertTrue(!mappings.isEmpty()); //Even though this is an 'empty' app should have some mappings,
				                                 // for example an 'error' page.
				return true;
			}
		};

		//Try to delete the app...
		reset(ui);
		when(ui.confirmOperation(eq("Deleting Elements"), anyString())).thenReturn(true);

		CloudAppDashElement app = model.getApplication(appName);
		app.getCloudModel().delete(ImmutableList.<BootDashElement>of(app), ui);

		new ACondition("wait for app to be deleted", APP_DELETE_TIMEOUT) {

			@Override
			public boolean test() throws Exception {
				assertNull(model.getApplication(appName));
				return true;
			}
		};
	}

	@Test public void testDeployAppIntoDebugMode() throws Exception {
		harness.createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		final BootProjectDashElement project = harness.getElementFor(
				projects.createBootProject("to-deploy", withStarters("actuator", "web"))
		);
		final String appName = appHarness.randomAppName();

		harness.answerDeploymentPrompt(ui, appName, appName);
		model.performDeployment(ImmutableSet.of(project.getProject()), ui, RunState.DEBUGGING);

		new ACondition("wait for app '"+ appName +"'to be DEBUGGING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.DEBUGGING, element.getRunState());
				return true;
			}
		};

	}

//This test commented because it uses 'createApplication' which no longer exists in V2 client.
//	@Test
//	public void testPreexistingApplicationInModel() throws Exception {
//		// Create external client and deploy app "externally"
//		ClientRequests externalClient = harness.createExternalClient(CfTestTargetParams.fromEnv());
//
//		List<CFCloudDomain> domains = externalClient.getDomains();
//
//		final String preexistingAppName = harness.randomAppName();
//
//		CloudApplicationDeploymentProperties deploymentProperties = new CloudApplicationDeploymentProperties();
//		deploymentProperties.setAppName(preexistingAppName);
//		deploymentProperties.setMemory(1024);
//		deploymentProperties.setUris(ImmutableList.of(preexistingAppName + "." + domains.get(0).getName()));
//		deploymentProperties.setServices(ImmutableList.<String>of());
//		externalClient.createApplication(deploymentProperties);
//
//		// Create the boot dash target and model
//		harness.createCfTarget(CfTestTargetParams.fromEnv());
//
//		final CloudFoundryBootDashModel model = harness.getCfTargetModel();
//
//		final BootProjectDashElement project = harness
//				.getElementFor(projects.createBootWebProject("testPreexistingApplicationInModel"));
//		final String newAppName = harness.randomAppName();
//
//		// Create a new one too
//		harness.answerDeploymentPrompt(ui, newAppName, newAppName);
//
//		model.add(ImmutableList.<Object> of(project), ui);
//
//		// The resulting deploy is asynchronous
//		new ACondition("wait for apps '" + newAppName + "' and '" + preexistingAppName + "' to appear",
//				APP_IS_VISIBLE_TIMEOUT) {
//			public boolean test() throws Exception {
//				assertNotNull(model.getApplication(newAppName));
//				assertNotNull(model.getApplication(preexistingAppName));
//
//				// check project mapping
//				assertEquals("Expected new element in model to have workspace project mapping",
//						model.getApplication(newAppName).getProject(), project.getProject());
//
//				// No project mapping for the "external" app
//				assertNull(model.getApplication(preexistingAppName).getProject());
//
//				// check the actual CloudApplication
//				CFApplication actualNewApp = model.getApplication(newAppName).getSummaryData();
//				assertEquals("No CloudApplication mapping found", actualNewApp.getName(), newAppName);
//
//				CFApplication actualPreexistingApp = model.getApplication(preexistingAppName).getSummaryData();
//				assertEquals("No CloudApplication mapping found", actualPreexistingApp.getName(), preexistingAppName);
//
//				return true;
//			}
//		};
//	}

	@Test
	public void testEnvVarsSetOnFirstDeploy() throws Exception {
		CloudFoundryBootDashModel target = harness.createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = appHarness.randomAppName();

		Map<String, String> env = new HashMap<>();
		env.put("FOO", "something");
		harness.answerDeploymentPrompt(ui, appName, appName, env);

		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};

		Map<String,String> actualEnv = harness.fetchEnvironment(target, appName);

		assertEquals("something", actualEnv.get("FOO"));
	}


	@Test
	public void testServicesBoundOnFirstDeploy() throws Exception {
		CloudFoundryBootDashModel target = harness.createCfTarget(CfTestTargetParams.fromEnv());
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		List<String> bindServices = ImmutableList.of(
				services.createTestService(), services.createTestService()
		);
		ACondition.waitFor("services exist "+bindServices, 30_000, () -> {
			Set<String> services = client.getServices().stream()
			.map(CFServiceInstance::getName)
			.collect(Collectors.toSet());
			System.out.println("services = "+services);
			assertTrue(services.containsAll(bindServices));
		});

		final String appName = appHarness.randomAppName();

		harness.answerDeploymentPrompt(ui, appName, appName, bindServices);

		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};

		Set<String> actualServices = client.getBoundServicesSet(appName).block();

		assertEquals(ImmutableSet.copyOf(bindServices), actualServices);
	}

	@Test public void testDeployManifestWithAbsolutePathAttribute() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		IProject project = projects.createProject("to-deploy");

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		waitForJobsToComplete();

		File zipFile = getTestZip("testapp");
		final String appName = appHarness.randomAppName();
		IFile manifestFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n" +
				"  path: "+zipFile.getAbsolutePath() + "\n" +
				"  buildpack: staticfile_buildpack"
		);
		harness.answerDeploymentPrompt(ui, manifestFile);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		ACondition.waitFor("app to appear", APP_IS_VISIBLE_TIMEOUT, () -> {
			assertNotNull(model.getApplication(appName));
		});

		CloudAppDashElement app = model.getApplication(appName);

		ACondition.waitFor("app to be running", APP_DEPLOY_TIMEOUT, () -> {
			assertEquals(RunState.RUNNING, app.getRunState());
			String url = pathJoin(app.getUrl(),"test.txt");
			assertEquals(url, "some content here\n", IOUtils.toString(new URL(url)));
		});

		verify(ui).promptApplicationDeploymentProperties(any());
		verifyNoMoreInteractions(ui);
	}

	private String pathJoin(String url, String append) {
		while (url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		while (append.startsWith("/")) {
			append = append.substring(1);
		}
		return url+"/"+append;
	}

	@Test public void deleteService() throws Exception {
		String serviceName = services.createTestService();
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		ACondition.waitFor("service to appear", APP_IS_VISIBLE_TIMEOUT, () -> {
			assertNotNull(model.getService(serviceName));
		});

		when(ui.confirmOperation(contains("Deleting"), contains("Are you sure that you want to delete")))
		.thenReturn(true);

		CloudServiceInstanceDashElement service = model.getService(serviceName);
		model.canDelete(service);
		model.delete(ImmutableSet.of(service), ui);

		ACondition.waitFor("service to disapear", SERVICE_DELETE_TIMEOUT, () -> {
			assertNull(model.getService(serviceName));
		});

	}

	///////////////////////////////////////////////////////////////////////////////////

	private File getTestZip(String fileName) {
		File sourceWorkspace = new File(
				StsTestUtil.getSourceWorkspacePath("org.springframework.ide.eclipse.boot.dash.test"));
		File file = new File(sourceWorkspace, fileName + ".zip");
		Assert.isTrue(file.exists(), ""+ file);
		return file;
	}

}
