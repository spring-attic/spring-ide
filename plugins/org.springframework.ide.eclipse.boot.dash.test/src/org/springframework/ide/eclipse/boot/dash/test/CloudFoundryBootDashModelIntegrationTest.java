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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DELETE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DEPLOY_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_IS_VISIBLE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.FETCH_REQUEST_MAPPINGS_TIMEOUT;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class CloudFoundryBootDashModelIntegrationTest {

	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private CloudFoundryTestHarness harness;

	////////////////////////////////////////////////////////////

	@Rule
	public AutobuildingEnablement disableAutoBuild = new AutobuildingEnablement(false);

	@Rule
	public TestBracketter testBracketter = new TestBracketter();

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
		harness.dispose();
	}

	////////////////////////////////////////////////////////////

	@Test
	public void testCreateCfTarget() throws Exception {
		CloudFoundryBootDashModel target =  harness.createCfTarget(CfTestTargetParams.fromEnv());
		assertNotNull(target);
		assertNotNull(target.getRunTarget().getTargetProperties().getPassword());
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
		final String appName = harness.randomAppName();

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

	@Test
	public void testPreexistingApplicationInModel() throws Exception {
		// Create external client and deploy app "externally"
		CloudFoundryOperations externalClient = harness.createExternalClient(CfTestTargetParams.fromEnv());

		List<CloudDomain> domains = externalClient.getDomains();

		List<String> services = new ArrayList<String>();
		int memory = 1024;
		final String preexistingAppName = harness.randomAppName();
		externalClient.createApplication(preexistingAppName, new Staging(), memory,
				ImmutableList.of(preexistingAppName + "." + domains.get(0).getName()), services);

		// Create the boot dash target and model
		harness.createCfTarget(CfTestTargetParams.fromEnv());

		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		final BootProjectDashElement project = harness
				.getElementFor(projects.createBootWebProject("testPreexistingApplicationInModel"));
		final String newAppName = harness.randomAppName();

		// Create a new one too
		harness.answerDeploymentPrompt(ui, newAppName, newAppName);

		model.add(ImmutableList.<Object> of(project), ui);

		// The resulting deploy is asynchronous
		new ACondition("wait for apps '" + newAppName + "' and '" + preexistingAppName + "' to appear",
				APP_IS_VISIBLE_TIMEOUT) {
			public boolean test() throws Exception {
				assertNotNull(model.getApplication(newAppName));
				assertNotNull(model.getApplication(preexistingAppName));

				// check project mapping
				assertEquals("Expected new element in model to have workspace project mapping",
						model.getApplication(newAppName).getProject(), project.getProject());

				// No project mapping for the "external" app
				assertNull(model.getApplication(preexistingAppName).getProject());

				// check the actual CloudApplication
				CloudApplication actualNewApp = model.getApplication(newAppName).getCloudModel().getAppCache()
						.getApp(newAppName);
				assertEquals("No CloudApplication mapping found", actualNewApp.getName(), newAppName);

				CloudApplication actualPreexistingApp = model.getApplication(preexistingAppName).getCloudModel()
						.getAppCache().getApp(preexistingAppName);
				assertEquals("No CloudApplication mapping found", actualPreexistingApp.getName(), preexistingAppName);

				return true;
			}
		};

	}

	///////////////////////////////////////////////////////////////////////////////////


}
