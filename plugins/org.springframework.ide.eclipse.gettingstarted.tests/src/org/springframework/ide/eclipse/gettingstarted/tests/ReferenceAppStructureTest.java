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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.ReferenceApp;
import org.springframework.ide.eclipse.gettingstarted.util.UIThreadDownloadDisallowed;

/**
 * This tests performs a quick validation that downloaded zip file contains
 * all of the expected elements. This merely checks that certain files and folders
 * exists. It does not try to actually validate the contents of the files
 * or folders.
 * 
 * @author Kris De Volder
 */
public class ReferenceAppStructureTest extends TestCase {
	
	private ReferenceApp app;

	public ReferenceAppStructureTest(ReferenceApp app) {
		super(app.getName());
		this.app = app;
	}

	@Override
	protected void runTest() throws Throwable {
		System.out.println("=== validating reference app zip structure: "+app.getName()+" ====");
		validateZipStructure(app);
		String description = app.getDescription();
		assertTrue("Reference App '"+app.getName()+"' has no description\n" +
				"A description must be provided in the metadata file, or as a " +
				"description of the github repo", description!=null && !"".equals(description.trim()));
	}

	public static void validateZipStructure(ReferenceApp app) throws IOException, Exception {
		File zipFile = app.getZip().getFile();
		assertTrue("Could not download "+ app.getZip(),
				zipFile!=null && zipFile.exists());
		
		CodeSet allData = CodeSet.fromZip("all", app.getZip(), new Path("/"));
		assertTrue("ZipFile is empty", allData.exists());
		
//		CodeSet.Processor<Void> printEntry = new CodeSet.Processor<Void>() {
//			public Void doit(CodeSetEntry e) {
//				System.out.println(e);
//				return null;
//			};
//		};
//		allData.each(printEntry);
		
		assertFolder(allData, app.getRootPath());
		String readme = app.getReadme();
		assertNotNull("No README.md file", readme);
		
		CodeSet codeset = app.getCodeSet();
		String codesetName = codeset.getName();
		assertTrue("No '"+codeset.getName()+"' codeset", codeset.exists());
		
		boolean isGradle = codeset.hasFile("build.gradle");
		boolean isMaven = codeset.hasFile("pom.xml");
		assertTrue("Codeset "+codesetName+" has neither a build.gradle nor a pom.xml", isGradle||isMaven);

	}
	
	private static void assertFolder(CodeSet content, IPath path) {
		assertTrue("Folder "+path+" not found in "+content, content.hasFolder(path));
	}
	private static void assertFile(CodeSet content, IPath path) throws UIThreadDownloadDisallowed {
		assertTrue("File "+path+" not found in "+content, content.hasFile(path));
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(ReferenceAppStructureTest.class.getName());
		
		ReferenceApp[] apps = ReferenceAppsTests.getReferenceApps();
		for (ReferenceApp app : apps) {
			suite.addTest(new ReferenceAppStructureTest(app));
		}
		
		return suite;
	}
	

}
