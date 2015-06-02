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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection.getDefaultProjectLocation;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertElements;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


public class BootDashModelTest {

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

		element.stop();
		waitForState(element, RunState.INACTIVE);

		verify(oldListener).stateChanged(element);
		verify(listener).stateChanged(element);
	}

	///////////////// harness code ////////////////////////

	@Rule
	public TestRule listenerLeakDetector = new ListenerLeakDetector();

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = spy(new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
				));
		this.model = new BootDashModel(context);
		StsTestUtil.setAutoBuilding(false);
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

	private IProject createBootProject(final String projectName) throws Exception {
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
