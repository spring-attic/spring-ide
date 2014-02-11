/*******************************************************************************
 * Copyright (c) 2012, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.core.classreading.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.classreading.JdtConnectedMetadata;
import org.springframework.ide.eclipse.core.java.classreading.JdtMetadataReaderFactory;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtClassMetadataTest {
	
	private IProject project;
	private IJavaProject javaProject;
	private ClassLoader classloader;
	private JdtMetadataReaderFactory factory;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("jdt-annotation-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		classloader = JdtUtils.getClassLoader(project, ApplicationContext.class.getClassLoader());
		factory = new JdtMetadataReaderFactory(javaProject, classloader);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testSimpleClass() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.NoAnnotations");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.NoAnnotations", metadata.getClassName());
		assertEquals(0, metadata.getInterfaceNames().length);

		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("java.lang.Object", metadata.getSuperClassName());
		
		assertTrue(metadata instanceof JdtConnectedMetadata);
		IType type = JdtUtils.getJavaType(project, "org.test.spring.NoAnnotations");
		assertEquals(type, ((JdtConnectedMetadata)metadata).getJavaElement());
	}

	@Test
	public void testSubClass() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SubClassWithoutAnnotation");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.SubClassWithoutAnnotation", metadata.getClassName());
		assertEquals(0, metadata.getInterfaceNames().length);
		
		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("org.test.spring.SimpleConfigurationClass", metadata.getSuperClassName());
	}

	@Test
	public void testSubClassOfBinaryType() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SubClassOfBinaryType");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.SubClassOfBinaryType", metadata.getClassName());
		assertEquals(0, metadata.getInterfaceNames().length);
		
		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("org.springframework.core.SpringVersion", metadata.getSuperClassName());
	}

	@Test
	public void testSubtypeOfGenericSupertype() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.GenericSubtype");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.GenericSubtype", metadata.getClassName());
		assertEquals(0, metadata.getInterfaceNames().length);
		
		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("org.test.spring.GenericSupertype", metadata.getSuperClassName());
	}

	@Test
	public void testPlainInterface() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.PlainInterface");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.PlainInterface", metadata.getClassName());
		assertEquals(0, metadata.getInterfaceNames().length);
		
		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("java.lang.Object", metadata.getSuperClassName());
	}

	@Test
	public void testSubInterface() throws Exception {
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SubInterface");
		
		ClassMetadata metadata = metadataReader.getClassMetadata();
		assertEquals("org.test.spring.SubInterface", metadata.getClassName());
		assertEquals(1, metadata.getInterfaceNames().length);
		assertEquals("org.test.spring.PlainInterface", metadata.getInterfaceNames()[0]);
		
		assertFalse(metadata.hasEnclosingClass());
		assertNull(metadata.getEnclosingClassName());

		assertTrue(metadata.hasSuperClass());
		assertEquals("java.lang.Object", metadata.getSuperClassName());
	}

}
