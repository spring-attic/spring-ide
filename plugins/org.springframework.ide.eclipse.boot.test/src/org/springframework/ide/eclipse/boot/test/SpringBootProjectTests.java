/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

public class SpringBootProjectTests extends TestCase {

	/**
	 * The group id we expect to see for boot starters. Some tests will fail if this doesn't match.
	 * But the infrastructure itself assumes no specific group id.
	 */
	private static final String BOOT_STARTER_GROUP_ID = "org.springframework.boot";

	/**
	 * The version we expect to see for boot starters. Some tests will fail if this doesn't match.
	 * But the infrastructure itself assumes no specific version (should work with different versions
	 * of spring-boot.
	 * 
	 * The version number in here should be the version of spring boot used by projects created 
	 * via the spring intializer app.
	 */
	private static final String BOOT_STARTER_VERSION = "0.5.0.BUILD-SNAPSHOT";

	private static final long MAVEN_POM_REFRESH_TIMEOUT = 20*1000;
	
	private static String projectName;
	private static ISpringBootProject project;
	private static Map<String, SpringBootStarter> knownStarters;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (projectName==null) {
			NewSpringBootWizardModel wizard = new NewSpringBootWizardModel();
			wizard.allowUIThread(true);
			projectName = wizard.getProjectName().getValue();
			wizard.performFinish(new NullProgressMonitor());
			project = SpringBootCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
			
			knownStarters = new HashMap<String, SpringBootStarter>();
			
			for (SpringBootStarter s : project.getKnownStarters()) {
				knownStarters.put(s.getName(), s);
			}
		}
	}
	
	public void testGetKnownBootStarters() throws Exception {
		List<SpringBootStarter> starters = project.getKnownStarters();
		assertContainsStarters(starters, 
				"web",
				"actuator",
				"jdbc"
		);
	}
	
	public void testAddAndRemoveBootStarter() throws Exception {
//		List<SpringBootStarter> starters = project.getBootStarters();
		assertTrue(project.getBootStarters().isEmpty()); //Expect no starters applied initially.
		project.addStarter(knownStarters.get("web"));
		//WARNING: presumably m2e model updates are asynchronous and we don't really know when the changes will be visible!
		//  the next assertion may fail when running at 'full speed'.
		new ACondition("web starter added") {
			public boolean test() throws Exception {
				assertContainsStarters(project.getBootStarters(), "web");
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);
		
		project.removeStarter(knownStarters.get("web"));
		
		new ACondition("web starter removed") {
			public boolean test() throws Exception {
				assertTrue(project.getBootStarters().isEmpty()); //starter should be removed again.
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);
	}

	private void assertContainsStarters(List<SpringBootStarter> starters, String... artifactIds) {
		HashSet<String> expecteds = new HashSet<String>(Arrays.asList(artifactIds));
		for (SpringBootStarter starter : starters) {
			String name = starter.getName();
			assertNotNull(name);
			assertEquals("ArtifactId", SpringBootStarter.AID_PREFIX+name, starter.getDep().getArtifactId());
			assertEquals("GroupId", BOOT_STARTER_GROUP_ID, starter.getDep().getGroupId());
			assertEquals("Version", BOOT_STARTER_VERSION, starter.getDep().getVersion());
			expecteds.remove(starter.getName());
		}
		if (!expecteds.isEmpty()) {
			StringBuilder msg = new StringBuilder("Missing artifacts: \n");
			for (String string : expecteds) {
				msg.append("   "+string+"\n");
			}
			msg.append("Found:\n");
			for (SpringBootStarter found : starters) {
				msg.append("   "+found+"\n");
			}
			fail(msg.toString());
		}
	}
	
	public void testSetStarters() throws Exception {
		assertTrue(project.getBootStarters().isEmpty()); //Expect no starters applied initially.
		
		project.setStarters(knownStarters.values());
		new ACondition("all known starters added") {
			public boolean test() throws Exception {
				assertStarters(project, knownStarters.values());
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);
		
		project.setStarters(new ArrayList<SpringBootStarter>());
		new ACondition("all starters removed") {
			public boolean test() throws Exception {
				assertStarters(project, new HashSet<SpringBootStarter>());
				return true;
			}
		}.waitFor(MAVEN_POM_REFRESH_TIMEOUT);
		
	}

	private void assertStarters(ISpringBootProject project, Collection<SpringBootStarter> _expected) throws Exception {
		HashSet<SpringBootStarter> expected = new HashSet<SpringBootStarter>(_expected);
		
		StringBuilder unexpected = new StringBuilder();
		StringBuilder missing = new StringBuilder();
		for (SpringBootStarter s : project.getBootStarters()) {
			if (!expected.remove(s)) {
				unexpected.append("   "+s+"\n");
			}
		}
		
		for (SpringBootStarter s : expected) {
			missing.append("    "+s+"\n");
		}
		boolean problem = !"".equals(missing.toString()) || !"".equals(unexpected.toString());
		if (problem) {
			fail(
				"Missing: \n"+missing+
				"Unexpected: \n"+unexpected
			);
		}
	}
	
	
}
