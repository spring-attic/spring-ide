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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.eclipse.gettingstarted.content.ReferenceApp;

/**
 * @author Kris De Volder
 */
public class ReferenceAppsTests extends TestCase {
	
	public static ReferenceApp[] getReferenceApps() {
		return GettingStartedContent.getInstance().getReferenceApps();
	}
	
	/**
	 * Check that at least some expected samples are available in default, non-modified
	 * STS. There may be more samples than the ones we check for. That should not
	 * cause a test failure (in anticipation more samples will be added.
	 *  
	 * @throws Exception
	 */
	public void testGetSamples() throws Exception {
		
		ReferenceApp[] samples = getReferenceApps();
		
		String[] expected = {
				"spring-mvc-showcase",
				"spring-petclinic",
				"greenhouse",
				"spring-hibernate-cf"
		};

		assertAtLeast(expected, samples);
		for (ReferenceApp s : samples) {
			System.out.println("==== reference app ==== ");
			System.out.println("name : "+s.getName());
			System.out.println("url  : "+s.getHomePage());
			System.out.println();
			System.out.println(s.getDescription());
		}
		
	}

	private void assertAtLeast(String[] expected, ReferenceApp[] samples) {
		Set<String> actual = new HashSet<String>();
		StringBuilder found = new StringBuilder();
		for (ReferenceApp sample : samples) {
			found.append("   "+sample.getName()+"\n");
			actual.add(sample.getName());
		}
		StringBuilder _missing = new StringBuilder();
		for (String expect : expected) {
			if (!actual.contains(expect)) {
				_missing.append("   "+expect+"\n");
			}
		}
		String missing = _missing.toString();
		if (!"".equals(missing)) {
			fail("Expected elements missing:\n"+missing+"Found:\n "+found.toString());
		}
	}

}
