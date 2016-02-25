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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.ide.eclipse.boot.dash.test.BootDashModelTest.waitForJobsToComplete;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFSpace;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * @author Kris De Volder
 */
public class CloudFoundryBootDashModelMockingTest {

	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private UserInteractions ui;
	private MockCloudFoundryClientFactory clientFactory;
	private CloudFoundryTestHarness harness;
	private BootDashActions actions;


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
		this.clientFactory = new MockCloudFoundryClientFactory();
		this.harness = CloudFoundryTestHarness.create(context, clientFactory);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
		this.actions = new BootDashActions(harness.model, harness.selection.forReading(), harness.sectionSelection, ui);
	}

	@After
	public void tearDown() throws Exception {
		waitForJobsToComplete();
		clientFactory.assertOnlyImplementedStubsCalled();
		harness.dispose();
	}

	////////////////////////////////////////////////////////////

	@Test
	public void testCreateCfTarget() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel target =  harness.createCfTarget(targetParams);

		assertNotNull(target);
		assertNotNull(target.getRunTarget().getTargetProperties().getPassword());
		assertEquals(1, harness.getCfRunTargetModels().size());
	}

	@Test
	public void testAppsShownInBootDash() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		space.defApp("foo");
		space.defApp("bar");

		final CloudFoundryBootDashModel target =  harness.createCfTarget(targetParams);

		new ACondition("wait for apps to appear", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				assertEquals(ImmutableSet.of("foo", "bar"), appNames);
				return true;
			}
		};
	}

	@Test
	public void testAppsAndServicesShownInBootDash() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		space.defApp("foo");
		space.defApp("bar");
		space.defService("a-sql");
		space.defService("z-rabbit");

		final CloudFoundryBootDashModel target =  harness.createCfTarget(targetParams);
		assertTrue(target.isConnected());

		debugListener("applications", target.getApplications());
		debugListener("services", target.getServices());
		debugListener("all", target.getElements());

		new ACondition("wait for elements to appear", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				ImmutableSet<String> serviceNames = getNames(target.getServices().getValues());
 				ImmutableSet<String> allNames = getNames(target.getElements().getValues());
 				assertEquals(ImmutableSet.of("foo", "bar"), appNames);
 				assertEquals(ImmutableSet.of("a-sql", "z-rabbit"), serviceNames);
 				assertEquals(ImmutableSet.of("foo", "bar", "a-sql", "z-rabbit"), allNames);
				return true;
			}
		};

		//Also test we sort this stuff in the right order.

		ArrayList<BootDashElement> elements = new ArrayList<>(target.getElements().getValues());
		Collections.sort(elements, target.getElementComparator());
		assertNames(elements,
				//first apps... alphabetic
				"bar",
				"foo",
				//then services... alphabetic
				"a-sql",
				"z-rabbit"
		);

		//For https://www.pivotaltracker.com/story/show/114408475
		// Apps and services should disappear when target is disconnected

		IAction toggleConnection = actions.getToggleTargetConnectionAction();
		harness.sectionSelection.setValue(target);
		toggleConnection.run();

		new ACondition("wait for elements to disappear", 10000) {
			@Override
			public boolean test() throws Exception {
				assertFalse(target.isConnected());
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				ImmutableSet<String> serviceNames = getNames(target.getServices().getValues());
 				ImmutableSet<String> allNames = getNames(target.getElements().getValues());
 				assertEquals(ImmutableSet.of(), appNames);
 				assertEquals(ImmutableSet.of(), serviceNames);
 				assertEquals(ImmutableSet.of(), allNames);
				return true;
			}
		};

	}

	private <T extends BootDashElement> void debugListener(final String name, ObservableSet<T> set) {
		set.addListener(new ValueListener<ImmutableSet<T>>() {

			@Override
			public void gotValue(LiveExpression<ImmutableSet<T>> exp, ImmutableSet<T> value) {
				StringBuilder elements = new StringBuilder();
				for (BootDashElement e : exp.getValue()) {
					elements.append(e.getName());
					elements.append(" ");
				}
				System.out.println(name+" -> "+elements);
			}
		});
	}

	private void assertNames(ArrayList<BootDashElement> elements, String... expectNames) {
		String[] actualNames = new String[elements.size()];
		for (int i = 0; i < actualNames.length; i++) {
			actualNames[i] = elements.get(i).getName();
		}
		assertArrayEquals(expectNames, actualNames);
	}

	private ImmutableSet<String> getNames(ImmutableSet<? extends BootDashElement> values) {
		Builder<String> builder = ImmutableSet.builder();
		for (BootDashElement e : values) {
			builder.add(e.getName());
		}
		return builder.build();
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
