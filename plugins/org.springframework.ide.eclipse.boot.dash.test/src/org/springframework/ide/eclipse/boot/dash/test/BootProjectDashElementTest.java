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

import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;


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

	public class TestElement extends BootProjectDashElement {

		public TestElement(IProject project, BootDashModel context) {
			super(project, context);
		}

		@Override
		public void launch(String runMode, ILaunchConfiguration conf) {
		}

		@Override
		protected IType[] guessMainTypes() throws CoreException {
			return NO_TYPES;
		}
	}

	private static final IType[] NO_TYPES = {};

	private String projectName;
	private IProject project;
	private IJavaProject javaProject;
	private IType fooType = mockType("demo", "FooApplication");

	private BootDashModel model;
	private TestElement element;
	private UserInteractions ui;


	public BootProjectDashElement createElement(String name) {
		IProject project = mockProject(name, true);
		return spy(new BootProjectDashElement(project, model));
	}

	private IType mockType(String pkg, String name) {
		IType type = mock(IType.class);
		when(type.getElementName()).thenReturn(name);
		when(type.getFullyQualifiedName()).thenReturn(pkg+"."+name);
		when(type.getJavaProject()).thenReturn(javaProject);
		return type;
	}

	@Before
	public void setup() {
		projectName = "foo";
		project = mockProject(projectName, true);
		javaProject = mock(IJavaProject.class);
		model = mock(BootDashModel.class);
		element = spy(new TestElement(project, model));
		when(element.getProject()).thenReturn(project);
		doReturn(javaProject).when(element).getJavaProject();
		ui = mock(UserInteractions.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void restartWithBadArgument() throws Exception {
		element.restart(RunState.INACTIVE, ui);
	}

	@Test
	public void restartNoMainTypes() throws Exception {
		when(element.guessMainTypes()).thenReturn(NO_TYPES);

		element.restart(RunState.RUNNING, ui);

		verify(element).stop(true);
		verify(ui).errorPopup(
				stringContains("Problem"),
				stringContains("Couldn't find a main type in 'foo'")
		);
		verifyNoMoreInteractions(ui);
	}

	@Test
	public void restartOneMainType() throws Exception {
		when(element.guessMainTypes()).thenReturn(new IType[] {fooType});

		element.restart(RunState.RUNNING, ui);

		verify(element).stop(true);
		verify(element).launch(
				eq(ILaunchManager.RUN_MODE),
				argThat(isLaunchableConfig(projectName, "demo.FooApplication")
		));
	}


	private Matcher<ILaunchConfiguration> isLaunchableConfig(final String expectedProject, final String expectedMainType) {
		return new BaseMatcher<ILaunchConfiguration>() {

			@Override
			public boolean matches(Object item) {
				try {
					if (item instanceof ILaunchConfiguration) {
						ILaunchConfiguration conf = (ILaunchConfiguration) item;
						String mainTypeName = conf.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
						String projectName = conf.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
						return projectName.equals(expectedProject)
							&& mainTypeName.equals(expectedMainType);
					}
				} catch (Exception e) {
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("ILaunchConfig{"+expectedProject+", "+expectedMainType+"}");
			}
		};
	}

	public static String stringContains(String... strings) {
		return argThat(stringContainsInOrder(Arrays.asList(strings)));
	}

}
