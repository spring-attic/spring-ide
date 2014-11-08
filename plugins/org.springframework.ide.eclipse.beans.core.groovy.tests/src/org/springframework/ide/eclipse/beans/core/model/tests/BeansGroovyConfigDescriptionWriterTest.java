/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.tests;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.groovy.tests.Activator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansGroovyConfigDescriptionWriterTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;

	@BeforeClass
	public static void setUpAll() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Set non-locking class-loader for windows testing
			 */
			InstanceScope.INSTANCE.getNode(SpringCore.PLUGIN_ID).putBoolean(
					SpringCore.USE_NON_LOCKING_CLASSLOADER, true);
		}
	}

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", Activator.PLUGIN_ID);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		new ACondition("Wait for Jobs") {
			@Override
			public boolean test() throws Exception {
				assertJobManagerIdle();
				return true;
			}
		}.waitFor(3 * 60 * 1000);
		project.delete(true, null);
	}

	@Test
	public void testBeansProjectDescriptionWriterWithXMLConfigsOnly() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLWriter writer = new XMLWriter(os);
		BeansProjectDescriptionWriter.write(beansProject, writer);
		writer.flush();
		writer.close();
		
		String description = os.toString();
		
		Matcher matcher = Pattern
				.compile(
						"<configs>\\W*<config>\\W*basic-bean-config.xml\\W*</config>\\W*</configs>")
				.matcher(description);
		assertTrue(matcher.find());
	}
	
	@Test
	public void testBeansProjectDescriptionWriterWithMixedConfigs() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLWriter writer = new XMLWriter(os);
		BeansProjectDescriptionWriter.write(beansProject, writer);
		writer.flush();
		writer.close();
		
		String description = os.toString();
		
		Matcher matcher = Pattern
				.compile(
						"<configs>\\W*<config>\\W*basic-bean-config.xml\\W*</config>\\W*<config>\\W*java:org.test.spring.SimpleConfigurationClass\\W*</config>\\W*</configs>")
				.matcher(description);
		assertTrue(matcher.find());
	}
	
}
