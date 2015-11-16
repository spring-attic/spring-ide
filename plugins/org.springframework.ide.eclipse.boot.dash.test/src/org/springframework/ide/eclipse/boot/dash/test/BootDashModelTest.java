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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.ide.eclipse.boot.core.BootPropertyTester.supportsLifeCycleManagement;
import static org.springframework.ide.eclipse.boot.dash.test.requestmappings.RequestMappingAsserts.assertRequestMappingWithPath;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.setContents;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.launch.AbstractBootLaunchConfigurationDelegate.PropVal;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.WizardConfigurer;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Kris De Volder
 */
public class BootDashModelTest {

	private static final long MODEL_UPDATE_TIMEOUT = 3000; // short, should be nearly instant
	private static final long RUN_STATE_CHANGE_TIMEOUT = 20000;

	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private BootDashModel model;

	/**
	 * Test that newly created spring boot project gets added to the model.
	 */
	@Test public void testNewSpringBootProject() throws Exception {

		assertWorkspaceProjects(/*none*/);
		assertModelElements(/*none*/);

		String projectName = "testProject";
		IProject project = createBootProject(projectName);
		new ACondition("Model update") {
			public boolean test() throws Exception {
				assertModelElements("testProject");
				return true;
			}
		}.waitFor(3000);
	}

	private IProject createBootProject(String projectName, WizardConfigurer... extraConfs) throws Exception {
		return projects.createBootWebProject(projectName, extraConfs);
	}

	/**
	 * Test that when project is deleted from workspace it also deleted from the model
	 */
	@Test public void testDeleteProject() throws Exception {
		String projectName = "testProject";
		IProject project = createBootProject(projectName);

		waitModelElements("testProject");
		project.delete(/*delete content*/true, /*force*/true, /*progress*/null);
		waitModelElements(/*none*/);
	}

	/**
	 * Test that when closed/opened it is removed/added to the model
	 */
	@Test public void testCloseAndOpenProject() throws Exception {
		String projectName = "testProject";
		IProject project = createBootProject(projectName);

		waitModelElements("testProject");

		project.close(null);
		waitModelElements();

		project.open(null);
		waitModelElements("testProject");
	}


	/**
	 * Test that element state listener is notified when a project is launched and terminated.
	 */
	@Test public void testRunStateChanges() throws Exception {
		doTestRunStateChanges(RunState.RUNNING);
	}

	/**
	 * Test that element state listener is notified when a project is launched in Debug mode and terminated.
	 */
	@Test public void testDebugStateChanges() throws Exception {
		doTestRunStateChanges(RunState.DEBUGGING);
	}

	protected void doTestRunStateChanges(RunState runState) throws Exception {
		String projectName = "testProject";
		createBootProject(projectName);
		waitModelElements(projectName);

		ElementStateListener listener = mock(ElementStateListener.class);
		ElementStateListener debugListener;
		model.addElementStateListener(debugListener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				System.out.println(e.getName()+" state became: "+e.getRunState());
			}
		});
		model.addElementStateListener(listener);
		System.out.println("Element state listener ADDED");
		BootDashElement element = getElement(projectName);
		element.restart(runState, null);
		waitForState(element, runState);

		ElementStateListener oldListener = listener;
		model.removeElementStateListener(oldListener);
		model.removeElementStateListener(debugListener);
		System.out.println("Element state listener REMOVED");

		listener = mock(ElementStateListener.class);
		model.addElementStateListener(listener);

		element.stopAsync(ui);
		waitForState(element, RunState.INACTIVE);

		//The expected behavior will change when boot 1.3.0 is the default so guard against
		// that already:
		int expectedReadyStateChanges =supportsLifeCycleManagement(element.getProject())
			?2  // INACTIVE -> STARTING -> RUNNING
			:1; // INACTIVE -> RUNNING
		verify(oldListener, times(expectedReadyStateChanges)).stateChanged(element);
		verify(listener,    times(1)).stateChanged(element); //running  -> inactive
	}

	@Test public void testRestartRunningProcessTest() throws Exception {
		String projectName = "testProject";
		createBootProject(projectName);
		waitModelElements(projectName);

		final RunState[] RUN_STATES = {
				RunState.RUNNING,
				RunState.DEBUGGING
		};

		for (RunState fromState : RUN_STATES) {
			for (RunState toState : RUN_STATES) {
				doRestartTest(projectName, fromState, toState);
			}
		}
	}

	@Test public void testDevtoolsPortRefreshedOnRestart() throws Exception {
		//Test that the local bootdash element 'liveport' is updated when boot devtools
		// does an in-place restart of the app, changing the port that it runs on.
		String projectName = "some-project-with-devtools";
		createBootProject(projectName, bootVersionAtLeast("1.3.0"),  //1.3.0 required for lifecycle & devtools support
				withStarters("devtools")
		);

		final BootDashElement element = getElement(projectName);
		try {
			waitForState(element, RunState.INACTIVE);
			element.restart(RunState.RUNNING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.RUNNING);

			int defaultPort = 8080;
			int changedPort = 8765;

			assertEquals(defaultPort, element.getLivePort());

			IFile props = element.getProject().getFile(new Path("src/main/resources/application.properties"));
			setContents(props, "server.port="+changedPort);
			StsTestUtil.assertNoErrors(element.getProject());
			   //builds the project... should trigger devtools to 'refresh'.

			waitForPort(element, changedPort);

			//Now try that this also works in debug mode...
			element.restart(RunState.DEBUGGING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.DEBUGGING);

			assertEquals(changedPort, element.getLivePort());
			setContents(props, "server.port="+defaultPort);
			StsTestUtil.assertNoErrors(element.getProject());
			   //builds the project... should trigger devtools to 'refresh'.
			waitForPort(element, defaultPort);

		} finally {
			element.stopAsync(ui);
			waitForState(element, RunState.INACTIVE);
		}
	}

	protected void waitForPort(final BootDashElement element, final int expectedPort) throws Exception {
		new ACondition("Wait for port to change") {
			@Override
			public boolean test() throws Exception {
				assertEquals(expectedPort, element.getLivePort());
				return true;
			}
		}.waitFor(5000); //Devtools should restart really fast
	}

	@Test public void testStartingStateObservable() throws Exception {
		//Test that, for boot project that supports it, the 'starting' state
		// is observable in the model.
		String projectName = "some-project";
		createBootProject(projectName,
				bootVersionAtLeast("1.3.0") //1.3.0 required for lifecycle support
		);
		BootDashElement element = getElement(projectName);
		try {
			waitForState(element, RunState.INACTIVE);

			element.restart(RunState.RUNNING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.RUNNING);

			element.restart(RunState.DEBUGGING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.DEBUGGING);
		} finally {
			element.stopAsync(ui);
			waitForState(element, RunState.INACTIVE);
		}
	}

	private void doRestartTest(String projectName, RunState fromState, RunState toState) throws Exception {
		BootDashElement element = getElement(projectName);
		try {
			element.restart(fromState, ui);
			waitForState(element, fromState);

			final ILaunch launch = getActiveLaunch(element);

			element.restart(toState, ui);

			//Watch out for race conditions... we can't really reliably observe the
			// 'terminated' state of the element, as we don't know how long it will
			// last and the 'restart' operation may happen concurrently with the testing
			// thread. Therefore we observe the terminated state of the actual launch.
			// Restarting the project will/should terminate the old launch and then
			// create a new launch.

			new ACondition("Wait for launch termination") {
				public boolean test() throws Exception {
					return launch.isTerminated();
				}
			}.waitFor(RUN_STATE_CHANGE_TIMEOUT);

			waitForState(element, toState);
		} finally {
			element.stopAsync(ui);
			waitForState(element, RunState.INACTIVE);
		}
	}

	@Test public void livePort() throws Exception {
		String projectName = "some-project";
		createBootProject(projectName, bootVersionAtLeast("1.3.0")); //1.3.0 required for lifecycle support.

		BootDashElement element = getElement(projectName);
		assertEquals(RunState.INACTIVE, element.getRunState());
		assertEquals(-1, element.getLivePort()); // live port is 'unknown' if app is not running
		try {
			waitForState(element, RunState.INACTIVE);

			element.restart(RunState.RUNNING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.RUNNING);

			assertEquals(8080, element.getLivePort());

			//Change port in launch conf and restart
			ILaunchConfiguration conf = element.getActiveConfig();
			ILaunchConfigurationWorkingCopy wc = conf.getWorkingCopy();
			BootLaunchConfigurationDelegate.setProperties(wc, Collections.singletonList(
					new PropVal("server.port", "6789", true)
			));
			wc.doSave();

			assertEquals(8080, element.getLivePort()); // port still the same until we restart

			element.restart(RunState.RUNNING, ui);
			waitForState(element, RunState.STARTING);
			waitForState(element, RunState.RUNNING);
			assertEquals(6789, element.getLivePort());

		} finally {
			element.stopAsync(ui);
			waitForState(element, RunState.INACTIVE);
		}
	}

	@Test public void testRequestMappings() throws Exception {
		String projectName = "actuated-project";
		IProject project = createBootProject(projectName,
				bootVersionAtLeast("1.3.0"), //required for us to be able to determine the actuator port
				withStarters("web", "actuator")     //required to actually *have* an actuator
		);
		createFile(project, "src/main/java/com/example/HelloController.java",
				"package com.example;\n" +
				"\n" +
				"import org.springframework.web.bind.annotation.RequestMapping;\n" +
				"import org.springframework.web.bind.annotation.RestController;\n" +
				"\n" +
				"@RestController\n" +
				"public class HelloController {\n" +
				"\n" +
				"	@RequestMapping(\"/hello\")\n" +
				"	public String hello() {\n" +
				"		return \"Hello, World!\";\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);
		StsTestUtil.assertNoErrors(project);
		BootDashElement element = getElement(projectName);
		try {
			waitForState(element, RunState.INACTIVE);
			assertNull(element.getLiveRequestMappings()); // unknown since can only be determined when app is running

			element.restart(RunState.RUNNING, ui);
			waitForState(element, RunState.RUNNING);
			List<RequestMapping> mappings = element.getLiveRequestMappings();
			assertNotNull(mappings);
			assertTrue(!mappings.isEmpty()); //Even though this is an 'empty' app should have some mappings,
			                                 // for example an 'error' page.
			System.out.println(">>> Found RequestMappings");
			for (RequestMapping m : mappings) {
				System.out.println(m.getPath());
				assertNotNull(m.getPath());
			}
			System.out.println("<<< Found RequestMappings");

			RequestMapping rm;
			//Case 2 examples (path extracted from 'pseudo' json in the key)
			rm = assertRequestMappingWithPath(mappings, "/hello"); //We defined it so should be there
			assertEquals("com.example.HelloController", rm.getFullyQualifiedClassName());
			assertEquals("hello", rm.getMethodName());
			assertEquals("com.example.HelloController", rm.getType().getFullyQualifiedName());

			IMethod method = rm.getMethod();
			assertEquals(rm.getType(), method.getDeclaringType());
			assertEquals("hello", method.getElementName());

			assertTrue(rm.isUserDefined());

			rm = assertRequestMappingWithPath(mappings, "/error"); //Even empty apps should have a 'error' mapping
			assertFalse(rm.isUserDefined());

			rm = assertRequestMappingWithPath(mappings, "/mappings || /mappings.json"); //Since we are using this, it should be there.
			assertNotNull(rm.getMethod());
			assertNotNull(rm.getType());
			assertFalse(rm.isUserDefined());

			//Case 1 example (path represented directly in the json key).
			rm = assertRequestMappingWithPath(mappings, "/**/favicon.ico");
			assertFalse(rm.isUserDefined());

		} finally {
			element.stopAsync(ui);
			waitForState(element, RunState.INACTIVE);
		}
	}

	@Test public void testDefaultRequestMapping() throws Exception {
		String projectName = "sdfsd-project";
		createBootProject(projectName);
		BootDashElement element = getElement(projectName);

		assertNull(element.getDefaultRequestMappingPath());
		element.setDefaultRequestMapingPath("something");
		assertProjectProperty(element.getProject(), "default.request-mapping.path", "something");

		assertEquals("something", element.getDefaultRequestMappingPath());

	}

	/**************************************************************************************
	 * TAGS Tests START
	 *************************************************************************************/

	private void testSettingTags(String[] tagsToSet, String[] expectedTags) throws Exception {
		String projectName = "alex-project";
		createBootProject(projectName);
		BootDashElement element = getElement(projectName);
		IProject project = element.getProject();

		if (tagsToSet==null || tagsToSet.length==0) {
			element.setTags(new LinkedHashSet<String>(Arrays.asList("foo", "bar")));
			assertFalse(element.getTags().isEmpty());
		} else {
			assertArrayEquals(new String[]{}, element.getTags().toArray(new String[0]));
		}

		element.setTags(linkedHashSet(tagsToSet));
		waitForJobsToComplete();
		assertArrayEquals(expectedTags, element.getTags().toArray(new String[0]));

		// Reopen the project to load tags from the resource
		project.close(null);
		project.open(null);
		element = getElement(projectName);
		assertArrayEquals(expectedTags, element.getTags().toArray(new String[0]));
	}

	private LinkedHashSet<String> linkedHashSet(String[] tagsToSet) {
		if (tagsToSet!=null) {
			return new LinkedHashSet<String>(Arrays.asList(tagsToSet));
		}
		return null;
	}

	@Test
	public void setUniqueTagsForProject() throws Exception {
		testSettingTags(new String[] {"xd", "spring"}, new String[] {"xd", "spring"});
	}

	@Test
	public void setDuplicateTagsForProject() throws Exception {
		testSettingTags(new String[] {"xd", "spring", "xd", "spring", "spring"}, new String[] {"xd", "spring"});
	}

	@Test
	public void setTagsWithWhiteSpaceCharsForProject() throws Exception {
		testSettingTags(new String[] {"#xd", "\tspring", "xd ko ko", "spring!!-@", "@@@ - spring"}, new String[] {"#xd", "\tspring", "xd ko ko", "spring!!-@", "@@@ - spring"});
	}

	@Test
	public void setNoTags() throws Exception {
		testSettingTags(new String[0], new String[0]);
	}

	@Test
	public void setNullTags() throws Exception {
		testSettingTags(null, new String[0]);
	}


	/**************************************************************************************
	 * TAGS Tests END
	 *************************************************************************************/

	///////////////// harness code ////////////////////////

	private void assertProjectProperty(IProject project, String prop, String value) {
		assertEquals(value, context.getProjectProperties().get(project, prop));
	}

	@Rule
	public TestRule listenerLeakDetector = new ListenerLeakDetector();

	private UserInteractions ui;
	private BootDashViewModelHarness harness;

	@Before
	public void setup() throws Exception {
		//As part of its normal operation, devtools will throw some uncaucht exceptions.
		// We don't want our tests to be disrupted when running the process in debug mode... so disable
		// suspending on such exceptions:
		suspendOnUncaughtException(false);

		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.harness = new BootDashViewModelHarness(context, RunTargetTypes.LOCAL);
		this.model = harness.getRunTargetModel(RunTargetTypes.LOCAL);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		StsTestUtil.setAutoBuilding(false);
		this.ui = mock(UserInteractions.class);

	}

	public static void suspendOnUncaughtException(boolean enable) {
		String suspendOption = "org.eclipse.jdt.debug.ui.javaDebug.SuspendOnUncaughtExceptions";
		IEclipsePreferences debugPrefs = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.debug.ui");
		debugPrefs.putBoolean(suspendOption, enable);
	}

	@After
	public void tearDown() throws Exception {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		for (ILaunch l : launches) {
			if (!l.isTerminated()) {
				fail("Leaky test code leaves launch running? "+l);
			}
			launchManager.removeLaunch(l);
		}

		for (ILaunchConfiguration conf : launchManager.getLaunchConfigurations()) {
			conf.delete();
		}

		this.harness.dispose();
	}

	/**
	 * Returns the only active (i.e. not terminated launch for a project). If there is more
	 * than one active launch, or no active launch this returns null.
	 */
	public ILaunch getActiveLaunch(BootDashElement element) {
		List<ILaunch> ls = BootLaunchUtils.getBootLaunches(element.getProject());
		ILaunch activeLaunch = null;
		for (ILaunch l : ls) {
			if (!l.isTerminated()) {
				if (activeLaunch==null) {
					activeLaunch = l;
				} else {
					//More than one active launch
					return null;
				}
			}
		}
		return activeLaunch;
	}

	private void waitForState(final BootDashElement element, final RunState state) throws Exception {
		new ACondition("Wait for state") {
			@Override
			public boolean test() throws Exception {
				return element.getRunState()==state;
			}
		}.waitFor(RUN_STATE_CHANGE_TIMEOUT);
	}

	private BootDashElement getElement(String name) {
		for (BootDashElement el : model.getElements().getValues()) {
			if (name.equals(el.getName())) {
				return el;
			}
		}
		return null;
	}

	private void assertModelElements(String... expectedElementNames) {
		Set<BootDashElement> elements = model.getElements().getValue();
		Set<String> names = new HashSet<String>();
		for (BootDashElement e : elements) {
			names.add(e.getName());
		}
		assertElements(names, expectedElementNames);
	}

	public void waitModelElements(final String... expectedElementNames) throws Exception {
		new ACondition("Model update") {
			public boolean test() throws Exception {
				assertModelElements(expectedElementNames);
				return true;
			}
		}.waitFor(MODEL_UPDATE_TIMEOUT);
	}

	private void assertWorkspaceProjects(String... expectedProjectNames) {
		IProject[] projects = context.getWorkspace().getRoot().getProjects();
		String[] names = new String[projects.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = projects[i].getName();
		}
		assertElements(names, expectedProjectNames);
	}

	private void waitForJobsToComplete() throws Exception {
		new ACondition("Wait for Jobs") {
			@Override
			public boolean test() throws Exception {
				assertJobManagerIdle();
				return true;
			}
		}.waitFor(3 * 60 * 1000);
	}

}
