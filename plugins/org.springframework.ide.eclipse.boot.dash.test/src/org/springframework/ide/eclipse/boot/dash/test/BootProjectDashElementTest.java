/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;


/**
 * Unit tests some of the methods that don't have great coverage yet
 * after running BootDashModelTests. These methods are harder to test
 * from the model (which does only minimal mocking).
 *
 * We perform focussed tests here using Mockito to essentially just test
 * those methods in isolation here using mocks for anything it uses.
 *
 * @author Kris De Volder
 */
public class BootProjectDashElementTest extends Mocks {

	public static class TestElement extends BootProjectDashElement {

		public TestElement(IProject project, LocalBootDashModel context, IScopedPropertyStore<IProject> projectProperties, BootDashElementFactory factory) {
			super(project, context, projectProperties, factory);
		}

		@Override
		public void launch(String runMode, ILaunchConfiguration conf) {
		}

		@Override
		public IType[] guessMainTypes() throws CoreException {
			return NO_TYPES;
		}
	}

	private static final IType[] NO_TYPES = {};

	public static TestElement createElement(LocalBootDashModel model, IJavaProject javaProject, RunTarget runTarget, IScopedPropertyStore<IProject> projectProperties) {
		BootDashElementFactory factory = mock(BootDashElementFactory.class);
		IProject project = javaProject.getProject();
		TestElement element = spy(new TestElement(project, model, projectProperties, factory));
		when(element.getTarget()).thenReturn(runTarget);
		doReturn(javaProject).when(element).getJavaProject();
		return element;
	}

	public static IType mockType(IJavaProject javaProject, String pkg, String name) {
		IType type = mock(IType.class);
		when(type.getElementName()).thenReturn(name);
		when(type.getFullyQualifiedName()).thenReturn(pkg+"."+name);
		when(type.getJavaProject()).thenReturn(javaProject);
		return type;
	}

	@Test(expected=IllegalArgumentException.class)
	public void restartWithBadArgument() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);

		element.restart(RunState.INACTIVE, ui);
	}

	public static IJavaProject mockJavaProject(IProject project) {
		String projectName = project.getName();
		IJavaProject jp = mock(IJavaProject.class);
		when(jp.getElementName()).thenReturn(projectName);
		when(jp.getProject()).thenReturn(project);
		return jp;
	}

	@Test
	public void restartNoMainTypes() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);

		when(element.guessMainTypes()).thenReturn(NO_TYPES);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();
		verify(ui).errorPopup(
				stringContains("Problem"),
				stringContains("Couldn't find a main type")
		);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void restartOneMainType() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		IType type = mockType(javaProject, "demo", "FooApplication");

		when(element.guessMainTypes()).thenReturn(new IType[] {type});
		when(runTarget.createLaunchConfig(javaProject, type)).thenReturn(conf);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();;
		verify(element).launch(ILaunchManager.RUN_MODE, conf);
		verifyZeroInteractions(ui);
	}

	@Test
	public void restartTwoMainTypes() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);
		IType fooType = mockType(javaProject, "demo", "FooApplication");
		IType barType = mockType(javaProject, "demo", "BarApplication");

		when(element.guessMainTypes()).thenReturn(new IType[] {fooType, barType});
		when(ui.chooseMainType(
				argThat(arrayContaining(fooType, barType)),
				any(String.class),
				any(String.class)
		)).thenReturn(barType);
		when(runTarget.createLaunchConfig(javaProject, barType)).thenReturn(conf);

		element.restart(RunState.RUNNING, ui);

		verify(element).stopSync();
		verify(ui).chooseMainType(any(IType[].class),
				stringContains("Choose"),
				stringContains("Choose", projectName)
		);
		verify(element).launch(ILaunchManager.RUN_MODE, conf);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void openConfigWithNoExistingConfs() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		when(runTarget.createLaunchConfig(javaProject, null)).thenReturn(conf);
		doReturn(RunState.INACTIVE).when(element).getRunState();

		element.openConfig(ui);

		verify(ui).openLaunchConfigurationDialogOnGroup(conf, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void openConfigWithOneExistingConfs() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf = mock(ILaunchConfiguration.class);

		when(runTarget.getLaunchConfigs(element)).thenReturn(Arrays.asList(conf));
		doReturn(RunState.INACTIVE).when(element).getRunState();

		element.openConfig(ui);

		verify(ui).openLaunchConfigurationDialogOnGroup(conf, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void openConfigWithTwoExistingConfs() throws Exception {
		String projectName = "fooProject";
		IScopedPropertyStore<IProject> projectProperties = new MockPropertyStore<IProject>();
		LocalBootDashModel model = mock(LocalBootDashModel.class);
		IProject project = mockProject(projectName, true);
		IJavaProject javaProject = mockJavaProject(project);
		RunTarget runTarget = mock(RunTarget.class);
		TestElement element = createElement(model, javaProject, runTarget, projectProperties);
		UserInteractions ui = mock(UserInteractions.class);
		ILaunchConfiguration conf1 = mock(ILaunchConfiguration.class);
		ILaunchConfiguration conf2 = mock(ILaunchConfiguration.class);

		when(runTarget.getLaunchConfigs(element)).thenReturn(Arrays.asList(conf1, conf2));
		doReturn(RunState.INACTIVE).when(element).getRunState();
		when(ui.chooseConfigurationDialog(anyString(), anyString(), listThat(hasItems(conf1, conf2))))
			.thenReturn(conf2);

		element.openConfig(ui);

		verify(ui).chooseConfigurationDialog(
				stringContains("Choose", "Configuration"),
				stringContains("Several"),
				(List<ILaunchConfiguration>) any()
		);
		verify(ui).openLaunchConfigurationDialogOnGroup(conf2, IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		verifyNoMoreInteractions(ui);
	}

	private <T> List<T> listThat(Matcher<Iterable<T>> iterMatcher) {
		Object untyped = iterMatcher;
		@SuppressWarnings("unchecked")
		Matcher<List<T>> listMatcher = (Matcher<List<T>>) untyped;
		return argThat(listMatcher);
	}

	public static String stringContains(String... strings) {
		return argThat(stringContainsInOrder(Arrays.asList(strings)));
	}

}
