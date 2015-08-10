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

import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;

import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Assert;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import junit.framework.TestCase;

public class DevToolsStarterTests extends TestCase {

	private static final long MAVEN_POM_REFRESH_TIMEOUT = 3*60*1000;

	BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		StsTestUtil.deleteAllProjects();
	}

	public void testAddAndRemoveDevTools() throws Exception {
		final ISpringBootProject project = SpringBootCore.create(
				projects.createBootProject("dev-starters-tester", bootVersionAtLeast("1.3.0")));
		StsTestUtil.setAutoBuilding(true);
		List<SpringBootStarter> knownStarters = project.getKnownStarters();
		assertStarter(knownStarters, "devtools");

		SpringBootStarter devtools = getStarter(knownStarters, "devtools");
		assertNoStarter(project.getBootStarters(), "devtools");

		project.addStarter(devtools);
		new ACondition("Wait for 'devtools' starter to be added") {
			public boolean test() throws Exception {
				assertStarter(project.getBootStarters(), "devtools");
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);

		project.removeStarter(devtools);
		new ACondition("Wait for 'devtools' starter to be removed") {
			public boolean test() throws Exception {
				assertNoStarter(project.getBootStarters(), "devtools");
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);

	}

	private SpringBootStarter getStarter(List<SpringBootStarter> knownStarters, String name) {
		for (SpringBootStarter s : knownStarters) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}

	public static void assertNoStarter(List<SpringBootStarter> starters, String starterName) {
		for (SpringBootStarter s : starters) {
			if (s.getName().equals(starterName)) {
				Assert.fail("Not expecting a starter '"+starterName+"' but found it: "+s);
			}
		}
	}

	public static void assertStarter(List<SpringBootStarter> starters, String expectedName) {
		StringBuilder found = new StringBuilder();
		for (SpringBootStarter s : starters) {
			found.append(s+"\n");
			if (s.getName().equals(expectedName)) {
				return;
			}
		}
		Assert.fail("Expected starter with name '"+expectedName+"' but found:\n"+
				found.toString());
	}

}
