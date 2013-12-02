/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
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

import org.eclipse.core.resources.IProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansProjectDescriptionWriterTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
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
		
		String configs = "\t<configs>\n\t\t<config>basic-bean-config.xml</config>\n\t</configs>";
		assertTrue(description.contains(configs));
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
		
		String configs = "\t<configs>\n\t\t<config>basic-bean-config.xml</config>\n\t\t<config>java:org.test.spring.SimpleConfigurationClass</config>\n\t</configs>";
		assertTrue(description.contains(configs));
	}
	
	@Test
	public void testBeansProjectDescriptionWriterWithMixedAutoConfigs() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.AUTO_DETECTED);
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLWriter writer = new XMLWriter(os);
		BeansProjectDescriptionWriter.write(beansProject, writer);
		writer.flush();
		writer.close();
		
		String description = os.toString();
		
		String configs = "\t<configs>\n\t\t<config>basic-bean-config.xml</config>\n\t\t<config>java:org.test.spring.SimpleConfigurationClass</config>\n\t</configs>";
		assertTrue(description.contains(configs));
	}
	
}
