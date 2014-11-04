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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroup;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroups;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioInfo;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
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
		URL formUrl = resourceUrl(resourcePath);
		return new NewSpringBootWizardModel(new URLConnectionFactory(), formUrl.toString(), "application/json");
	}

	public static URL resourceUrl(String resourcePath) {
		URL formUrl = NewSpringBootWizardModelTest.class.getResource(resourcePath);
		return formUrl;
	}
	
	public void testParsedRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
		RadioGroups radioGroups = model.getRadioGroups();
		assertGroupNames(radioGroups, "type", "packaging", "javaVersion", "language", "bootVersion");
	}

	public void testPackagingRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
		RadioGroup packagingTypes = model.getRadioGroups().getGroup("packaging");
		assertNotNull(packagingTypes);
		assertGroupValues(packagingTypes, "jar", "war");
		assertEquals("jar", packagingTypes.getDefault().getValue());
	}

	public void testJavaVersionRadios() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
		RadioGroup group = model.getRadioGroups().getGroup("javaVersion");
		assertNotNull(group);
		assertGroupValues(group, "1.6", "1.7", "1.8");
		assertEquals("1.7", group.getDefault().getValue());
	}
	
	public void testBuildTypeRadios() throws Exception {
		String mavenId = "maven-project";
		String gradleId = "gradle-project";
		String jsonFile = "initializr.json";
		NewSpringBootWizardModel model = parseFrom(jsonFile);
		String starterZipUrl = resourceUrl(jsonFile).toURI().resolve("/starter.zip").toString();
		assertEquals(starterZipUrl, model.baseUrl.getValue());
		
		RadioGroup group = model.getRadioGroups().getGroup("type");
		assertNotNull(group);
		assertGroupValues(group, mavenId, gradleId);
		assertEquals(mavenId, group.getDefault().getValue());
		
		group.getSelection().selection.setValue(group.getRadio(mavenId));
		assertEquals(BuildType.MAVEN, model.getBuildType());
		assertEquals(starterZipUrl, model.baseUrl.getValue());
		
		group.getSelection().selection.setValue(group.getRadio(gradleId));
		assertEquals(BuildType.GRADLE, model.getBuildType());
		assertEquals(starterZipUrl, model.baseUrl.getValue());
	}
	
	public void testBuildTypeRadiosVariant() throws Exception {
		//Hypothetical variant where the json "types" lists different actions for maven and gradle zip 
		
		String mavenId = "maven-project";
		String gradleId = "gradle-project";
		String jsonFile = "initializr-variant.json";
		
		String mavenZipUrl = resourceUrl(jsonFile).toURI().resolve("/maven.zip").toString();
		String gradleZipUrl = resourceUrl(jsonFile).toURI().resolve("/gradle.zip").toString();
		
		NewSpringBootWizardModel model = parseFrom(jsonFile);
		
		RadioGroup group = model.getRadioGroups().getGroup("type");
		assertNotNull(group);
		assertGroupValues(group, gradleId, mavenId);
		assertEquals(mavenId, group.getDefault().getValue());
		assertEquals(mavenZipUrl, model.baseUrl.getValue());
		
		group.getSelection().selection.setValue(group.getRadio(gradleId));
		assertEquals(BuildType.GRADLE, model.getBuildType());
		assertEquals(gradleZipUrl, model.baseUrl.getValue());
		
		group.getSelection().selection.setValue(group.getRadio(mavenId));
		assertEquals(BuildType.MAVEN, model.getBuildType());
		assertEquals(mavenZipUrl, model.baseUrl.getValue());
	}
	
	public void testStarters() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
		String[] styles = model.style.getChoices();
		assertNotNull(styles);
		assertTrue(styles.length>10);
		
		String lastLabel = null; //check that style labels are sorted 
		for (String choice : model.style.getChoices()) {
			String label = model.style.getLabel(choice);
			if (lastLabel!=null) {
				assertTrue("Labels not sorted: '"+lastLabel+"' > '"+label+"'", lastLabel.compareTo(label)<0);
			}
			lastLabel = label;
			assertNotNull("No tooltip for: "+choice+" ["+label+"]", model.style.getTooltip(choice));
		}
		
	}
		
	public void testLabels() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
		Iterator<FieldModel<String>> stringInputs = model.stringInputs.iterator();
		while (stringInputs.hasNext()) {
			FieldModel<String> input = stringInputs.next();
			assertRealLabel(input.getLabel());
		}
		
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
		NewSpringBootWizardModel model = parseFrom("initializr.json");
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
	 * Basic test of the 'spec parser'. Just check that it accepts a well-formed
	 * json spec document and doesn't crash on it.
	 */
	public void testInitializrSpecParser() throws Exception {
		doParseTest("initializr.json");
		doParseTest("initializr-variant.json"); //same file with some extra 'crap' added which parser should tolerate.
	}

	private void doParseTest(String resource) throws IOException, Exception {
		URL url = NewSpringBootWizardModelTest.class.getResource(resource);
		URLConnection conn = new URLConnectionFactory().createConnection(url);
		conn.connect();
		InputStream input = conn.getInputStream();
		try {
			InitializrServiceSpec spec = InitializrServiceSpec.parseFrom(input);
			assertNotNull(spec);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Test that radio params are wired up in the model so that selecting them changes the downloadUrl.
	 */
	public void testRadioQueryParams() throws Exception {
		NewSpringBootWizardModel model = parseFrom("initializr.json");
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
		assertNotNull("Label is null", label); //have a label
		assertFalse("Label is empty", "".equals(label.trim())); //label not empty
		if (Character.isDigit(label.charAt(0))) {
			//labels like '1.6.' are okay too.
			return;
		}
		assertTrue("Label doesn't start with uppercase: '"+label+"'", Character.isUpperCase(label.charAt(0))); //'real' label, not just the default taken from the name.
	}

	private void assertGroupValues(RadioGroup group, String... expecteds) {
		Set<String> expectedSet = new HashSet<String>(Arrays.asList(expecteds));
		RadioInfo[] radios = group.getRadios();
		for (int i = 0; i < radios.length; i++) {
			String actual = radios[i].getValue();
			if (!expectedSet.contains(actual)) {
				fail("Unexpected: "+actual);
			}
			expectedSet.remove(actual);
		}
		if (!expectedSet.isEmpty()) {
			StringBuilder notFound = new StringBuilder();
			for (String missing : expectedSet) {
				notFound.append(" "+missing);
			}
			fail("Missing: "+notFound);
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
