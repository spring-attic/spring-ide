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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.JavaUtils;
import org.springframework.ide.eclipse.boot.test.util.LaunchResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


public class BootJarPackagingTest extends StsTestCase {

	private JavaUtils java = new JavaUtils();

	@Override
	protected String getBundleName() {
		return Activator.BUNDLE_ID;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		StsTestUtil.deleteAllProjects();
		StsTestUtil.setAutoBuilding(false);
	}

	public void testSimple() throws Exception {
		UserInteractions ui = Mockito.mock(UserInteractions.class);
		BootProjectTestHarness harness = getHarness();
		IProject project = harness.createBootProject("simple-boot");
		createFile(project, "src/main/java/demo/Greeter.java",
				"package demo;\n" +
				"\n" +
				"import org.springframework.boot.CommandLineRunner;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class Greeter implements CommandLineRunner {\n" +
				"\n" +
				"	@Override\n" +
				"	public void run(String... arg0) throws Exception {\n" +
				"		System.out.println(\"Hello World!\");\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);
		StsTestUtil.assertNoErrors(project); // Builds the project
		File jarFile = packageAsJar(project, ui);
		LaunchResult result = java.runJar(jarFile);
		assertContains("Hello World!", result.out);
		assertEquals(0, result.terminationCode);
	}

	private File packageAsJar(IProject project, UserInteractions ui) throws Exception {
		CloudApplicationArchiverStrategy strategy = CloudApplicationArchiverStrategies.packageAsJar(project, ui);
		ICloudApplicationArchiver archiver = strategy.getArchiver(new NullProgressMonitor());
		assertNotNull(archiver);
		File jar = archiver.getApplicationArchive(new NullProgressMonitor());
		assertNotNull(jar);
		assertTrue(jar.isFile());
		return jar;
	}

	private BootProjectTestHarness getHarness() {
		return new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	}

}
