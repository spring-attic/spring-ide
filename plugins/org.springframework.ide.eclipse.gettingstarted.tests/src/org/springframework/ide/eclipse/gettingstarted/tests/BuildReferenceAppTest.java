/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.tests;

import static org.springframework.ide.eclipse.gettingstarted.importing.ImportUtils.importConfig;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.assertNoErrors;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.getProject;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.setAutoBuilding;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.ReferenceApp;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

/**
 * A BuildSample test checks that a particular sample project builds properly
 * when it gets imported into STS.
 * 
 * This test class is intended to be instantiated with data about a particular
 * sample project. It provides a static suite method that fetches the samples
 * and creates one test for each sample.
 * 
 * @author Kris De Volder
 */
public class BuildReferenceAppTest extends TestCase {

	private static final int SECOND = 1000;
	private static final int MINUTE = 60*SECOND;
	
	////// Data defining the sample under test ////
	private ReferenceApp item;
	private BuildType buildtype;
	///////////////////////////////////////////////

	public BuildReferenceAppTest(ReferenceApp item, BuildType buildtype) {
		super(item.getName()+"-"+buildtype.name());
		this.item = item;
		this.buildtype = buildtype;
	}

	@Override
	protected void runTest() throws Throwable {
		setAutoBuilding(false);
		try {
			System.out.println("=== reference app build test ===");
			System.out.println("name   : "+item.getName());
			System.out.println("type    : "+buildtype);
			System.out.println();
			
			String projectName = item.getName();
			CodeSet codeset = item.getCodeSet();
			IRunnableWithProgress importOp = buildtype.getImportStrategy().createOperation(importConfig(
					/*location*/
					Platform.getLocation().append(projectName),
					/*name*/
					projectName,
					/*codeset*/
					codeset
			));
			
			importOp.run(new NullProgressMonitor());
	
			//TODO: we are not checking if there are extra projects beyond the expected one.
			IProject project = getProject(projectName);
			assertNoErrors(project);
		} catch (Throwable e) {
			//Shorter stacktrace for somewhat nicer looking test failures on bamboo
			throw ExceptionUtil.getDeepestCause(e);
		}
	}
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(BuildReferenceAppTest.class.getName());
		ReferenceApp[] items = ReferenceAppsTests.getReferenceApps();

		for (ReferenceApp item : items) {
			List<BuildType> buildTypes = item.getBuildTypes();
			for (BuildType bt : buildTypes) {
				suite.addTest(new BuildReferenceAppTest(item, bt));
			}
		}
		
		return suite;
	}

}
