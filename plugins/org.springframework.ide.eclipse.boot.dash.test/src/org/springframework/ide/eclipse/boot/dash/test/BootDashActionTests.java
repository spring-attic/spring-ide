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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.AbstractLaunchConfigurationsDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargets;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockMultiSelection;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions.RunOrDebugStateAction;
import org.springframework.ide.eclipse.boot.dash.views.DuplicateConfigAction;
import org.springframework.ide.eclipse.boot.dash.views.OpenLaunchConfigAction;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport;
import org.springframework.ide.eclipse.boot.test.AutobuildingEnablement;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.WizardConfigurer;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class BootDashActionTests {

	@Test
	public void deleteConfigActionEnablementForProject() throws Exception {
		//At the moment, this action does not enable for projects at all
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		BootLaunchConfigurationDelegate.createConf(javaProject);

		final IAction action = actions.getDeleteConfigsAction();
		action.setEnabled(true); //force it to true so we can tell that it actually changes.
 		selection.setElements(element);
 		new ACondition("Wait for disablement", 3000) {
			public boolean test() throws Exception {
				assertFalse(action.isEnabled());
				return true;
			}
		};
	}

	@Test
	public void deleteConfigActionEnablementForConf() throws Exception {
		//At the moment, this action always enables for one or more launch confs
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final ILaunchConfiguration conf1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		BootDashElement el1 = harness.getElementFor(conf1);
		final ILaunchConfiguration conf2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		BootDashElement el2 = harness.getElementFor(conf2);

		new ACondition("Wait for children", 3000) {
			public boolean test() throws Exception {
				assertEquals(2, element.getCurrentChildren().size());
				return true;
			}
		};

		final IAction action = actions.getDeleteConfigsAction();

		assertFalse(action.isEnabled());
		selection.setElements(el1);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		action.setEnabled(false);
		selection.setElements(el1, el2);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

	}

	@Test
	public void deleteConfigAction() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final ILaunchConfiguration conf1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final BootDashElement el1 = harness.getElementFor(conf1);
		final ILaunchConfiguration conf2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final BootDashElement el2 = harness.getElementFor(conf2);

		new ACondition("Wait for children", 3000) {
			public boolean test() throws Exception {
				assertEquals(2, element.getCurrentChildren().size());
				assertEquals(ImmutableSet.of(el1, el2), element.getCurrentChildren());
				return true;
			}
		};

		final IAction action = actions.getDeleteConfigsAction();

		assertFalse(action.isEnabled());
		selection.setElements(el1);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		when(ui.confirmOperation(eq("Deleting Elements"), anyString())).thenReturn(true);
		action.run();

		new ACondition("Wait for config deletion", 3000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(conf2), element.getLaunchConfigs());
				assertFalse(conf1.exists());
				assertTrue(element.getCurrentChildren().size()==1);
				return true;
			}
		};
	}


	@Test
	public void duplicateConfigAction() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final DuplicateConfigAction action = actions.getDuplicateConfigAction();
		final ILaunchConfiguration conf1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		assertEquals(0, getJMXPortAsInt(conf1));
		selection.setElements(element);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		action.run();

		new ACondition("Wait for action post conditions", 3000) {
			public boolean test() throws Exception {
				ImmutableSet<ILaunchConfiguration> confs = element.getLaunchConfigs();
				assertEquals(2, confs.size());
				assertEquals(2, element.getCurrentChildren().size());
				assertTrue(confs.contains(conf1));
				for (ILaunchConfiguration other : confs) {
					assertEquals(0, getJMXPortAsInt(other));
				}
				return true;
			}

		};
	}

	@Test
	public void duplicateConfigActionWithJmxPortSet() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final DuplicateConfigAction action = actions.getDuplicateConfigAction();
		final ILaunchConfiguration conf1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		assertEquals(0, getJMXPortAsInt(conf1));
		String randomPort = ""+JmxBeanSupport.randomPort();
		setJMXPort(conf1, randomPort);
		assertEquals(randomPort, BootLaunchConfigurationDelegate.getJMXPort(conf1));
		selection.setElements(element);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		action.run();

		new ACondition("Wait for action post conditions", 3000) {
			public boolean test() throws Exception {
				ImmutableSet<ILaunchConfiguration> confs = element.getLaunchConfigs();
				assertEquals(2, confs.size());
				assertEquals(2, element.getCurrentChildren().size());
				assertTrue(confs.contains(conf1));
				for (ILaunchConfiguration other : confs) {
					if (!other.equals(conf1)) {
						assertFalse(getJMXPortAsInt(conf1)==getJMXPortAsInt(other));
					}
				}
				return true;
			}

		};
	}

	private static void setJMXPort(ILaunchConfiguration conf, String port) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = conf.getWorkingCopy();
		BootLaunchConfigurationDelegate.setJMXPort(wc, port);
		wc.doSave();
	}

	private static int getJMXPortAsInt(ILaunchConfiguration conf) {
		try {
			String str = BootLaunchConfigurationDelegate.getJMXPort(conf);
			if (str!=null) {
				return Integer.parseInt(str);
			}
		} catch (NumberFormatException e) {
			//couldn't parse
		}
		return -1;
	}

	@Test
	public void duplicateConfigActionEnablementForLaunchConf() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final DuplicateConfigAction action = actions.getDuplicateConfigAction();

		ILaunchConfiguration conf1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		ILaunchConfiguration conf2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		assertEquals(2, element.getLaunchConfigs().size());
		new ACondition("Wait for elements", 3000) {
			public boolean test() throws Exception {
				assertEquals(2, element.getCurrentChildren().size());
				return true;
			}
		};

		BootDashElement el1 = harness.getElementFor(conf1);
		BootDashElement el2 = harness.getElementFor(conf2);

		assertFalse(action.isEnabled()); // or test may pass vacuously without an actual update
		selection.setElements(el1);
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		selection.setElements(el1, el2);
		new ACondition("Wait for disablement", 3000) {
			public boolean test() throws Exception {
				assertFalse(action.isEnabled());
				return true;
			}
		};
	}

	@Test
	public void duplicateConfigActionEnablementForProject() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final AbstractLaunchConfigurationsDashElement<?> element = (AbstractLaunchConfigurationsDashElement<?>) harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final DuplicateConfigAction action = actions.getDuplicateConfigAction();

		//If selection is empty the action must not be enabled
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		//If project is selected then...
		selection.setElements(element);
		// a) if project has no launch configs ...
		assertTrue(element.getLaunchConfigs().isEmpty());
		// then there's nothing to duplicate... so disabled
		assertFalse(action.isEnabled());

		// b) if project has exactly one launch config ...
		BootLaunchConfigurationDelegate.createConf(javaProject);
		assertEquals(1, element.getLaunchConfigs().size());
		// action enablement is updated as response to some asynchronous state changes
		// so may not happen immediately
		new ACondition("Wait for enablement", 3000) {
			public boolean test() throws Exception {
				assertTrue(action.isEnabled());
				return true;
			}
		};

		// c) if project has more than one launch config...
		BootLaunchConfigurationDelegate.createConf(javaProject);
		assertEquals(2, element.getLaunchConfigs().size());
		// ... async update may not happen right away...
		new ACondition("Wait for disablement", 3000) {
			public boolean test() throws Exception {
				assertEquals(2, element.getCurrentChildren().size());
				assertFalse(action.isEnabled());
				return true;
			}
		};

	}

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
		selection.setElements(element, mockLocalElement());
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
	public void redebugActionEnablementForMultipleProjects() throws Exception {
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

	@Test
	public void restartActionEnablementForProject() throws Exception {
		doRestartActionEnablementForProjectTest(RunState.RUNNING);
	}

	@Test
	public void redebugActionEnablementForProject() throws Exception {
		doRestartActionEnablementForProjectTest(RunState.DEBUGGING);
	}

	private void doRestartActionEnablementForProjectTest(RunState runOrDebug) throws Exception, CoreException {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		final BootDashElement element = harness.getElementWithName(projectName);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final RunStateAction action = getRunStateAction(runOrDebug);

		//If selection is empty the action must not be enabled
		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		//If selection has one element...
		selection.setElements(element);

		//a) and element has no launch configs...
		assertTrue(element.getLaunchConfigs().isEmpty());
		assertTrue(action.isEnabled());

		//b) and element has multiple launch config
		action.setEnabled(false); // make sure the test won't pass 'by accident'.
		final ILaunchConfiguration c1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final ILaunchConfiguration c2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		new ACondition(2000) {
			public boolean test() throws Exception {
				assertEquals(ImmutableSet.of(c1,c2), element.getLaunchConfigs());
				assertTrue(action.isEnabled());
				return true;
			}
		};

		//b) and element has a single launch config
		action.setEnabled(false); // make sure the test won't pass 'by accident'.
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
	public void restartActionEnablementForMultipleProjects() throws Exception {
		IProject p1 = createBootProject("project1");
		IProject p2 = createBootProject("project2");

		BootDashElement e1 = harness.getElementWithName(p1.getName());
		BootDashElement e2 = harness.getElementWithName(p2.getName());

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final RunStateAction action = getRunStateAction(RunState.RUNNING);

		assertTrue(selection.isEmpty());
		assertFalse(action.isEnabled());

		selection.setElements(e1, e2);

		assertTrue(action.isEnabled());
	}

	@Test
	public void restartActionTargetsChildrenDirectly() throws Exception {
		String projectName = "hohoho";
		IProject project = createBootProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);

		MockMultiSelection<BootDashElement> selection = harness.selection;
		final BootDashElement element = harness.getElementWithName(projectName);
		final ILaunchConfiguration c1 = BootLaunchConfigurationDelegate.createConf(javaProject);
		final ILaunchConfiguration c2 = BootLaunchConfigurationDelegate.createConf(javaProject);
		BootDashElement child1 = harness.getElementFor(c1);
		BootDashElement child2 = harness.getElementFor(c2);

		ImmutableSet<BootDashElement> theChildren = ImmutableSet.of(
				child1, child2
		);
		assertEquals(theChildren, element.getChildren().getValues());

		for (RunState runOrDebug: EnumSet.of(RunState.RUNNING, RunState.DEBUGGING)) {
			final RunOrDebugStateAction action = (RunOrDebugStateAction) getRunStateAction(runOrDebug);
			selection.setElements(/*none*/);
			assertEquals(ImmutableSet.of(), action.getSelectedElements());
			assertEquals(ImmutableSet.of(), action.getTargetElements());

			selection.setElements(element);
			assertEquals(ImmutableSet.of(element), action.getSelectedElements());
			assertEquals(""+runOrDebug, theChildren, action.getTargetElements());

			selection.setElements(element, child1);
			assertEquals(ImmutableSet.of(element, child1), action.getSelectedElements());
			assertEquals(theChildren, action.getTargetElements());
		}
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

	private BootDashElement mockLocalElement() {
		BootDashElement element = mock(BootDashElement.class);
		RunTarget target = RunTargets.LOCAL;
		when(element.getBootDashModel()).thenReturn(harness.getRunTargetModel(RunTargetTypes.LOCAL));
		when(element.getTarget()).thenReturn(target);
		return element;
	}



}
