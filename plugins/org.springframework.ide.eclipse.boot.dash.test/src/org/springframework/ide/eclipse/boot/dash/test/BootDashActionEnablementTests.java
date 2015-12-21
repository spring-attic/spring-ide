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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockMultiSelection;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.OpenLaunchConfigAction;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.WizardConfigurer;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class BootDashActionEnablementTests {

	@Test
	public void openConfigActionEnablementForProject() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final BootDashElement element = harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final OpenLaunchConfigAction action = actions.getOpenConfigAction();

		//If selection is empty the action must not be enabled
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		//If selection has more than one element... the action must not be enabled
		selection.setElements(element, mock(BootDashElement.class));
		assertFalse(action.isEnabled());

		//If selection has one element...
		selection.setElements(element);

		//a) and element has no launch configs...
		assertTrue(element.getLaunchConfigs().isEmpty());
		assertTrue(action.isEnabled());

		// Careful... when changing the launch configs of a element, the enablement state of
		// action should auto-refresh, but this happens asyncly so the tests sequences are put in such
		// a order that the enablement state changes on each (otherwise the ACondition may vacuously
		// pass immediately even if the enablement didn't get updated, as it was correct from
		// the start)

		//b) and element has multiple launch config
		assertTrue(action.isEnabled()); // make sure the test won't pass 'by accident'.
		final ILaunchConfiguration c1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final ILaunchConfiguration c2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		new ACondition(2000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(c1,c2), element.getLaunchConfigs());
				assertFalse(action.isEnabled());
				return true;
			}
		};

		//b) and element has a single launch config
		assertFalse(action.isEnabled()); // make sure the test won't pass 'by accident'.
		c2.delete();
		new ACondition(2000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(c1), element.getLaunchConfigs());
				assertTrue(action.isEnabled());
				return true;
			}
		};
	}


	@Test
	public void openConfigActionEnablementForLaunchConfig() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final BootDashElement element = harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final OpenLaunchConfigAction action = actions.getOpenConfigAction();

		BootLaunchConfigurationDelegate.createConf(javaProject);
		BootLaunchConfigurationDelegate.createConf(javaProject);

		//Check initial conditions are as expected:
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());
		assertEquals(2, element.getChildren().getValues().size());

		//Check action enablement for the children
		for (BootDashElement child : element.getChildren().getValues()) {
			selection.setElements(child);
			assertTrue(action.isEnabled());
		}
	}

	@Test
	public void openRedebugActionEnablementForLaunchConfig() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final BootDashElement element = harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final RunStateAction action = getRunStateAction(RunState.DEBUGGING);

		BootLaunchConfigurationDelegate.createConf(javaProject);
		BootLaunchConfigurationDelegate.createConf(javaProject);

		//Check initial conditions are as expected:
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());
		assertEquals(2, element.getChildren().getValues().size());

		//Check action enablement for the children
		for (BootDashElement child : element.getChildren().getValues()) {
			selection.setElements(child);
			assertTrue(action.isEnabled());
		}
	}

	@Test
	public void openRedebugActionEnablementForProject() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final BootDashElement element = harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final RunStateAction action = getRunStateAction(RunState.DEBUGGING);

		//If selection is empty the action must not be enabled
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		//If selection has one element...
		selection.setElements(element);

		//a) and element has no launch configs...
		assertTrue(element.getLaunchConfigs().isEmpty());
		assertTrue(action.isEnabled());

		// Careful... when changing the launch configs of a element, the enablement state of
		// action should auto-refresh, but this happens asyncly so the tests sequences are put in such
		// a order that the enablement state changes on each (otherwise the ACondition may vacuously
		// pass immediately even if the enablement didn't get updated, as it was correct from
		// the start)

		//b) and element has multiple launch config
		assertTrue(action.isEnabled()); // make sure the test won't pass 'by accident'.
		final ILaunchConfiguration c1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final ILaunchConfiguration c2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		new ACondition(2000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(c1,c2), element.getLaunchConfigs());
				assertFalse(action.isEnabled());
				return true;
			}
		};

		//b) and element has a single launch config
		assertFalse(action.isEnabled()); // make sure the test won't pass 'by accident'.
		c2.delete();
		new ACondition(2000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(c1), element.getLaunchConfigs());
				assertTrue(action.isEnabled());
				return true;
			}
		};
	}

	@Test
	public void openRedebugActionEnablementForMultipleProjects() throws Exception {
		IProject p1 = createBootProject("project1");
		IProject p2 = createBootProject("project2");

		BootDashElement e1 = harness.getElementWithName(p1.getName());
		BootDashElement e2 = harness.getElementWithName(p2.getName());

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final RunStateAction action = getRunStateAction(RunState.DEBUGGING);

		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		selection.setElements(e1, e2);

		assertTrue(action.isEnabled());
	}


	////////////////////////////////////////////////////////////////////////


	private TestBootDashModelContext context;
	private BootProjectTestHarness projects;
	private BootDashViewModelHarness harness;
	private BootDashActions actions;
	private UserInteractions ui;

	@Rule
	public AutobuildingEnablement autobuild = new AutobuildingEnablement(false);

	@Rule
	public TestBracketter bracketter = new TestBracketter();

	@Rule
	public LaunchCleanups launchCleanups = new LaunchCleanups();

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(
				ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager()
		);
		this.harness = new BootDashViewModelHarness(context, RunTargetTypes.LOCAL);
		this.projects = new BootProjectTestHarness(context.getWorkspace());
		this.ui = mock(UserInteractions.class);
		this.actions = new BootDashActions(harness.model, harness.selection.forReading(), ui);
	}

	@After
	public void tearDown() throws Exception {
		this.harness.dispose();
		this.actions.dispose();
	}


	private IProject createBootProject(String projectName, WizardConfigurer... extraConfs) throws Exception {
		return projects.createBootWebProject(projectName, extraConfs);
	}

	private RunStateAction getRunStateAction(RunState goalState) {
		for (RunStateAction s : actions.getRunStateActions()) {
			if (s.getGoalState()==goalState) {
				return s;
			}
		}
		return null;
	}


}
