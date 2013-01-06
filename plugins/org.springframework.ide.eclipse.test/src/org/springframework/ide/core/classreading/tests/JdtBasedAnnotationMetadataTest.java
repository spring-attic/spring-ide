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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.eclipse.beans.core.metadata.internal.model.DelegatingAnnotationReadingMetadataProvider;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.java.annotation.JdtBasedAnnotationMetadata;
import org.springframework.ide.eclipse.core.java.classreading.IJdtMethodMetadata;
import org.springframework.ide.eclipse.core.java.classreading.JdtMetadataReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtBasedAnnotationMetadataTest {
	
	private IProject project;
	private CachingClassReaderFactory classReaderFactory;
	private ClassLoader classLoader;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("jdt-annotation-tests", "org.springframework.ide.eclipse.test");
		classReaderFactory = new CachingClassReaderFactory(JdtUtils.getClassLoader(project, null));
		classLoader = JdtUtils.getClassLoader(project, null);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}
	
	protected IAnnotationMetadata getAnnotationMetadata(IType type) {
		AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();
		DelegatingAnnotationReadingMetadataProvider provider = new DelegatingAnnotationReadingMetadataProvider();
		provider.runAnnotationMetadataVisitor(type, classReaderFactory, classLoader, project, visitor);
		return visitor;
		
//		return new JdtBasedAnnotationMetadata(type);
	}

	@Test
	public void testNoAnnotations() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.NoAnnotations");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);
		
		assertNull(metadata.getTypeLevelAnnotation(Configuration.class.getName()));
		assertEquals(0, metadata.getTypeLevelAnnotationClasses().size());
		
		assertEquals(0, metadata.getMethodLevelAnnotations(Bean.class.getName()).size());
		assertEquals(0, metadata.getFieldLevelAnnotations(Autowired.class.getName()).size());
	}

	@Test
	public void testSimpleConfigurationClass() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SimpleConfigurationClass");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);

		Annotation annotation = metadata.getTypeLevelAnnotation(Configuration.class.getName());
		assertEquals(Configuration.class.getName(), annotation.getAnnotationClass());
		
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		assertEquals(0, members.size());
	}

	@Test
	public void testSimpleBeanClass() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SimpleBeanClass");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);
		
		assertEquals(0, metadata.getTypeLevelAnnotationClasses().size());
		
		Map<IMethod, Annotation> annotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(1, annotations.size());
		
		IMethod method = type.getMethods()[0];
		Annotation annotation = annotations.get(method);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());
		
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		assertEquals(0, members.size());
	}

	@Test
	public void testSimpleBeanClassWithAutowireAttribute() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SimpleBeanClassWithAttribute");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);
		
		assertEquals(0, metadata.getTypeLevelAnnotationClasses().size());
		
		Map<IMethod, Annotation> annotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(1, annotations.size());
		
		IMethod method = type.getMethods()[0];
		Annotation annotation = annotations.get(method);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());
		
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		assertEquals(1, members.size());
		AnnotationMemberValuePair pair = members.iterator().next();
		assertEquals("autowire", pair.getName());
		assertEquals("Autowire.BY_NAME", pair.getValue());
	}

	@Test
	public void testSimpleFieldAnnotation() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SimpleFieldAnnotation");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);
		
		Map<IField, Annotation> annotations = metadata.getFieldLevelAnnotations(Autowired.class.getName());
		assertEquals(1, annotations.size());
		
		IField field = type.getField("injectedDependency");
		Annotation annotation = annotations.get(field);
		assertEquals(Autowired.class.getName(), annotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		assertEquals(0, members.size());
	}

	@Test
	public void testSubClassWithoutAnnotation() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.SubClassWithoutAnnotation");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);

		Annotation annotation = metadata.getTypeLevelAnnotation(Configuration.class.getName());
		assertEquals(Configuration.class.getName(), annotation.getAnnotationClass());
		
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		assertEquals(0, members.size());
	}

	@Test
	public void testOverriddenMethodWithoutAnnotation() throws Exception {
		IType subtype = JdtUtils.getJavaType(project, "org.test.spring.OverriddenMethodWithoutAnnotation");
		
		IAnnotationMetadata metadata = getAnnotationMetadata(subtype);

		assertEquals(0, metadata.getTypeLevelAnnotationClasses().size());
		Map<IMethod, Annotation> annotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(0, annotations.size());
	}

	@Test
	public void testSuperclassWithMethodAnnotation() throws Exception {
		IType superType = JdtUtils.getJavaType(project, "org.test.spring.SimpleBeanClass");
		IType subtype = JdtUtils.getJavaType(project, "org.test.spring.SimpleBeanClassSubclass");
		IAnnotationMetadata metadata = getAnnotationMetadata(subtype);
		
		assertNull(metadata.getTypeLevelAnnotation(Bean.class.getName()));
		
		Map<IMethod, Annotation> methodAnnotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(1, methodAnnotations.size());
		
		IMethod method = superType.getMethods()[0];
		Annotation annotation = methodAnnotations.get(method);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());
		assertEquals(0, annotation.getMembers().size());
	}
	
	@Test
	public void testAdvancedControllerCases() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.ControllerAdvancedRequestMapping");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);
		
		Annotation controllerAnnotation = metadata.getTypeLevelAnnotation(Controller.class.getName());
		assertEquals(Controller.class.getName(), controllerAnnotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> controllerMembers = controllerAnnotation.getMembers();
		assertEquals(0, controllerMembers.size());

		Annotation requestAnnotation = metadata.getTypeLevelAnnotation(RequestMapping.class.getName());
		assertEquals(RequestMapping.class.getName(), requestAnnotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> requestMembers = requestAnnotation.getMembers();
		assertEquals(1, requestMembers.size());
		
		AnnotationMemberValuePair pair = requestMembers.iterator().next();
		assertNull(pair.getName());
		assertEquals("/index1.htm, /index2.htm", pair.getValue());
		
		Map<IMethod, Annotation> requestMappingMethodAnnotations = metadata.getMethodLevelAnnotations(RequestMapping.class.getName());
		assertEquals(1, requestMappingMethodAnnotations.size());
		
		IMethod method = type.getMethods()[0];
		Annotation requestMethodAnnotation = requestMappingMethodAnnotations.get(method);
		assertEquals(RequestMapping.class.getName(), requestMethodAnnotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> requestMethodMembers = requestMethodAnnotation.getMembers();
		assertEquals(1, requestMethodMembers.size());
		
		pair = requestMethodMembers.iterator().next();
		assertEquals("method", pair.getName());
		assertEquals("RequestMethod.GET, RequestMethod.POST", pair.getValue());
	}
	
	@Test
	public void testCombinedCase() throws Exception {
		IType superType = JdtUtils.getJavaType(project, "org.test.spring.CombinedCaseSuperclass");
		IType subType = JdtUtils.getJavaType(project, "org.test.spring.CombinedCaseSubclass");
		IAnnotationMetadata metadata = getAnnotationMetadata(subType);
		
		Annotation controllerAnnotation = metadata.getTypeLevelAnnotation(Controller.class.getName());
		assertEquals(Controller.class.getName(), controllerAnnotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> controllerMembers = controllerAnnotation.getMembers();
		assertEquals(0, controllerMembers.size());

		Annotation requestAnnotation = metadata.getTypeLevelAnnotation(RequestMapping.class.getName());
		assertEquals(RequestMapping.class.getName(), requestAnnotation.getAnnotationClass());
		Set<AnnotationMemberValuePair> requestMembers = requestAnnotation.getMembers();
		assertEquals(1, requestMembers.size());
		
		AnnotationMemberValuePair pair = requestMembers.iterator().next();
		assertNull(pair.getName());
		assertEquals("/index3.htm, /index4.htm, /index5.htm", pair.getValue());
		
		Map<IMethod, Annotation> beanMethodAnnotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(4, beanMethodAnnotations.size());
		
		IMethod getAnotherBean = subType.getMethod("getAnotherBean", new String[0]);
		IMethod getBeanInstance = subType.getMethod("getBeanInstance", new String[0]);
		IMethod getBeanInstanceWithString = subType.getMethod("getBeanInstance", new String[] {"QString;"});
		IMethod getSuperInstance = superType.getMethod("getSuperInstance", new String[0]);
		
		Annotation annotation = beanMethodAnnotations.get(getAnotherBean);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());
		
		annotation = beanMethodAnnotations.get(getBeanInstance);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());

		annotation = beanMethodAnnotations.get(getBeanInstanceWithString);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());

		annotation = beanMethodAnnotations.get(getSuperInstance);
		assertEquals(Bean.class.getName(), annotation.getAnnotationClass());
	}
	
	@Test
	public void testMethodIdentification() throws Exception {
		IType type = JdtUtils.getJavaType(project, "org.test.spring.MethodIdentificationSubtype");
		IAnnotationMetadata metadata = getAnnotationMetadata(type);

		Map<IMethod, Annotation> beanMethodAnnotations = metadata.getMethodLevelAnnotations(Bean.class.getName());
		assertEquals(8, beanMethodAnnotations.size());

		IMethod getInstanceNoArg = type.getMethod("getInstance", new String[0]);
		IMethod getInstanceObject = type.getMethod("getInstance", new String[] {"QObject;"});
		IMethod getInstanceString = type.getMethod("getInstance", new String[] {"QString;"});
		IMethod getInstanceObjectObject = type.getMethod("getInstance", new String[] {"QObject;", "QObject;"});
		IMethod getInstanceObjectString = type.getMethod("getInstance", new String[] {"QObject;", "QString;"});
		IMethod getInstanceStringObject = type.getMethod("getInstance", new String[] {"QString;", "QObject;"});
		IMethod getInstanceStringString = type.getMethod("getInstance", new String[] {"QString;", "QString;"});
		IMethod getInstanceStringStringString = type.getMethod("getInstance", new String[] {"QString;", "QString;", "QString;"});

		assertTrue(beanMethodAnnotations.containsKey(getInstanceNoArg));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceObject));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceString));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceObjectObject));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceObjectString));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceStringObject));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceStringString));
		assertTrue(beanMethodAnnotations.containsKey(getInstanceStringStringString));
	}

}
