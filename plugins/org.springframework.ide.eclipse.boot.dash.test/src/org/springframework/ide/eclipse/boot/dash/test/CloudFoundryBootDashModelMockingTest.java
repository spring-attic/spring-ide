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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Kris De Volder
 */
public class CloudFoundryBootDashModelMockingTest {

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
//		CloudFoundryOperations client = clientFactory.client;

		CloudFoundryBootDashModel target =  harness.createCfTarget(CfTestTargetParams.fromEnv());
		assertNotNull(target);
		assertNotNull(target.getRunTarget().getTargetProperties().getPassword());
		assertEquals(1, harness.getCfRunTargetModels().size());
	}

//	/**
//	 * Test that tests a bunch of things.
//	 * TODO: It isn't good practice to create 'test everything' tests...
//	 * but we do it anyway because ramping up a test that deploys an app takes about 90 seconds...
//	 * Maybe we can factor this better somehow so we have separate tests, but only deploy app once?
//	 */
//	@Test
//	public void testDeployAppAndDeleteAndStuff() throws Exception {
//		harness.createCfTarget(CfTestTargetParams.fromEnv());
//		final CloudFoundryBootDashModel model = harness.getCfTargetModel();
//
//		final BootProjectDashElement project = harness.getElementFor(
//				projects.createBootProject("to-deploy", withStarters("actuator", "web"))
//		);
//		final String appName = harness.randomAppName();
//
//		harness.answerDeploymentPrompt(ui, appName, appName);
//		model.add(ImmutableList.<Object>of(project), ui);
//
//		//The resulting deploy is asynchronous
//		new ACondition("wait for app '"+ appName +"'to appear", APP_IS_VISIBLE_TIMEOUT) {
//			public boolean test() throws Exception {
//				assertNotNull(model.getApplication(appName));
//				return true;
//			}
//		};
//
//		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
//			public boolean test() throws Exception {
//				CloudAppDashElement element = model.getApplication(appName);
//				assertEquals(RunState.RUNNING, element.getRunState());
//				return true;
//			}
//		};
//
//		//Try to get request mappings
//		new ACondition("wait for request mappings", FETCH_REQUEST_MAPPINGS_TIMEOUT) {
//			public boolean test() throws Exception {
//				CloudAppDashElement element = model.getApplication(appName);
//				List<RequestMapping> mappings = element.getLiveRequestMappings();
//				assertNotNull(mappings); //Why is the test sometimes failing here?
//				assertTrue(!mappings.isEmpty()); //Even though this is an 'empty' app should have some mappings,
//				                                 // for example an 'error' page.
//				return true;
//			}
//		};
//
//		//Try to delete the app...
//		reset(ui);
//		when(ui.confirmOperation(eq("Deleting Elements"), anyString())).thenReturn(true);
//
//		CloudAppDashElement app = model.getApplication(appName);
//		app.getCloudModel().delete(ImmutableList.<BootDashElement>of(app), ui);
//
//		new ACondition("wait for app to be deleted", APP_DELETE_TIMEOUT) {
//
//			@Override
//			public boolean test() throws Exception {
//				assertNull(model.getApplication(appName));
//				return true;
//			}
//		};
//	}

	///////////////////////////////////////////////////////////////////////////////////


}
