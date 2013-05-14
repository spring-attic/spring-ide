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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.gettingstarted.content.BuildType;
import org.springframework.ide.gettingstarted.content.CodeSet;
import org.springframework.ide.gettingstarted.content.importing.ImportConfiguration;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;

import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;
import org.springsource.ide.eclipse.gradle.core.util.expression.LiveExpression;

import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.*;

/**
 * An instace of this test verifies that a codesets for a given 
 * guide imports and builds cleanly with with a given build
 * tool. 
 * <p>
 * A static suite method is provided to create a suite that has
 * a test instance for each valid guide, codeset and buildtool 
 * combination.
 * 
 * @author Kris De Volder
 */
public class BuildGuidesTest extends GuidesTestCase {

	private CodeSet codeset;
	private BuildType buildType;

	public BuildGuidesTest(GettingStartedGuide guide, CodeSet codeset, BuildType buildType) {
		super(guide);
		setName(getName()+"-"+codeset.getName()+"-"+buildType);
		this.codeset = codeset;
		this.buildType = buildType;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		StsTestUtil.deleteAllProjects();
	}
	
	@Override
	protected void runTest() throws Throwable {
		System.out.println("=== codeset build test ===");
		System.out.println("guide   : "+guide.getName());
		System.out.println("codeset : "+codeset.getName());
		System.out.println("type    : "+buildType);
		System.out.println();
		
		String projectName = guide.getName() + "-" + codeset.getName();
		IRunnableWithProgress importOp = buildType.getImportStrategy().createOperation(importConfig(
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
		
	}
	
	private static ImportConfiguration importConfig(final IPath location, final String projectName, final CodeSet codeset) {
		ImportConfiguration conf = new ImportConfiguration() {

			@Override
			public LiveExpression<String> getLocationField() {
				return LiveExpression.constant(location.toString());
			}

			@Override
			public LiveExpression<String> getProjectNameField() {
				return LiveExpression.constant(projectName);
			}

			@Override
			public LiveExpression<CodeSet> getCodeSetField() {
				return LiveExpression.constant(codeset);
			}
		};
		return conf;
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(BuildGuidesTest.class.getName());
		for (GettingStartedGuide g : GuidesTests.getGuides()) {
		//	if (g.getName().contains("facebook")) {
				for (CodeSet cs : g.getCodeSets()) {
					List<BuildType> buildTypes = cs.getBuildTypes();
					for (BuildType bt : buildTypes) {
						if (bt.getImportStrategy().isSupported()) {
							suite.addTest(new BuildGuidesTest(g, cs, bt));
						}
					}
				}
			}
		//}
		return suite;
	}
	

}
