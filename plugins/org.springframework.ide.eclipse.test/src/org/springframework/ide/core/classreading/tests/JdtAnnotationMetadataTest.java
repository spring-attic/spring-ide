/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Role;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.classreading.IJdtMethodMetadata;
import org.springframework.ide.eclipse.core.java.classreading.JdtMetadataReaderFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtAnnotationMetadataTest {
	
	private IProject project;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("jdt-annotation-tests", "org.springframework.ide.eclipse.test");
		javaProject = JdtUtils.getJavaProject(project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testNoAnnotations() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.NoAnnotations");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		
		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotatedMethods(Bean.class.getName()));
		assertNull(annotationMetadata.getAnnotationAttributes(Configuration.class.getName()));

		assertEquals(0, annotationMetadata.getAnnotatedMethods(Bean.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
	}

	@Test
	public void testSimpleConfigurationClass() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SimpleConfigurationClass");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

		assertTrue(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertTrue(annotationMetadata.hasAnnotation(Configuration.class.getName()));
		Set<String> types = annotationMetadata.getAnnotationTypes();
		assertEquals(1, types.size());
		assertEquals(Configuration.class.getName(), types.iterator().next());
		
		assertFalse(annotationMetadata.isAnnotated(Bean.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Bean.class.getName()));
		assertFalse(annotationMetadata.hasAnnotatedMethods(Bean.class.getName()));

		assertEquals(0, annotationMetadata.getAnnotatedMethods(Bean.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
		
		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(Configuration.class.getName());
		assertEquals(1, attributes.size());
		assertEquals("", attributes.get("value"));
	}

	@Test
	public void testSimpleBeanClass() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SimpleBeanClass");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		
		assertFalse(annotationMetadata.isAnnotated(Bean.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Bean.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		
		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotatedMethods(Configuration.class.getName()));

		assertTrue(annotationMetadata.hasAnnotatedMethods(Bean.class.getName()));

		Set<MethodMetadata> methods = annotationMetadata.getAnnotatedMethods(Bean.class.getName());
		assertEquals(1, methods.size());
		MethodMetadata methodMetadata = methods.iterator().next();
		assertTrue(methodMetadata.isAnnotated(Bean.class.getName()));
		
		Map<String, Object> annotationAttributes = methodMetadata.getAnnotationAttributes(Bean.class.getName());
		assertEquals(4, annotationAttributes.size());
		
		assertEquals("", annotationAttributes.get("initMethod"));
		assertEquals("(inferred)", annotationAttributes.get("destroyMethod"));
		
		Object nameArray = annotationAttributes.get("name");
		assertNotNull(nameArray);
		assertTrue(nameArray instanceof Object[]);
		assertEquals(0, ((Object[]) nameArray).length);
		
		assertEquals(Autowire.class.getName(), annotationAttributes.get("autowire").getClass().getName());
		assertEquals(Autowire.NO.toString(), annotationAttributes.get("autowire").toString());
		
		assertTrue(methodMetadata instanceof IJdtMethodMetadata);
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SimpleBeanClass");
		IMethod method = type.getMethods()[0];
		assertEquals(method, ((IJdtMethodMetadata) methodMetadata).getMethod());
		
		assertNull(methodMetadata.getAnnotationAttributes(Role.class.getName()));
		
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
	}

	@Test
	public void testSimpleFieldAnnotation() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SimpleFieldAnnotation");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		
		assertFalse(annotationMetadata.isAnnotated(Autowired.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Autowired.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		assertEquals(0, annotationMetadata.getAnnotatedMethods(Autowired.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
	}

	@Test
	public void testImportResourceSpecialCase() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.ImportResourceClass");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		assertFalse(annotationMetadata.isAnnotated(ImportResource.class.getName()));
		assertTrue(annotationMetadata.hasAnnotation(ImportResource.class.getName()));
		assertTrue(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertTrue(annotationMetadata.hasAnnotation(Configuration.class.getName()));
		assertTrue(annotationMetadata.isAnnotated(Import.class.getName()));
		assertTrue(annotationMetadata.hasAnnotation(Import.class.getName()));
		Set<String> types = annotationMetadata.getAnnotationTypes();
		assertEquals(3, types.size());
		assertTrue(types.contains(Configuration.class.getName()));
		assertTrue(types.contains(ImportResource.class.getName()));
		assertTrue(types.contains(Import.class.getName()));
		
		Map<String, Object> configAttributes = annotationMetadata.getAnnotationAttributes(Configuration.class.getName());
		assertEquals(1, configAttributes.size());
		assertEquals("", configAttributes.get("value"));

		Map<String, Object> importResourceAttributesAsStrings = annotationMetadata.getAnnotationAttributes(ImportResource.class.getName(), true);
		assertEquals(2, importResourceAttributesAsStrings.size());
		assertEquals("org.springframework.beans.factory.xml.XmlBeanDefinitionReader", importResourceAttributesAsStrings.get("reader"));
		assertEquals("classpath:/com/acme/database-config.xml", ((String[])importResourceAttributesAsStrings.get("value"))[0]);
		
		Map<String, Object> importResourceAttributesAsObjects = annotationMetadata.getAnnotationAttributes(ImportResource.class.getName(), false);
		assertEquals(2, importResourceAttributesAsObjects.size());
		assertTrue(importResourceAttributesAsObjects.get("reader") instanceof Class);
		assertEquals("org.springframework.beans.factory.xml.XmlBeanDefinitionReader", ((Class<?>)importResourceAttributesAsObjects.get("reader")).getName());
		assertEquals("classpath:/com/acme/database-config.xml", ((String[])importResourceAttributesAsObjects.get("value"))[0]);

		Map<String, Object> importAttributesAsStrings = annotationMetadata.getAnnotationAttributes(Import.class.getName(), true);
		assertEquals(1, importAttributesAsStrings.size());
		assertEquals("org.test.spring.SimpleConfigurationClass", ((String[])importAttributesAsStrings.get("value"))[0]);
		
		Map<String, Object> importAttributesAsObjects = annotationMetadata.getAnnotationAttributes(Import.class.getName(), false);
		assertEquals(1, importAttributesAsObjects.size());
		assertEquals("org.test.spring.SimpleConfigurationClass", ((Class[])importAttributesAsObjects.get("value"))[0].getName());
	}

	@Test
	public void testSubClassWithoutAnnotation() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SubClassWithoutAnnotation");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Configuration.class.getName()));

		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		assertEquals(0, annotationMetadata.getAnnotatedMethods(Configuration.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
		assertNull(annotationMetadata.getAnnotationAttributes(Configuration.class.getName()));
	}
	
	@Test
	public void testOverriddenMethodWithoutAnnotation() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.OverriddenMethodWithoutAnnotation");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		
		assertFalse(annotationMetadata.isAnnotated(Bean.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Bean.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		
		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotatedMethods(Configuration.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotatedMethods(Bean.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
	}

	@Test
	public void testSuperclassWithMethodAnnotation() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.SimpleBeanClassSubclass");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		
		assertFalse(annotationMetadata.isAnnotated(Bean.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Bean.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotationTypes().size());
		
		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotation(Configuration.class.getName()));
		assertFalse(annotationMetadata.hasAnnotatedMethods(Configuration.class.getName()));

		assertFalse(annotationMetadata.hasAnnotatedMethods(Bean.class.getName()));
		assertEquals(0, annotationMetadata.getAnnotatedMethods(Bean.class.getName()).size());
		assertEquals(0, annotationMetadata.getMemberClassNames().length);
	}
	
	@Test
	public void testAdvancedControllerCases() throws Exception {
		JdtMetadataReaderFactory factory = new JdtMetadataReaderFactory(javaProject);
		MetadataReader metadataReader = factory.getMetadataReader("org.test.spring.ControllerAdvancedRequestMapping");
		
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		
		assertFalse(annotationMetadata.isAnnotated(Bean.class.getName()));
		assertFalse(annotationMetadata.isAnnotated(Configuration.class.getName()));

		assertTrue(annotationMetadata.isAnnotated(Controller.class.getName()));
		assertTrue(annotationMetadata.isAnnotated(RequestMapping.class.getName()));
		
		assertEquals(2, annotationMetadata.getAnnotationTypes().size());
		Map<String, Object> controllerAttributes = annotationMetadata.getAnnotationAttributes(Controller.class.getName());
		assertEquals(1, controllerAttributes.size());
		assertEquals("", controllerAttributes.get("value"));
		
		Map<String, Object> mappingAttributes = annotationMetadata.getAnnotationAttributes(RequestMapping.class.getName());
		assertEquals(6, mappingAttributes.size());
		assertEquals(0, ((String[])mappingAttributes.get("headers")).length);
		assertEquals(2, ((String[])mappingAttributes.get("value")).length);
		assertEquals("/index1.htm", ((String[])mappingAttributes.get("value"))[0]);
		assertEquals("/index2.htm", ((String[])mappingAttributes.get("value"))[1]);
		assertEquals(0, ((String[])mappingAttributes.get("produces")).length);
		assertEquals(0, ((Object[])mappingAttributes.get("method")).length);
		assertEquals(0, ((String[])mappingAttributes.get("params")).length);
		assertEquals(0, ((String[])mappingAttributes.get("consumes")).length);
		
		assertTrue(annotationMetadata.hasAnnotatedMethods(RequestMapping.class.getName()));
		Set<MethodMetadata> methods = annotationMetadata.getAnnotatedMethods(RequestMapping.class.getName());
		assertEquals(1, methods.size());
		
		MethodMetadata methodMetadata = methods.iterator().next();
		assertFalse(methodMetadata.isAnnotated(Controller.class.getName()));
		assertTrue(methodMetadata.isAnnotated(RequestMapping.class.getName()));
		
		Map<String, Object> methodAttributes = methodMetadata.getAnnotationAttributes(RequestMapping.class.getName());
		assertEquals(6, methodAttributes.size());
		assertEquals(0, ((String[])methodAttributes.get("headers")).length);
		assertEquals(0, ((String[])methodAttributes.get("value")).length);
		assertEquals(0, ((String[])methodAttributes.get("produces")).length);
		assertEquals(2, ((Object[])methodAttributes.get("method")).length);
		assertEquals("RequestMethod.GET", ((Object[])methodAttributes.get("method"))[0]);
		assertEquals("RequestMethod.POST", ((Object[])methodAttributes.get("method"))[1]);
		assertEquals(0, ((String[])methodAttributes.get("params")).length);
		assertEquals(0, ((String[])methodAttributes.get("consumes")).length);
		
	}

}
