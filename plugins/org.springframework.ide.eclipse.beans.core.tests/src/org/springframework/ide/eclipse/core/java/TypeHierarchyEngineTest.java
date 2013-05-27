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
package org.springframework.ide.eclipse.core.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.core.java.typehierarchy.BytecodeTypeHierarchyClassReaderFactory;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyEngine;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class TypeHierarchyEngineTest {
	
	private IProject project;
	private IJavaProject javaProject;
	private TypeHierarchyEngine engine;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("type-hierarchy-engine-testcases", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		engine = new TypeHierarchyEngine();
//		engine.setClassReaderFactory(new ClassloaderTypeHierarchyClassReaderFactory());
		engine.setClassReaderFactory(new BytecodeTypeHierarchyClassReaderFactory());
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testExtendsItselfObject() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertTrue(engine.doesExtend(type, type.getFullyQualifiedName()));
	}

	@Test
	public void testExtendsItselfSimpleClass() throws Exception {
		IType type = javaProject.findType("org.SimpleClass");
		assertTrue(engine.doesExtend(type, type.getFullyQualifiedName()));
	}

	@Test
	public void testTypeHierarchyClassExtendsObject() throws Exception {
		IType type = javaProject.findType("org.SimpleClass");
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
	}

	@Test
	public void testTypeHierarchyInterfaceExtendsObject() throws Exception {
		IType type = javaProject.findType("org.SimpleInterface");
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
	}

	@Test
	public void testTypeHierarchyExtendsSimple() throws Exception {
		IType type = javaProject.findType("org.Subclass");
		assertTrue(engine.doesExtend(type, "org.SimpleClass"));
	}

	@Test
	public void testTypeHierarchyObjectNotExtendSimple() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertFalse(engine.doesExtend(type, "org.SimpleClass"));
	}
	
	@Test
	public void testTypeHierarchySuperclassFromLibrary() throws Exception {
		IType type = javaProject.findType("org.ImplementingInterfaceThroughExtendingTypeFromLibrary");
		assertFalse(engine.doesExtend(type, "org.SimpleClass"));
		assertTrue(engine.doesExtend(type, "org.springframework.beans.factory.config.AbstractFactoryBean"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
		assertTrue(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean"));
	}
	
	@Test
	public void testObjectImplementsNothing() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertFalse(engine.doesImplement(type, "java.io.Serializable"));
		assertFalse(engine.doesImplement(type, "org.SimpleInterface"));
	}
	
	@Test
	public void testCombinedSubclassImplementsAndExtends() throws Exception {
		IType type = javaProject.findType("org.CombinedSubclass");
		assertTrue(engine.doesImplement(type, "org.SimpleInterface"));
		assertTrue(engine.doesExtend(type, "org.SimpleClass"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean"));
	}
	
	@Test
	public void testClassImplementsInterfaceThroughSubInterface() throws Exception {
		IType type = javaProject.findType("org.ClassImplementingInterfaceThroughSubInterface");
		assertTrue(engine.doesImplement(type, "org.SubInterface"));
		assertTrue(engine.doesImplement(type, "org.SimpleInterface"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean"));
	}
	
	@Test
	public void testComplexInterfaceStructure() throws Exception {
		IType type = javaProject.findType("org.sub.ClassABCD");
		
		assertTrue(engine.doesExtend(type, "org.sub.ClassB"));
		assertTrue(engine.doesExtend(type, "org.ClassA"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));

		assertFalse(engine.doesExtend(type, "org.SimpleClass"));
		assertFalse(engine.doesExtend(type, "org.Subclass"));

		assertTrue(engine.doesImplement(type, "org.InterfaceA"));
		assertTrue(engine.doesImplement(type, "org.InterfaceB"));
		assertTrue(engine.doesImplement(type, "org.InterfaceC"));
		assertTrue(engine.doesImplement(type, "org.InterfaceD"));
		assertTrue(engine.doesImplement(type, "org.sub.InterfaceAB"));
		assertTrue(engine.doesImplement(type, "org.sub.InterfaceCD"));
		
		assertFalse(engine.doesImplement(type, "org.SimpleInterface"));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean"));
	}
	
	@Test
	public void testInnerClassImplementingsInterface() throws Exception {
		IType type = javaProject.findType("org.OuterClassA$InnerClassA");
		assertTrue(engine.doesImplement(type, "org.SimpleInterface"));
		assertFalse(engine.doesExtend(type, "org.SimpleClass"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
	}
	
	@Test
	public void testClassExtendsInnerClass() throws Exception {
		IType type = javaProject.findType("org.SubclassingInnerClassB");
		assertFalse(engine.doesImplement(type, "org.OuterClassB$InnerInterfaceB"));
		assertTrue(engine.doesExtend(type, "org.OuterClassB$InnerClassB"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
	}
	
	@Test
	public void testClassImplementsInnerInterface() throws Exception {
		IType type = javaProject.findType("org.ImplementingInnerInterfaceB");
		assertTrue(engine.doesImplement(type, "org.OuterClassB$InnerInterfaceB"));
		assertFalse(engine.doesExtend(type, "org.OuterClassB$InnerClassB"));
		assertTrue(engine.doesExtend(type, "java.lang.Object"));
	}
	
	@Test
	public void testGetSupertypeOfClass() throws Exception {
		IType type = javaProject.findType("org.ImplementingInterfaceThroughExtendingTypeFromLibrary");
		assertEquals("org.springframework.beans.factory.config.AbstractFactoryBean", engine.getSupertype(type));
		assertEquals("java.lang.Object", engine.getSupertype(project, "org.springframework.beans.factory.config.AbstractFactoryBean"));
	}
	
	@Test
	public void testGetSupertypeOfInterface() throws Exception {
		IType type = javaProject.findType("org.SimpleInterface");
		assertEquals("java.lang.Object", engine.getSupertype(type));
	}
	
	@Test
	public void testAdditionalCase() throws Exception {
		IType type = javaProject.findType("org.AdditionalCase");
		assertEquals("java.lang.Object", engine.getSupertype(type));
	}
	
}
