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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuides;

/**
 * @author Kris De Volder
 */
public class GuidesTests extends TestCase {

	/** 
	 * Expected Guide instances represent some expected data about a Guide than can be
	 * compared to an actual Guide data instance.
	 */
	private class EG {
		String name;
		String descriptionFragment;
		public EG(String name, String descriptionFragment) {
			super();
			this.name = name;
			this.descriptionFragment = descriptionFragment;
		}

		public boolean match(GettingStartedGuide actual) {
			if (!this.name.equals(actual.getName())) {
				return false;
			} else if (descriptionFragment!=null) {
				return actual.getDescription().contains(descriptionFragment);
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "-----------\n"+name +"\n"+descriptionFragment+"\n";
		}
	}

	/**
	 * Check that at least some expected samples are available in default, non-modified
	 * STS. There may be more samples than the ones we check for. That should not
	 * cause a test failure (in anticipation more samples will be added.
	 *  
	 * @throws Exception
	 */
	public void testGetSamples() throws Exception {
		
		//TODO: samples should have a short description blurb.
		
		GettingStartedGuide[] guides = getGuides();
		
		EG[] expected = {
				new EG(
						 "gs-authenticating-ldap",
						 "authenticating a user against an LDAP server"
				),
				new EG(
						"gs-consuming-rest-android",
						"RESTful services on Android"				
				)
		};

		assertAtLeast(expected, guides);
	}
	
	public void testDownloadZips() throws Exception {
		GettingStartedGuide[] guides = getGuides();
		
		for (GettingStartedGuide guide : guides) {
			System.out.println("=== guide: "+guide.getName()+" ====");
			DownloadableItem zip = guide.getZip();
			File zipFile = zip.getFile();
			assertTrue(zipFile.exists());
			ZipFile zipper = new ZipFile(zipFile);
			try {
				Enumeration<? extends ZipEntry> entries = zipper.entries();
				while (entries.hasMoreElements()) {
					ZipEntry e = entries.nextElement();
					System.out.println(e.getName());
				}
			} finally {
				zipper.close();
			}
		}
		
	}

	private GettingStartedGuide[] getGuides() {
		return GettingStartedGuides.getInstance().getAll();
	}

	private void assertAtLeast(EG[] expected, GettingStartedGuide[] guides) {
		StringBuilder _missing = new StringBuilder();
		StringBuilder found = new StringBuilder();
		for (GettingStartedGuide g : guides) {
			found.append(g);
		}
		for (EG expect : expected) {
			boolean ok = false;
			for (int i = 0; i < guides.length && !ok; i++) {
				ok = expect.match(guides[i]);
			}
			if (!ok) {
				_missing.append(expect);
			}
		}
		String missing = _missing.toString();
		if (!"".equals(missing)) {
			fail("***Expected elements missing:\n"+missing+"***Found:\n "+found.toString());
		}
	}

	
}
