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

import junit.framework.TestCase;

import org.springframework.ide.eclipse.gettingstarted.content.Describable;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;

/**
 * @author Kris De Volder
 */
public class GuidesTests extends TestCase {

	/** 
	 * EG (Expected Guide) instances represent some expected data about a Guide than can be
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

		public boolean match(GithubRepoContent actual) {
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
	 * Check that at least some expected guides are available in default, non-modified
	 * STS. There may be more than the ones we check for. That should not
	 * cause a test failure (in anticipation more guides will be added.
	 *  
	 * @throws Exception
	 */
	public void testGetGuides() throws Exception {
		
		GithubRepoContent[] guides = getGuides();
		
		EG[] expected = {
				new EG(
						 "gs-authenticating-ldap",
						 "LDAP"
				),
				new EG(
						"gs-accessing-facebook",
						"Facebook"
				)
		};

		assertAtLeast(expected, guides);
	}
	
//	public void testDownloadZips() throws Exception {
//		GettingStartedGuide[] guides = getGuides();
//		
//		for (GettingStartedGuide guide : guides) {
//			System.out.println("=== guide: "+guide.getName()+" ====");
//			DownloadableItem zip = guide.getZip();
//			File zipFile = zip.getFile();
//			assertTrue(zipFile.exists());
//			ZipFile zipper = new ZipFile(zipFile);
//			try {
//				Enumeration<? extends ZipEntry> entries = zipper.entries();
//				while (entries.hasMoreElements()) {
//					ZipEntry e = entries.nextElement();
//					System.out.println(e.getName());
//				}
//			} finally {
//				zipper.close();
//			}
//		}
//	}
	
	public static GithubRepoContent[] getGuides() {
		return GettingStartedContent.getInstance().getGuides();
	}

	private void assertAtLeast(EG[] expected, GithubRepoContent[] guides) {
		StringBuilder _missing = new StringBuilder();
		StringBuilder found = new StringBuilder();
		for (Describable g : guides) {
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
