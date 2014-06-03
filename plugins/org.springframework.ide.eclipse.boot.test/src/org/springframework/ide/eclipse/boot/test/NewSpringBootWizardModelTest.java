/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertNotEquals;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroup;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroups;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioInfo;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * Tests whether NewSpringBootWizardModel adequately parses initializer form data.
 * 
 * @author Kris De Volder
 */
public class NewSpringBootWizardModelTest extends TestCase {
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public static NewSpringBootWizardModel parseFrom(String resourcePath) throws Exception {
		URL formUrl = NewSpringBootWizardModelTest.class.getResource("test-form.html");
		return new NewSpringBootWizardModel(new URLConnectionFactory(), formUrl.toString());
	}
	
	public void testParsedRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		RadioGroups radioGroups = model.getRadioGroups();
		assertGroupNames(radioGroups, "type", "packaging", "javaVersion", "language");
	}

	public void testPackagingRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		RadioGroup packagingTypes = model.getRadioGroups().getGroup("packaging");
		assertNotNull(packagingTypes);
		assertGroupValues(packagingTypes, "jar", "war");
		assertEquals("jar", packagingTypes.getDefault().getValue());
	}

	public void testJavaVersionRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		RadioGroup group = model.getRadioGroups().getGroup("javaVersion");
		assertNotNull(group);
		assertGroupValues(group, "1.6", "1.7", "1.8");
		assertEquals("1.7", group.getDefault().getValue());
	}
	
	public void testBuildTypeRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		RadioGroup group = model.getRadioGroups().getGroup("type");
		assertNotNull(group);
		assertGroupValues(group, "gradle.zip", "starter.zip");
		assertEquals("starter.zip", group.getDefault().getValue());
	}
	
	public void testLabels() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		for (RadioGroup group : model.getRadioGroups().getGroups()) {
			String label = group.getLabel();
			assertRealLabel(label);
			
			for (RadioInfo radio : group.getRadios()) {
				label = radio.getLabel();
				assertRealLabel(label);
			}
		}
	}
	
	public void testPrintLabels() throws Exception {
		//print all radios in groups with lable for quick visual inspection.
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		for (RadioGroup group : model.getRadioGroups().getGroups()) {
			String label = group.getLabel();
			System.out.println(group + " -> "+label);
			
			for (RadioInfo radio : group.getRadios()) {
				label = radio.getLabel();
				System.out.println("  " + radio + " -> "+label);
			}
		}
		
	}
	
	/**
	 * Test that radio params are wired up in the model so that selecting them changes the downloadUrl.
	 */
	public void testRadioQueryParams() throws Exception {
		NewSpringBootWizardModel model = parseFrom("test-form.html");
		RadioGroup packaging = model.getRadioGroups().getGroup("packaging");
		LiveVariable<RadioInfo> selection = packaging.getSelection().selection;
		assertEquals("jar", selection.getValue().getValue());
		String urlParam = getUrlParam(model.downloadUrl.getValue(), "packaging");
		assertEquals("jar", urlParam);
		selection.setValue(packaging.getRadio("war"));
	}
	
	public static Map<String, List<String>> getQueryParams(String url) throws Exception {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }

                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }

        return params;
	}	
	
	private String getUrlParam(String url, String name) throws Exception {
		Map<String, List<String>> params = getQueryParams(url);
		List<String> values = params.get(name);
		if (values!=null && !values.isEmpty()) {
			assertEquals(1, values.size());
			return values.get(0);
		}
		return null;
	}

	private void assertRealLabel(String label) {
		assertNotNull(label); //have a label
		assertNotEquals("", label.trim()); //label not empty
		assertTrue(Character.isUpperCase(label.charAt(0))); //'real' label, not just the default taken from the name.
	}

	private void assertGroupValues(RadioGroup group, String... expecteds) {
		RadioInfo[] radios = group.getRadios();
		assertEquals(expecteds.length, radios.length);
		for (int i = 0; i < radios.length; i++) {
			assertEquals(expecteds[i], radios[i].getValue());
		}
	}

	private void assertGroupNames(RadioGroups radioGroups, String... expectNames) {
		List<RadioGroup> groups = radioGroups.getGroups();
		assertEquals(expectNames.length, groups.size());
		for (int i = 0; i < expectNames.length; i++) {
			assertEquals(expectNames[i], groups.get(i).getName());
		}
	}
	
	
	

}
