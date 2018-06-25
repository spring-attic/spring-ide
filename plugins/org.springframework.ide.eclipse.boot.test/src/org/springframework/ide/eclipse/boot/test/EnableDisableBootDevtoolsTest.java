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
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.ui.EnableDisableBootDevtools;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import static org.springframework.ide.eclipse.boot.ui.EnableDisableBootDevtools.*;

import java.util.List;

public class EnableDisableBootDevtoolsTest {

	//TODO: better test coverage for EnableDisableBootDevtools

	private static final long MAVEN_POM_REFRESH_TIMEOUT = 3*60*1000;

	/**
	 * We want the devtools enablement action not to depend on initializr service.
	 * So lets explicitly mock it with a 'unavalailable' service.
	 */
	private InitializrService initializr = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			return null;
		}

		@Override
		public String getPom(String bootVersion, List<String> starters) throws Exception {
			throw new UnsupportedOperationException();
		}
	};
	private SpringBootCore springBootCore = new SpringBootCore(initializr);
	private BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	private EnableDisableBootDevtools delegate = new EnableDisableBootDevtools(springBootCore);
	private IAction action = mock(IAction.class);

	@Rule
	public AutobuildingEnablement autobuilding = new AutobuildingEnablement(true);

	@Before
	public void setUp() throws Exception {
		StsTestUtil.deleteAllProjects();
	}

	@Test
	public void addBootDevtools() throws Exception {
		final ISpringBootProject project = springBootCore.project(
				projects.createBootWebProject("dev-starters-tester", bootVersionAtLeast("1.3.0")));

		delegate.selectionChanged(action, selectionOf(project));

		verify(action).setText("Add Boot Devtools");
		verify(action).setEnabled(true);

		assertNoDependency(project.getDependencies(), SPRING_BOOT_DEVTOOLS_GID, SPRING_BOOT_DEVTOOLS_AID);

		delegate.run(action);

		new ACondition("Wait for 'devtools' starter to be added") {
			@Override
			public boolean test() throws Exception {
				assertDependency(project.getDependencies(), SPRING_BOOT_DEVTOOLS_GID, SPRING_BOOT_DEVTOOLS_AID);
				return true;
			}

		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);

	}

	@Test
	public void removeBootDevtools() throws Exception {
		final ISpringBootProject project = springBootCore.project(
				projects.createBootWebProject("dev-starters-tester",
						bootVersionAtLeast("1.3.0"),
						withStarters("devtools")
				)
		);
		delegate.selectionChanged(action, selectionOf(project));

		verify(action).setText("Remove Boot Devtools");
		verify(action).setEnabled(true);

		assertDependency(project.getDependencies(), SPRING_BOOT_DEVTOOLS_GID, SPRING_BOOT_DEVTOOLS_AID);

		delegate.run(action);

		new ACondition("Wait for 'devtools' starter to be removed") {
			@Override
			public boolean test() throws Exception {
				assertNoDependency(project.getDependencies(), SPRING_BOOT_DEVTOOLS_GID, SPRING_BOOT_DEVTOOLS_AID);
				return true;
			}


		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);

	}

	///////////////////////////////////////////////////////////////////////////////

	private ISelection selectionOf(ISpringBootProject project) {
		return selectionOf(project.getProject());
	}

	private ISelection selectionOf(IProject project) {
		return new StructuredSelection(new Object[] {project});
	}

	private void assertDependency(List<IMavenCoordinates> dependencies, String gid, String aid) {
		for (IMavenCoordinates d : dependencies) {
			if (gid.equals(d.getGroupId()) && aid.equals(d.getArtifactId())) {
				return; //okay!
			}
		}
		fail("No dependency "+gid+":"+aid+" in "+dependencies);
	}

	private void assertNoDependency(List<IMavenCoordinates> dependencies, String gid, String aid) {
		for (IMavenCoordinates d : dependencies) {
			if (gid.equals(d.getGroupId()) && aid.equals(d.getArtifactId())) {
				fail("Dependency found but not expected "+gid+":"+aid+" in "+dependencies);
			}
		}
	}
}
