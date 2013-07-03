/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.tests;

import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.assertNoErrors;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.getProject;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportConfiguration;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

/**
 * An instance of this test verifies that a codesets for a given 
 * guide imports and builds cleanly with with a given build
 * tool. 
 * <p>
 * A static suite method is provided to create a suite that has
 * a test instance for each valid guide, codeset and buildtool 
 * combination.
 * 
 * @author Kris De Volder
 */
public class ZBuildGuidesTest extends GuidesTestCase {
	
	//To flush out more dependency problems enable this: Be warned that it
	// will take a lot longer to run each test!
	private static final boolean CLEAR_MAVEN_CACHE = false;
	
	//Note the funny name of this class is an attempt to
	// show test results at the bottom on bamboo builds.
	// It looks like the tests reports are getting sorted
	// alphabetically.
	
	private CodeSet codeset;
	private BuildType buildType;

	public ZBuildGuidesTest(GithubRepoContent guide, CodeSet codeset, BuildType buildType) {
		super(guide);
		setName(getName()+"-"+codeset.getName()+"-"+buildType);
		this.codeset = codeset;
		this.buildType = buildType;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (buildType==BuildType.MAVEN && CLEAR_MAVEN_CACHE) {
			File userHome = new File(System.getProperty("user.home"));
			File m2 = new File(userHome, ".m2");
			if (m2.isDirectory()) {
				FileUtils.deleteQuietly(m2);
			}
		}
		System.out.println(">>> Setting up "+getName());
		//Clean stuff from previous test: Delete any projects and their contents.
		// We need to do this because imported maven and gradle projects will have the same name.
		// And this cause clashes / errors.
	
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : allProjects) {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			project.delete(/*content*/true, /*force*/true, new NullProgressMonitor());
		}
		System.out.println("<<< Setting up "+getName());
	}
	
	@Override
	protected void runTest() throws Throwable {
		System.out.println(">>> Running "+getName());
		
		try {
			System.out.println("=== codeset build test ===");
			System.out.println("guide   : "+guide.getName());
			System.out.println("codeset : "+codeset.getName());
			System.out.println("type    : "+buildType);
			System.out.println();
			
			ImportConfiguration importConf = ImportUtils.importConfig(guide, codeset);
			String projectName = importConf.getProjectName();
			IRunnableWithProgress importOp = buildType.getImportStrategy().createOperation(importConf);
			importOp.run(new NullProgressMonitor());
	
			//TODO: we are not checking if there are extra projects beyond the expected one.
			IProject project = getProject(projectName);
			assertNoErrors(project);
		} catch (Throwable e) {
			//Shorter stacktrace for somewhat nicer looking test failures on bamboo
			throw ExceptionUtil.getDeepestCause(e);
		} finally {
			System.out.println("<<< Running "+getName());
		}
	}
	
	static boolean zipLooksOk(GithubRepoContent g) {
		try {
			GuidesStructureTest.validateZipStructure(g);
			return true;
		} catch (Throwable e) {
		}
		return false;
	}
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(ZBuildGuidesTest.class.getName());
		for (GithubRepoContent g : GuidesTests.getGuides()) {
			if (!g.getName().contains("android")) {
				//Skipping android tests for now... lots of problems there.
				if (zipLooksOk(g)) {
					//Avoid running build tests for zips that look like they have 'missing parts'
					for (CodeSet cs : g.getCodeSets()) {
						List<BuildType> buildTypes = cs.getBuildTypes();
						for (BuildType bt : buildTypes) {
							//Don't run tests for things we haven't yet implemented support for.
							if (bt.getImportStrategy().isSupported()) {
								GuidesTestCase test = new ZBuildGuidesTest(g, cs, bt);
								suite.addTest(test);
							}
						}
					}
				}
			}
		}
		return suite;
	}

}
