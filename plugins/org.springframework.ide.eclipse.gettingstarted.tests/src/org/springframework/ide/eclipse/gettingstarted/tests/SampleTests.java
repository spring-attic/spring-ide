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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.wizard.template.util.ExampleProjectsPreferenceModel;
import org.springframework.ide.eclipse.wizard.template.util.NameUrlPair;

/**
 * @author Kris De Volder
 */
public class SampleTests extends TestCase {
	
	public static ArrayList<NameUrlPair> getSamples() {
		return ExampleProjectsPreferenceModel.getInstance().getElements();
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
		
		ArrayList<NameUrlPair> samples = getSamples();
		
		String[] expected = {
				"spring-mvc-showcase|https://github.com/SpringSource/spring-mvc-showcase",
				"spring-hibernate-cf|https://github.com/cloudfoundry-samples/springmvc-hibernate-template"
		};

		assertAtLeast(expected, samples);
		for (NameUrlPair s : samples) {
			System.out.println(s);
		}
		
	}

	private void assertAtLeast(String[] expected, ArrayList<NameUrlPair> samples) {
		Set<String> actual = new HashSet<String>();
		StringBuilder found = new StringBuilder();
		for (NameUrlPair sample : samples) {
			found.append("   "+sample+"\n");
			actual.add(sample.toString());
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
