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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.ide.eclipse.boot.core.BootPropertyTester.supportsLifeCycleManagement;
import static org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection.getDefaultProjectLocation;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.LaunchUtil;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.PropVal;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroup;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioInfo;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BootDashModelTest {

	public interface WizardConfigurer {

		void apply(NewSpringBootWizardModel wizard);

		WizardConfigurer NULL = new WizardConfigurer(){
			public void apply(NewSpringBootWizardModel wizard) {/*do nothing*/}
		};
	}

	WizardConfigurer withStarters(final String... ids) {
		if (ids.length>0) {
			return new WizardConfigurer() {
				public void apply(NewSpringBootWizardModel wizard) {
					for (String id : ids) {
						wizard.addDependency(id);
					}
				}
			};
		}
		return WizardConfigurer.NULL;
	}

	/**
	 * @return A wizard configurer that ensures the selected 'boot version' is at least
	 * a given version of boot.
	 */
	WizardConfigurer bootVersionAtLeast(final String wantedVersion) throws Exception {
		final VersionRange WANTED_RANGE = new VersionRange(wantedVersion);
		return new WizardConfigurer() {
			public void apply(NewSpringBootWizardModel wizard) {
				RadioGroup bootVersionRadio = wizard.getBootVersion();
				RadioInfo selected = bootVersionRadio.getValue();
				Version selectedVersion = getVersion(selected);
				if (WANTED_RANGE.includes(selectedVersion)) {
					//existing selection is fine
				} else {
					//try to select the latest available version and verify it meets the requirement
					bootVersionRadio.setValue(selected =  getLatestVersion(bootVersionRadio));
					selectedVersion = getVersion(selected);
					Assert.isTrue(WANTED_RANGE.includes(selectedVersion));
				}
			}

			private RadioInfo getLatestVersion(RadioGroup bootVersionRadio) {
				RadioInfo[] infos = bootVersionRadio.getRadios();
				Arrays.sort(infos, new Comparator<RadioInfo>() {
					public int compare(RadioInfo o1, RadioInfo o2) {
						Version v1 = getVersion(o1);
						Version v2 = getVersion(o2);
						return v2.compareTo(v1);
					}
				});
				return infos[0];
			}

			private Version getVersion(RadioInfo info) {
				String versionString = info.getValue();
				Version v = new Version(versionString);
				if ("BUILD-SNAPSHOT".equals(v.getQualifier())) {
					// Caveat "M1" will be treated as 'later' than "BUILD-SNAPSHOT" so that is wrong.
					return new Version(v.getMajor(), v.getMinor(), v.getMicro(), "SNAPSHOT"); //Comes after "MX" but before "RELEASE"
				}
				return v;
			}
		};
	}

	private static final long BOOT_PROJECT_CREATION_TIMEOUT = 3*60*1000; // long, may download maven dependencies
	private static final long MODEL_UPDATE_TIMEOUT = 3000; // short, should be nearly instant
	private static final long RUN_STATE_CHANGE_TIMEOUT = 20000;

	private TestBootDashModelContext context;
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
		model.addElementStateListener(listener);
		System.out.println("Element state listener ADDED");
		BootDashElement element = getElement(projectName);
		element.restart(runState, null);
		waitForState(element, runState);

		ElementStateListener oldListener = listener;
		model.removeElementStateListener(oldListener);
		System.out.println("Element state listener REMOVED");

		listener = mock(ElementStateListener.class);
		model.addElementStateListener(listener);

		element.stopAsync();
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

	@Test public void testStartingStateObservable() throws Exception {
		//Test that, for boot project that supports it, the 'starting' state
		// is observable in the model.
		String projectName = "some-project";
		createBootProject(projectName, bootVersionAtLeast("1.3.0")); //1.3.0 required for lifecycle support.

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
			element.stopAsync();
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
			element.stopAsync();
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
			element.stopAsync();
			waitForState(element, RunState.INACTIVE);
		}
	}

	@Test public void testRequestMappings() throws Exception {
		String projectName = "actuated-project";
		createBootProject(projectName,
				bootVersionAtLeast("1.3.0"), //required for us to be able to determine the actuator port
				withStarters("actuator")     //required to actually *have* an actuator
		);
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

			//Case 2 examples (path extracted from 'pseudo' json in the key)
			assertRequestMappingWithPath(mappings, "/error"); //Even empty apps should have a 'error' mapping
			assertRequestMappingWithPath(mappings, "/mappings"); //Since we are using this, it should be there.

			//Case 1 example (path represented directly in the json key).
			assertRequestMappingWithPath(mappings, "/**/favicon.ico");

		} finally {
			element.stopAsync();
			waitForState(element, RunState.INACTIVE);
		}
	}

	///////////////// harness code ////////////////////////

	private void assertRequestMappingWithPath(List<RequestMapping> mappings, String string) {
		StringBuilder builder = new StringBuilder();
		for (RequestMapping m : mappings) {
			builder.append(m.getPath()+"\n");
			if (m.getPath().equals(string)) {
				return;
			}
		}
		fail(
				"Expected path not found: "+string+"\n" +
				"Found:\n" +
				builder
		);
	}

	@Rule
	public TestRule listenerLeakDetector = new ListenerLeakDetector();

	private UserInteractions ui;

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.model = new BootDashModel(context);
		StsTestUtil.setAutoBuilding(false);
		this.ui = mock(UserInteractions.class);
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

		this.model.dispose();
	}

	/**
	 * Returns the only active (i.e. not terminated launch for a project). If there is more
	 * than one active launch, or no active launch this returns null.
	 */
	public ILaunch getActiveLaunch(BootDashElement element) {
		List<ILaunch> ls = LaunchUtil.getLaunches(element.getProject());
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

	private IProject createBootProject(final String projectName, final WizardConfigurer... extraConfs) throws Exception {
		final Job job = new Job("Create boot project '"+projectName+"'") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					NewSpringBootWizardModel wizard = new NewSpringBootWizardModel();
					wizard.getProjectName().setValue(projectName);
					wizard.getArtifactId().setValue(projectName);
					//Note: unlike most of the rest of the wizard's behavior, the 'use default location'
					//  checkbox and its effect is not part of the model but part of the GUI code (this is
					//  wrong, really, but that's how it is, so we have to explictly set the project
					//  location in the model.
					wizard.getLocation().setValue(getDefaultProjectLocation(projectName));
					wizard.addDependency("web");
					for (WizardConfigurer extraConf : extraConfs) {
						extraConf.apply(wizard);
					}
					wizard.performFinish(new NullProgressMonitor());
					return Status.OK_STATUS;
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
			}
		};
		job.setRule(context.getWorkspace().getRuleFactory().buildRule());
		job.schedule();

		new ACondition() {
			@Override
			public boolean test() throws Exception {
				assertOk(job.getResult());
				StsTestUtil.assertNoErrors(getProject(projectName));
				return true;
			}

		}.waitFor(BOOT_PROJECT_CREATION_TIMEOUT);
		return getProject(projectName);
	}

	private void assertOk(IStatus result) throws Exception{
		if (!result.isOK()) {
			throw ExceptionUtil.coreException(result);
		}
	}

	private IProject getProject(String projectName) {
		return context.getWorkspace().getRoot().getProject(projectName);
	}

	private void assertWorkspaceProjects(String... expectedProjectNames) {
		IProject[] projects = context.getWorkspace().getRoot().getProjects();
		String[] names = new String[projects.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = projects[i].getName();
		}
		assertElements(names, expectedProjectNames);
	}


}
