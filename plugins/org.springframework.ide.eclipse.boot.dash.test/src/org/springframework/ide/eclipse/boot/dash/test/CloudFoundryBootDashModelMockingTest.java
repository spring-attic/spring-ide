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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.ide.eclipse.boot.dash.test.BootDashModelTest.waitForJobsToComplete;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DELETE_TIMEOUT;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DEPLOY_TIMEOUT;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ModelStateListener;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFApplication;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCFSpace;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockCloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.test.mocks.RunStateHistory;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.CustmomizeTargetLabelAction;
import org.springframework.ide.eclipse.boot.dash.views.CustomizeTargetLabelDialogModel;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;
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

	public CloudFoundryApplicationHarness appHarness = new CloudFoundryApplicationHarness(null);

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
		appHarness.dispose();
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
	public void testBasicRefreshApps() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		space.defApp("foo");
		space.defApp("bar");

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);

		waitForApps(target, "foo", "bar");

		space.defApp("anotherfoo");
		space.defApp("anotherbar");
		target.refresh(ui);

		waitForApps(target, "foo", "bar", "anotherfoo", "anotherbar");
	}

	@Test
	public void testRefreshAppsRunState() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		final MockCFApplication foo = space.defApp("foo");
		space.defApp("bar");

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);

		waitForApps(target, "foo", "bar");

		foo.start(CancelationTokens.NULL);

		target.refresh(ui);

		new ACondition("wait for app states", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				assertEquals(ImmutableSet.of("foo", "bar"), appNames);
				CloudAppDashElement appElement = harness.getCfTargetModel().getApplication("foo");
				assertEquals(RunState.RUNNING, appElement.getRunState());

				appElement = harness.getCfTargetModel().getApplication("bar");
				assertEquals(RunState.INACTIVE, appElement.getRunState());

				return true;
			}
		};
	}

	@Test
	public void testRefreshAppsHealthCheck() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		final MockCFApplication foo = space.defApp("foo");


		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);

		waitForApps(target, "foo");

		CloudAppDashElement appElement = harness.getCfTargetModel().getApplication("foo");
		assertEquals(HealthCheckSupport.HC_PORT, appElement.getHealthCheck());


		foo.setHealthCheck(HealthCheckSupport.HC_NONE);

		target.refresh(ui);

		new ACondition("wait for app health check", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				assertEquals(ImmutableSet.of("foo"), appNames);

				CloudAppDashElement appElement = harness.getCfTargetModel().getApplication("foo");
				assertEquals(HealthCheckSupport.HC_NONE, appElement.getHealthCheck());

				return true;
			}
		};
	}

	@Test
	public void testRefreshServices() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();

		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		space.defApp("foo");
		space.defService("elephantsql");
		space.defService("cleardb");

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);

		waitForApps(target, "foo");
		waitForServices(target, "elephantsql", "cleardb");
		waitForElements(target, "foo", "elephantsql", "cleardb");

		space.defService("rabbit");

		target.refresh(ui);
		waitForServices(target, "elephantsql", "cleardb", "rabbit");
		waitForElements(target, "foo", "elephantsql", "cleardb", "rabbit");
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


	@Test
	public void testDeployActionsSorted() throws Exception {
		//Generate some random 'space' names.
		String orgName = "CloudRunAMock";
		String[] spaceNames = new String[6];
		for (int i = 0; i < spaceNames.length; i++) {
			spaceNames[i] = RandomStringUtils.randomAlphabetic(10).toLowerCase();
		}

		//Define the spaces in the 'mock' cloud:
		for (String spaceName : spaceNames) {
			//Since this is just a mock client we creating, the params don't matter all that much at all.
			clientFactory.defSpace(orgName, spaceName);
		}

		//Create targets in the boot dash that connect to these spaces:
		for (String spaceName : spaceNames) {
			CFClientParams params = new CFClientParams(
					"http://api.run.cloud.mock.com",
					"some-user",  "his-password",
					false,
					orgName, spaceName
			);
			harness.createCfTarget(params);
		}

		{
			ImmutableList<IAction> deployActions = actions.getDebugOnTargetActions();
			assertEquals(spaceNames.length, deployActions.size());
			assertSorted(deployActions);
		}

		{
			ImmutableList<IAction> deployActions = actions.getRunOnTargetActions();
			assertEquals(spaceNames.length, deployActions.size());
			assertSorted(deployActions);
		}

	}

	@Test
	public void targetTypeProperties() throws Exception {
		{
			CloudFoundryRunTargetType cfTargetType = harness.getCfTargetType();
			PropertyStoreApi props = cfTargetType.getPersistentProperties();
			props.put("testkey", "testvalue");
			assertEquals("testvalue", props.get("testkey"));
		}

		harness.reload();

		{
			CloudFoundryRunTargetType cfTargetType = harness.getCfTargetType();
			PropertyStoreApi props = cfTargetType.getPersistentProperties();
			assertEquals("testvalue", props.get("testkey"));
		}

	}

	@Test
	public void templateDrivenTargetNames() throws Exception {
		clientFactory.defSpace("my-org", "foo");
		clientFactory.defSpace("your-org", "bar");

		String apiUrl = "http://api.some-cloud.com";
		String username = "freddy"; String password = "whocares";
		AbstractBootDashModel fooSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "my-org", "foo"));
		AbstractBootDashModel barSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "your-org", "bar"));

		//check the default rendering is like it used to be before introducing templates.
		assertEquals("my-org : foo - [http://api.some-cloud.com]", fooSpace.getDisplayName());
		assertEquals("your-org : bar - [http://api.some-cloud.com]", barSpace.getDisplayName());

		RunTargetType targetType = fooSpace.getRunTarget().getType();

		//Let's try switching the order of org and space
		targetType.setNameTemplate("%s - %o @ %a");
		assertEquals("foo - my-org @ http://api.some-cloud.com", fooSpace.getDisplayName());
		assertEquals("bar - your-org @ http://api.some-cloud.com", barSpace.getDisplayName());

		//Let's try adding 'username' into the label
		targetType.setNameTemplate("%u@%s");
		assertEquals("freddy@foo", fooSpace.getDisplayName());
		assertEquals("freddy@bar", barSpace.getDisplayName());
	}

	@Test
	public void customizeTargetLabelAction() throws Exception {
		clientFactory.defSpace("my-org", "foo");
		clientFactory.defSpace("your-org", "bar");

		String apiUrl = "http://api.some-cloud.com";
		String username = "freddy"; String password = "whocares";
		LocalBootDashModel local = harness.getLocalModel();
		AbstractBootDashModel fooSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "my-org", "foo"));
		AbstractBootDashModel barSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "your-org", "bar"));
		CustmomizeTargetLabelAction action = actions.getCustomizeTargetLabelAction();

		//////////// not applicable for local targets:

		harness.sectionSelection.setValue(local);
		assertFalse(action.isEnabled());
		assertFalse(action.isVisible());

		//////////// for cf targets //////////////////////////////////////////////////

		harness.sectionSelection.setValue(fooSpace);
		assertTrue(action.isEnabled());
		assertTrue(action.isVisible());

		ModelStateListener modelStateListener = mock(ModelStateListener.class);
		fooSpace.addModelStateListener(modelStateListener);
		barSpace.addModelStateListener(modelStateListener);

		doAnswer(editSetTemplate("%s - %o @ %a"))
			.when(ui).openEditTemplateDialog(any(EditTemplateDialogModel.class));

		action.run();

		//Changing the template should result in modelStateListener firing on all the affected models
		verify(modelStateListener).stateChanged(same(fooSpace));
		verify(modelStateListener).stateChanged(same(barSpace));

		assertEquals("foo - my-org @ http://api.some-cloud.com", fooSpace.getDisplayName());
		assertEquals("bar - your-org @ http://api.some-cloud.com", barSpace.getDisplayName());

		//Let's also try a user interaction that involves the 'Restore Defaults' button...

		reset(ui, modelStateListener);

		doAnswer(restoreDefaultTemplate())
			.when(ui).openEditTemplateDialog(any(EditTemplateDialogModel.class));

		action.run();

		verify(modelStateListener).stateChanged(same(fooSpace));
		verify(modelStateListener).stateChanged(same(barSpace));

		assertEquals("my-org : foo - [http://api.some-cloud.com]", fooSpace.getDisplayName());
		assertEquals("your-org : bar - [http://api.some-cloud.com]", barSpace.getDisplayName());
	}

	@Test
	public void customizeTargetLabelDialog() throws Exception {
		EditTemplateDialogModel dialog;
		clientFactory.defSpace("my-org", "foo");
		clientFactory.defSpace("your-org", "bar");

		String apiUrl = "http://api.some-cloud.com";
		String username = "freddy"; String password = "whocares";

		AbstractBootDashModel fooSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "my-org", "foo"));
		AbstractBootDashModel barSpace = harness.createCfTarget(new CFClientParams(apiUrl, username, password, false, "your-org", "bar"));

		ModelStateListener modelStateListener = mock(ModelStateListener.class);
		fooSpace.addModelStateListener(modelStateListener);
		barSpace.addModelStateListener(modelStateListener);

		// Check initial state of the dialog when no custom labels have yet been set at all:
		dialog = CustomizeTargetLabelDialogModel.create(fooSpace);

		assertTrue(dialog.applyToAll.getValue());
		assertEquals("%o : %s - [%a]", dialog.template.getValue());

		//Check performOk only changes the one label when 'apply to all' is disabled.
		dialog.applyToAll.setValue(false);
		dialog.template.setValue("CHANGED %s -> %o");
		dialog.performOk();

		verify(modelStateListener).stateChanged(same(fooSpace));
		verify(modelStateListener, never()).stateChanged(same(barSpace));

		assertEquals("CHANGED foo -> my-org", fooSpace.getDisplayName());
		assertEquals("your-org : bar - [http://api.some-cloud.com]", barSpace.getDisplayName());

		//Opening the dialog now should have 'apply to all' disabled to avoid accidentally overwriting
		// existing individually customized labels...
		dialog = CustomizeTargetLabelDialogModel.create(fooSpace);
		assertFalse(dialog.applyToAll.getValue());
		assertEquals("CHANGED %s -> %o", dialog.template.getValue());

		//Also if we open the dialog on the other element!!!
		dialog = CustomizeTargetLabelDialogModel.create(barSpace);
		assertFalse(dialog.applyToAll.getValue());
		assertEquals("%o : %s - [%a]", dialog.template.getValue());

		//Selecting 'apply to all' should set the template on the type and erase custom templates on the
		// individual targets.
		dialog.applyToAll.setValue(true);
		dialog.template.setValue("DIFFERENT %s -> %o");
		dialog.performOk();

		assertEquals("DIFFERENT %s -> %o", harness.getCfTargetType().getNameTemplate());
		for (BootDashModel target : harness.getCfRunTargetModels()) {
			assertFalse(target.hasCustomNameTemplate());
			assertEquals("DIFFERENT %s -> %o", target.getNameTemplate());
		}

		assertEquals("DIFFERENT foo -> my-org", fooSpace.getDisplayName());
		assertEquals("DIFFERENT bar -> your-org", barSpace.getDisplayName());
	}

	@Test
	public void testEnvVarsSetOnFirstDeploy() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel target =  harness.createCfTarget(targetParams);
		final CloudFoundryBootDashModel model = harness.getCfTargetModel();

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = appHarness.randomAppName();

		Map<String, String> env = new HashMap<>();
		env.put("FOO", "something");
		harness.answerDeploymentPrompt(ui, appName, appName, env);

		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		new ACondition("wait for app '"+ appName +"'to be RUNNING", 30000) { //why so long? JDT searching for main type.
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};

		Map<String,String> actualEnv = harness.fetchEnvironment(target, appName);

		assertEquals("something", actualEnv.get("FOO"));
	}

	@Test public void appToProjectBindingsPersisted() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		final IProject project = projects.createBootProject(projectName, withStarters("actuator", "web"));

		harness.createCfTarget(targetParams);
		{
			final CloudFoundryBootDashModel model = harness.getCfTargetModel();

			deployApp(model, appName, project);

			CloudAppDashElement appByProject = getApplication(model, project);
			CloudAppDashElement appByName = model.getApplication(appName);
			assertNotNull(appByProject);
			assertEquals(appByProject, appByName);
		}

		harness.reload();
		{
			final CloudFoundryBootDashModel model = harness.getCfTargetModel();
			waitForApps(model, appName);
			CloudAppDashElement appByName = model.getApplication(appName);
			CloudAppDashElement appByProject = getApplication(model, project);
			assertNotNull(appByProject);
			assertEquals(appByProject, appByName);
		}
	}

	@Test public void appToProjectBindingsPersistedAfterDisconnectConnect() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		final IProject project = projects.createBootProject(projectName, withStarters("actuator", "web"));

		final CloudFoundryBootDashModel model = harness.createCfTarget(targetParams);
		deployApp(model, appName, project);
		assertAppToProjectBinding(model, project, appName);

		IAction toggleConnectionAction = actions.getToggleTargetConnectionAction();
		harness.sectionSelection.setValue(model);

		toggleConnectionAction.run();
		waitForElements(model /*none*/);
		toggleConnectionAction.run();
		waitForElements(model, appName);

		assertAppToProjectBinding(model, project, appName);
	}

	@Test public void appToProjectBindingChangedAfterProjectRename() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		space.defApp(appName);
		final IProject project = projects.createProject(projectName);

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);
		waitForApps(target, appName);
		CloudAppDashElement app = target.getApplication(appName);
		app.setProject(project);

		assertAppToProjectBinding(target, project, appName);


		ElementStateListener elementStateListener = mock(ElementStateListener.class);
		target.addElementStateListener(elementStateListener);

		final IProject newProject = projects.rename(project, projectName+"-RENAMED");
		// resource listeners called synchronously by eclipse so we don't need ACondition

		assertAppToProjectBinding(target, newProject, appName);

		//state change event should have been fired (to update label of element in view)
		verify(elementStateListener).stateChanged(same(app));
	}

	@Test public void appToProjectBindingForgottenAfterDelete() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		space.defApp(appName);
		final IProject project = projects.createProject(projectName);

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);
		waitForApps(target, appName);
		CloudAppDashElement app = target.getApplication(appName);
		app.setProject(project);

		assertAppToProjectBinding(target, project, appName);

		ElementStateListener elementStateListener = mock(ElementStateListener.class);
		target.addElementStateListener(elementStateListener);

		project.delete(true, new NullProgressMonitor());

		assertNull(app.getProject(true));
		verify(elementStateListener).stateChanged(same(app));
	}

	@Test public void runstateBecomesUnknownWhenStartOperationFails() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		MockCFApplication app = space.defApp(appName);
		final IProject project = projects.createProject(projectName);

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);
		waitForApps(target, appName);
		CloudAppDashElement appElement = target.getApplication(appName);
		appElement.setProject(project);

		//The state refressh is asynch so until state becomes 'INACTIVE' it will be unknown.
		waitForState(appElement, RunState.INACTIVE, 3000);
		IAction restartAction = actions.getRestartOnlyApplicationAction();

		RunStateHistory runstateHistory = new RunStateHistory();

		appElement.getBaseRunStateExp().addListener(runstateHistory);
		doThrow(IOException.class).when(app).start(any());

		System.out.println("restarting application...");
		harness.selection.setElements(appElement);
		restartAction.run();

		waitForState(appElement, RunState.UNKNOWN, 3000);

		runstateHistory.assertHistoryContains(
				RunState.INACTIVE,
				RunState.STARTING
		);
		runstateHistory.assertLast(
				RunState.UNKNOWN
		);
	}

	@Test public void refreshClearsErrorState() throws Exception {
		final String appName = "foo";
		String projectName = "to-deploy";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		space.defApp(appName);
		final IProject project = projects.createProject(projectName);

		final CloudFoundryBootDashModel target = harness.createCfTarget(targetParams);
		waitForApps(target, appName);
		CloudAppDashElement appElement = target.getApplication(appName);
		appElement.setProject(project);

		waitForState(appElement, RunState.INACTIVE, 3000);
		//The state refressh is asynch so until state becomes 'INACTIVE' it will be unknown.
		appElement.setError(new IOException("Something bad happened"));
		waitForState(appElement, RunState.UNKNOWN, 3000);

		target.refresh(ui);

		waitForState(appElement, RunState.INACTIVE, 3000);
	}

	@Test public void simpleDeploy() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = appHarness.randomAppName();

		harness.answerDeploymentPrompt(ui, appName, appName);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);

		waitForState(app, RunState.RUNNING, 10000);

		assertEquals((Integer)1, space.getPushCount(appName).getValue());
	}

	@Test public void simpleDeployWithManifest() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = appHarness.randomAppName();

		IFile manifestFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n" +
				"  memory: 512M\n"
		);

		harness.answerDeploymentPrompt(ui, (dialog) -> {
			dialog.okPressed();
		});

		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);

		waitForState(app, RunState.RUNNING, 10000);

		assertEquals((Integer)1, space.getPushCount(appName).getValue());
		assertEquals(manifestFile, app.getDeploymentManifestFile());
		assertEquals(512, (int) app.getMemory());
	}

	@Test public void simpleDeployWithDefaultManualManifest() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = project.getName();

		harness.answerDeploymentPrompt(ui, (dialog) -> {
			dialog.okPressed();
		});

		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);

		waitForState(app, RunState.RUNNING, 10000);

		assertEquals((Integer)1, space.getPushCount(appName).getValue());
		assertNull(app.getDeploymentManifestFile());
		assertEquals(1024, (int) app.getMemory());
		assertEquals(appName, app.getName());
	}

	@Test public void stopCancelsDeploy() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));

		final String appName = appHarness.randomAppName();

		clientFactory.setAppStartDelay(TimeUnit.MINUTES, 2);
		harness.answerDeploymentPrompt(ui, appName, appName);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);

		waitForState(app, RunState.STARTING, 3000);

		ACondition.waitFor("stop hammering", 20000, () -> {
			app.stopAsync(ui);
			assertEquals(RunState.INACTIVE, app.getRunState());
		});

		//TODO: can we check that deployment related jobs are really canceled/finished somehow?
		//   can we check that they didn't pop errors?
	}

	@Test public void stopCancelsStart() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		final String appName = "foo";
		space.defApp(appName);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		app.setProject(project);

		waitForApps(model, appName);

		clientFactory.setAppStartDelay(TimeUnit.MINUTES, 2);

		app.getBaseRunStateExp().addListener(new ValueListener<RunState>() {
			@Override
			public void gotValue(LiveExpression<RunState> exp, RunState value) {
				System.out.println("Runstate -> "+value);
			}
		});
		System.out.println("Restaring app...");
		app.restart(RunState.RUNNING, ui);
		waitForState(app, RunState.STARTING, 30000);

		System.out.println("Stopping app...");
		app.stopAsync(ui);

		waitForState(app, RunState.INACTIVE, 20000);
		System.out.println("Stopped!");
	}

	@Test public void stopCancelsRestartOnly() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createProject("to-deploy");
		final String appName = "foo";
		space.defApp(appName);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		app.setProject(project);

		waitForApps(model, appName);

		clientFactory.setAppStartDelay(TimeUnit.MINUTES, 2);
		app.restartOnlyAsynch(ui, app.createCancelationToken());
		waitForState(app, RunState.STARTING, 3000);

		app.stopAsync(ui);
		waitForState(app, RunState.INACTIVE, 20000);
	}

	@Test public void acceptDeployOfExistingApp() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createBootProject("to-deploy", withStarters("actuator", "web"));
		final String appName = "foo";
		MockCFApplication deployedApp = space.defApp(appName);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		app.setProject(null);
		assertNull(app.getProject());

		assertEquals(RunState.INACTIVE, app.getRunState());

		harness.answerDeploymentPrompt(ui, appName, appName);
		Mockito.doReturn(true).when(ui).confirmOperation(same("Replace Existing Application"), any());
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		System.out.println(app.getRunState());
		waitForJobsToComplete();
		System.out.println(app.getRunState());
		assertEquals(project, app.getProject());
		assertEquals(1, deployedApp.getPushCount());

		verify(ui).confirmOperation(any(), any());
	}


	@Test public void cancelDeployOfExistingApp() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		final String appName = "foo";
		MockCFApplication deployedApp = space.defApp(appName);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		app.setProject(null);
		assertNull(app.getProject());

		harness.answerDeploymentPrompt(ui, appName, appName);
		doReturn(false).when(ui).confirmOperation(same("Replace Existing Application"), any());
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForJobsToComplete();
		assertNull(app.getProject()); // since op was canceled it should not have set the project on the app.
		assertEquals(0, deployedApp.getPushCount());							  // since op was canceled it should not have deployed the app.

		verify(ui).confirmOperation(any(), any());
	}

	@Test public void manifestDiffDialogNotShownWhenNothingChanged() throws Exception {
		final String appName = "foo";

		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n"
		);
		MockCFApplication deployedApp = space.defApp(appName);
		deployedApp.start(CancelationTokens.NULL);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);
		app.setDeploymentManifestFile(project.getFile("manifest.yml"));
		app.setProject(project);
		assertEquals(1, app.getActualInstances());

//		deployedApp.scaleInstances(2); // change it 'externally'
		assertEquals(1, app.getActualInstances()); //The model doesn't know yet that it has changed!

//		harness.answerDeploymentPrompt(ui, appName, appName);

		app.restart(RunState.RUNNING, ui);

		waitForJobsToComplete();

		//If no change was detected the manfiest compare dialog shouldn't have popped.
		verify(ui, never()).openManifestDiffDialog(any());
	}

	@Test public void manifestDiffDialogShownWhenInstancesChangedExternally() throws Exception {
		final String appName = "foo";

		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n"
		);
		MockCFApplication deployedApp = space.defApp(appName);
		deployedApp.start(CancelationTokens.NULL);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);
		app.setDeploymentManifestFile(project.getFile("manifest.yml"));
		app.setProject(project);
		assertEquals(1, app.getActualInstances());

		deployedApp.scaleInstances(2); // change it 'externally'
		assertEquals(1, app.getActualInstances()); //The model doesn't know yet that it has changed!

		app.restart(RunState.RUNNING, ui);

		waitForJobsToComplete();

		//If the change was detected the deployment props dialog should have popped exactly once.
		verify(ui).openManifestDiffDialog(any());
	}

	@Test public void manifestDiffDialogChooseUseManfifest() throws Exception {
		//Setup initial state for our test
		final String appName = "foo";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		IFile manifest = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n" +
				"  memory: 1111M\n"
		);

		harness.answerDeploymentPrompt(ui, manifest);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		waitForState(app, RunState.RUNNING, APP_DEPLOY_TIMEOUT);

		{
			MockCFApplication appInCloud = space.getApplication(appName);
			assertEquals(1111, appInCloud.getMemory());
			Mockito.reset(ui);

			//// real test begins here

			appInCloud.setMemory(2222);
		}

		harness.answerManifestDiffDialog(ui, (ManifestDiffDialogModel dialog) -> {
			//??? code to check what's in the dialog???
			return ManifestDiffDialogModel.Result.USE_MANIFEST;
		});

		app.restart(RunState.RUNNING, ui);

		waitForJobsToComplete();
		{
			MockCFApplication appInCloud = space.getApplication(appName);
			assertEquals(2, appInCloud.getPushCount());
			assertEquals(RunState.RUNNING, app.getRunState());
			assertEquals(1111, appInCloud.getMemory());
			assertEquals(1111, (int)app.getMemory());
		}
	}

	@Test public void manifestDiffDialogChooseForgetManfifest() throws Exception {
		//Setup initial state for our test
		final String appName = "foo";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		IProject project = projects.createBootProject("to-deploy", withStarters("web", "actuator"));
		IFile manifest = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n" +
				"  memory: 1111M\n"
		);

		harness.answerDeploymentPrompt(ui, manifest);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, appName);
		CloudAppDashElement app = model.getApplication(appName);
		waitForState(app, RunState.RUNNING, APP_DEPLOY_TIMEOUT);

		MockCFApplication appInCloud = space.getApplication(appName);
		assertEquals(1111, appInCloud.getMemory());
		Mockito.reset(ui);

		//// real test begins here

		appInCloud.setMemory(2222);

		harness.answerManifestDiffDialog(ui, (ManifestDiffDialogModel dialog) -> {
			//??? code to check what's in the dialog???
			return ManifestDiffDialogModel.Result.FORGET_MANIFEST;
		});

		app.restart(RunState.RUNNING, ui);

		waitForJobsToComplete();

		assertEquals(2, appInCloud.getPushCount());
		assertEquals(RunState.RUNNING, app.getRunState());
		assertEquals(2222, appInCloud.getMemory());
		assertEquals(2222, (int)app.getMemory());
	}

	@Test public void testDeployManifestWithAbsolutePathAttribute() throws Exception {
		final String appName = "foo";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createProject("to-deploy");

		File zipFile = getTestZip("testapp");

		IFile manifestFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: foo\n" +
				"  path: "+zipFile.getAbsolutePath() + "\n" +
				"  buildpack: staticfile_buildpack"
		);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		harness.answerDeploymentPrompt(ui, manifestFile);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, "foo");

		CloudAppDashElement app = model.getApplication("foo");
		waitForState(app, RunState.RUNNING, APP_DELETE_TIMEOUT);

		assertEquals(project, app.getProject());

		assertEquals("some content here\n", space.getApplication(appName).getFileContents("test.txt"));

		verify(ui).promptApplicationDeploymentProperties(any());
		verifyNoMoreInteractions(ui);
	}


	@Test public void testDeployManifestWithRelativePathAttribute() throws Exception {
		final String appName = "foo";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		IProject project = projects.createProject("to-deploy");

		File zipFile = getTestZip("testapp");
		project.getFolder("zips").create(true, true, new NullProgressMonitor());
		project.getFolder("manifests").create(true, true, new NullProgressMonitor());
		createFile(project, "zips/testapp.zip", zipFile);

		IFile manifestFile = createFile(project, "manifests/manifest.yml",
				"applications:\n" +
				"- name: foo\n" +
				"  path: ../zips/testapp.zip\n" +
				"  buildpack: staticfile_buildpack"
		);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		harness.answerDeploymentPrompt(ui, manifestFile);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, "foo");

		CloudAppDashElement app = model.getApplication("foo");
		waitForState(app, RunState.RUNNING, APP_DEPLOY_TIMEOUT);

		assertEquals(project, app.getProject());

		assertEquals("some content here\n", space.getApplication(appName).getFileContents("test.txt"));

		verify(ui).promptApplicationDeploymentProperties(any());
		verifyNoMoreInteractions(ui);
	}

	@Test public void testDeployManifestWithoutPathAttribute() throws Exception {
		String appName = "foo";
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());
		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);

		IProject project = projects.createBootWebProject("empty-web-app");
		IFile manifestFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n"
		);
		File referenceJar = BootJarPackagingTest.packageAsJar(project, ui);

		harness.answerDeploymentPrompt(ui, manifestFile);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);
		waitForState(app, RunState.RUNNING, APP_DEPLOY_TIMEOUT);

		System.out.println("platform location = '"+Platform.getLocation()+"'");
		assertDeployedBytes(referenceJar, space.getApplication(appName));
	}

	@Test public void testSelectManifestActionEnablement() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		IProject project1 = projects.createProject("pr1");
		IProject project2 = projects.createProject("pr2");

		final String appName1 = "app1";
		final String appName2 = "app2";

		MockCFApplication cfApp1 = space.defApp(appName1);
		MockCFApplication cfApp2 = space.defApp(appName2);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName1, appName2);

		CloudAppDashElement app1 = model.getApplication(appName1);
		CloudAppDashElement app2 = model.getApplication(appName2);

		app1.setProject(project1);
		app2.setProject(project2);

		IAction action = actions.getSelectManifestAction();

		assertTrue(harness.selection.getElements().isEmpty());
		assertFalse(action.isEnabled());

		harness.selection.setElements(ImmutableSet.of(app1));
		assertNotNull(app1.getProject());
		assertTrue(action.isEnabled());

		harness.selection.setElements(ImmutableSet.of(app1, app2));
		assertFalse(action.isEnabled());

		app1.setProject(null);
		harness.selection.setElements(ImmutableSet.of(app1));
		assertFalse(action.isEnabled());

		harness.selection.setElements(ImmutableSet.of(app2));
		assertTrue(action.isEnabled());
		action.run();

	}

	@Test public void testSelectManifestAction() throws Exception {
		CFClientParams targetParams = CfTestTargetParams.fromEnv();
		MockCFSpace space = clientFactory.defSpace(targetParams.getOrgName(), targetParams.getSpaceName());

		IProject project = projects.createProject("pr");

		final String appName = "app";

		IFile manifestFile = createFile(project, "manifest.yml",
				"applications:\n" +
				"- name: "+appName+"\n"
		);

		space.defApp(appName);

		CloudFoundryBootDashModel model =  harness.createCfTarget(targetParams);
		waitForApps(model, appName);

		CloudAppDashElement app = model.getApplication(appName);
		app.setProject(project);

		harness.selection.setElements(ImmutableSet.of(app));

		harness.answerDeploymentPrompt(ui, manifestFile);

		assertNull(app.getDeploymentManifestFile());
		actions.getSelectManifestAction().run();
		waitForJobsToComplete();
		assertEquals(manifestFile, app.getDeploymentManifestFile());

		verify(ui).promptApplicationDeploymentProperties(any());
		verifyNoMoreInteractions(ui);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////

	//Stuff below is 'cruft' intended to make the tests above more readable. Maybe this code could be
	// moved to some kind of 'harness' (if there is a case where it can be reused).

	private void assertDeployedBytes(File referenceJar, MockCFApplication app) throws IOException {
		try (InputStream actualBits = app.getBits()) {
			try (InputStream expectedBits = new BufferedInputStream(new FileInputStream(referenceJar))) {
				try {
					assertEqualStreams(expectedBits, actualBits);
				} catch (Exception e) {
					saveArtefacts(referenceJar, app);
					throw e;
				}
			}
		}
	}

	private void saveArtefacts(File referenceJar, MockCFApplication app) throws IOException {
		File targetDir = getSaveDir();
		if (targetDir!=null) {
			int id = uniqueId++;
			File referenceJarCopy = new File(targetDir, "deployed-reference-"+id+".jar");
			File faultyJarCopy = new File(targetDir, "deployed-faulty-"+id+".jar");
			FileUtils.copyFile(referenceJar, referenceJarCopy);
			System.out.println("Reference jar saved: "+referenceJarCopy);
			IOUtil.pipe(app.getBits(), faultyJarCopy);
			System.out.println("Faulty jar saved: "+faultyJarCopy);
		}
	}

	private static int uniqueId = 0;

	private File getSaveDir() {
		IPath targetDirPath = Platform.getLocation();
		while (targetDirPath.segmentCount()>0 && !targetDirPath.lastSegment().equals("target")) {
			targetDirPath = targetDirPath.removeLastSegments(1);
		}
		if (targetDirPath.segmentCount()>0) {
			return targetDirPath.toFile();
		}
		return new File(System.getProperty("user.home"));
	}

	private void assertEqualStreams(InputStream expectedBytes, InputStream actualBytes) throws IOException {
		int offset = 0;
		int expected; int actual;
		while ((expected=expectedBytes.read())>=0) {
			actual = actualBytes.read();
			assertEquals("Different bytes at offset: "+offset, expected, actual);
			offset++;
		}
		assertEquals(-1, actualBytes.read());
	}

	private File getTestZip(String fileName) {
		File sourceWorkspace = new File(
				StsTestUtil.getSourceWorkspacePath("org.springframework.ide.eclipse.boot.dash.test"));
		File file = new File(sourceWorkspace, fileName + ".zip");
		Assert.isTrue(file.exists(), ""+ file);
		return file;
	}

	private void assertAppToProjectBinding(CloudFoundryBootDashModel target, IProject project, String appName) throws Exception {
		CloudAppDashElement appByProject = getApplication(target, project);
		CloudAppDashElement appByName = target.getApplication(appName);
		assertNotNull(appByProject);
		assertEquals(appByProject, appByName);
	}

	private CloudAppDashElement getApplication(CloudFoundryBootDashModel model, IProject project) {
		for (CloudAppDashElement app : model.getApplicationValues()) {
			IProject p = app.getProject();
			if (project.equals(p)) {
				return app;
			}
		}
		return null;
	}

	protected CloudAppDashElement deployApp(final CloudFoundryBootDashModel model, final String appName, IProject project)
			throws Exception {
		harness.answerDeploymentPrompt(ui, appName, appName);
		model.performDeployment(ImmutableSet.of(project), ui, RunState.RUNNING);

		waitForApps(model, appName);

		new ACondition("wait for app '"+ appName +"'to be RUNNING", 30000) { //why so long? JDT searching for main type.
			public boolean test() throws Exception {
				CloudAppDashElement element = model.getApplication(appName);
				assertEquals(RunState.RUNNING, element.getRunState());
				return true;
			}
		};
		return model.getApplication(appName);
	}

	protected void waitForApps(final CloudFoundryBootDashModel target, final String... names) throws Exception {
		new ACondition("wait for apps to appear", 10000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> appNames = getNames(target.getApplications().getValues());
				assertEquals(ImmutableSet.copyOf(names), appNames);
				return true;
			}
		};
	}

	protected void waitForServices(final CloudFoundryBootDashModel target, final String... names) throws Exception {
		new ACondition("wait for services to appear", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> serviceNames = getNames(target.getServices().getValues());
				assertEquals(ImmutableSet.copyOf(names), serviceNames);
				return true;
			}
		};
	}

	protected void waitForElements(final CloudFoundryBootDashModel target, final String... names) throws Exception {
		new ACondition("wait for elements to appear", 3000) {
			@Override
			public boolean test() throws Exception {
				ImmutableSet<String> elements = getNames(target.getElements().getValues());
				assertEquals(ImmutableSet.copyOf(names), elements);
				return true;
			}
		};
	}

	private Answer<Void> restoreDefaultTemplate() {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				EditTemplateDialogModel dialog = (EditTemplateDialogModel) invocation.getArguments()[0];
				dialog.restoreDefaultsHandler.call();
				dialog.performOk();
				return null;
			}
		};
	}

	/**
	 * Create a mockito {@link Answer} that interacts with EditTemplateDialog by setting the template value and then
	 * clicking the OK button.
	 */
	private Answer<Void> editSetTemplate(final String newText) {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				EditTemplateDialogModel dialog = (EditTemplateDialogModel) invocation.getArguments()[0];
				dialog.template.setValue(newText);
				dialog.performOk();
				return null;
			}
		};
	}

	private void assertSorted(ImmutableList<IAction> actions) {
		String[] actionNames = new String[actions.size()];
		for (int i = 0; i < actionNames.length; i++) {
			actionNames[i] = actions.get(i).getText();
		}

		String actual = StringUtils.arrayToDelimitedString(actionNames, "\n");

		Arrays.sort(actionNames);
		String expected = StringUtils.arrayToDelimitedString(actionNames, "\n");

		assertEquals(expected, actual);
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

	public static void waitForState(final BootDashElement element, final RunState state, long timeOut) throws Exception {
		new ACondition("Wait for state "+state, timeOut) {
			@Override
			public boolean test() throws Exception {
				assertEquals(state, element.getRunState());
				return true;
			}
		};
	}

	///////////////////////////////////////////////////////////////////////////////////


}
