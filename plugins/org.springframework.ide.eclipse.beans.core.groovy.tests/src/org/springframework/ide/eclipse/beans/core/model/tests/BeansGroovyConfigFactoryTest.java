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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.groovy.tests.Activator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansGroovyConfigFactoryTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", Activator.PLUGIN_ID);
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testCreateBeansConfig() throws Exception {
		IBeansConfig config = BeansConfigFactory.create(beansProject, "basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		assertTrue(config instanceof BeansConfig);
		assertEquals("basic-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testCreateBeansConfigFullyQualifiedPath() throws Exception {
		IBeansConfig config = BeansConfigFactory.create(beansProject, "/beans-config-tests/basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		assertTrue(config instanceof BeansConfig);
		assertEquals("basic-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testCreateBeansJavaConfig() throws Exception {
		IBeansConfig config = BeansConfigFactory.create(beansProject, "java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		assertTrue(config instanceof BeansJavaConfig);
		assertEquals("java:org.test.spring.SimpleConfigurationClass", config.getElementName());
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		assertEquals(type, ((BeansJavaConfig)config).getConfigClass());
	}
	
	@Test
	public void testCreateBeansJavaConfigTypeError() throws Exception {
		IBeansConfig config = BeansConfigFactory.create(beansProject, "java:org.test.spring.SimpleConfigurationClassError", IBeansConfig.Type.MANUAL);
		assertTrue(config instanceof BeansJavaConfig);
		assertNull(((BeansJavaConfig)config).getConfigClass());
		assertEquals("org.test.spring.SimpleConfigurationClassError", ((BeansJavaConfig)config).getConfigClassName());
	}
	
	@Test
	public void testConfigNameXMLRelative() throws Exception {
		IFile file = project.getFile("/basic-bean-config.xml");
		assertEquals("basic-bean-config.xml", BeansConfigFactory.getConfigName(file, project));
	}
	
	@Test
	public void testConfigNameFullPath() throws Exception {
		IProject extraProject = StsTestUtil.createPredefinedProject("jdt-annotation-tests", Activator.PLUGIN_ID);

		IFile file = extraProject.getFile("/test.xml");
		assertEquals("/jdt-annotation-tests/test.xml", BeansConfigFactory.getConfigName(file, project));
		
		extraProject.delete(true, null);
	}
	
	@Test
	public void testConfigNameJavaFile() throws Exception {
		IFile file = project.getFile("/src/org/test/spring/SimpleConfigurationClass.java");
		assertEquals("java:org.test.spring.SimpleConfigurationClass", BeansConfigFactory.getConfigName(file, project));
	}
	
}
