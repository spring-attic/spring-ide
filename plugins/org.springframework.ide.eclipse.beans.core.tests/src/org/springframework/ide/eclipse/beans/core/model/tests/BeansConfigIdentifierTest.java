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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigIdentifier;
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
public class BeansConfigIdentifierTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testBeansConfigSerialize() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		assertEquals("basic-bean-config.xml", BeansConfigIdentifier.serialize(config));
	}
	
	@Test
	public void testBeansConfigDeserialize() throws Exception {
		IBeansConfig config = BeansConfigIdentifier.deserialize("basic-bean-config.xml", beansProject);
		assertTrue(config instanceof BeansConfig);
		assertEquals("basic-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testBeansConfigDeserializeFullyQualifiedPath() throws Exception {
		IBeansConfig config = BeansConfigIdentifier.deserialize("/beans-config-tests/basic-bean-config.xml", beansProject);
		assertTrue(config instanceof BeansConfig);
		assertEquals("basic-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testBeansJavaConfigSerialize() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, IBeansConfig.Type.MANUAL);
		assertEquals("java:org.test.spring.SimpleConfigurationClass", BeansConfigIdentifier.serialize(config));
	}

	@Test
	public void testBeansJavaConfigDeserialize() throws Exception {
		IBeansConfig config = BeansConfigIdentifier.deserialize("java:org.test.spring.SimpleConfigurationClass", beansProject);
		assertTrue(config instanceof BeansJavaConfig);
		assertEquals("org.test.spring.SimpleConfigurationClass", config.getElementName());
	}
	
	@Test
	public void testBeansJavaConfigDeserializeTypeError() throws Exception {
		IBeansConfig config = BeansConfigIdentifier.deserialize("java:org.test.spring.SimpleConfigurationClassError", beansProject);
		assertNull(config);
	}
	
}
