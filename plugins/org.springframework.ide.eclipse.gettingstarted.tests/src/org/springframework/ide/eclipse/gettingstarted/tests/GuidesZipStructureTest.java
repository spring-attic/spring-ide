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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.gettingstarted.content.CodeSet;
import org.springframework.ide.gettingstarted.content.CodeSet.CodeSetEntry;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;

/**
 * This tests performs a quick validation that downloaded Guides zip file contains
 * all of the expected elements. This merely checks that certain files and folders
 * exists. It does not try to actually validate the contents of the files
 * or folders.
 * 
 * @author Kris De Volder
 */
public class GuidesZipStructureTest extends GuidesTestCase {

	private static final int SECOND = 1000;
	private static final int MINUTE = 60*SECOND;

	public GuidesZipStructureTest(GettingStartedGuide guide) {
		super(guide);
	}

	@Override
	protected void runTest() throws Throwable {
		System.out.println("=== guide: "+guide.getName()+" ====");
		
		File zipFile = guide.getZip().getFile();
		assertTrue("Could not download "+ guide.getZip(),
				zipFile!=null && zipFile.exists());
		
		CodeSet allData = CodeSet.fromZip("all", guide.getZip(), new Path("/"));
		assertTrue("ZipFile is empty", allData.exists());
		
		CodeSet.Processor<Void> printEntry = new CodeSet.Processor<Void>() {
			public Void doit(CodeSetEntry e) {
				System.out.println(e);
				return null;
			};
		};
		allData.each(printEntry);
		
		assertFolder(allData, guide.getRootPath());
		assertFile(allData, guide.getRootPath().append("README.md"));
		
		String previousCodesetName = null;
		Boolean previousSetIsGradle = null;
		Boolean previousSetIsMaven = null;

		List<CodeSet> codeSets = guide.getCodeSets();
		assertEquals("Guides should provide 2 codesets", 2, codeSets.size());
		for (CodeSet codeset : codeSets) {
			String codesetName = codeset.getName();
			System.out.println("=== code set: "+codesetName);
			codeset.each(printEntry);
			assertTrue("No '"+codeset.getName()+" codeset", codeset.exists());
			
			boolean isGradle = codeset.hasFile("build.gradle");
			boolean isMaven = codeset.hasFile("pom.xml");
			assertTrue("Codeset "+codesetName+" has neither a build.gradle nor a pom.xml", isGradle||isMaven);

			if (previousCodesetName!=null) {
				//Ensure next codeset is consistent with previous one
				assertTrue("One of the codesets ("+previousCodesetName+", "+codesetName
						+") has a 'pom.xml' but the other one does not",
						previousSetIsMaven==isMaven);
				assertTrue("One of the codesets ("+previousCodesetName+", "+codesetName
						+") has a 'build.gradle' but the other one does not",
						previousSetIsGradle==isGradle);
			}
			
			previousCodesetName = codesetName;
			previousSetIsGradle = isGradle;
			previousSetIsMaven = isMaven;
		}
		
	}
	
	private void assertFolder(CodeSet content, IPath path) {
		assertTrue("Folder "+path+" not found in "+content, content.hasFolder(path));
	}
	private void assertFile(CodeSet content, IPath path) {
		assertTrue("File "+path+" not found in "+content, content.hasFile(path));
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(GuidesZipStructureTest.class.getName());
		
		GettingStartedGuide[] guides = GuidesTests.getGuides();
		for (GettingStartedGuide guide : guides) {
			suite.addTest(new GuidesZipStructureTest(guide));
		}
		
		return suite;
	}

}
