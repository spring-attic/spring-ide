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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.core.java.typehierarchy.BytecodeTypeHierarchyClassReaderFactory;
import org.springframework.ide.eclipse.core.java.typehierarchy.DirectTypeHierarchyElementCacheFactory;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyClassReader;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyClassReaderFactory;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyElement;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyElementCache;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyElementCacheFactory;
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
	private BytecodeTypeHierarchyClassReaderFactory classReaderFactory;
	private DirectTypeHierarchyElementCacheFactory elementCacheFactory;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("type-hierarchy-engine-testcases", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		classReaderFactory = new BytecodeTypeHierarchyClassReaderFactory();
		elementCacheFactory = new DirectTypeHierarchyElementCacheFactory();

		engine = new TypeHierarchyEngine();
		engine.setClassReaderFactory(classReaderFactory);
		engine.setTypeHierarchyElementCacheFactory(elementCacheFactory);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}
	
	@Test
	public void testDefaultPackageClass() throws Exception {
		assertEquals("org.SimpleClass", engine.getSupertype(project, "DefaultPackageClass", true));
		assertTrue(engine.doesExtend("DefaultPackageClass", "org.SimpleClass", project, true));
	}

	@Test
	public void testExtendsItselfObject() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertTrue(engine.doesExtend(type, type.getFullyQualifiedName(), true));
	}

	@Test
	public void testExtendsItselfSimpleClass() throws Exception {
		IType type = javaProject.findType("org.SimpleClass");
		assertTrue(engine.doesExtend(type, type.getFullyQualifiedName(), true));
	}

	@Test
	public void testTypeHierarchyClassExtendsObject() throws Exception {
		IType type = javaProject.findType("org.SimpleClass");
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
	}

	@Test
	public void testTypeHierarchyInterfaceExtendsObject() throws Exception {
		IType type = javaProject.findType("org.SimpleInterface");
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
	}

	@Test
	public void testTypeHierarchyExtendsSimple() throws Exception {
		IType type = javaProject.findType("org.Subclass");
		assertTrue(engine.doesExtend(type, "org.SimpleClass", true));
	}

	@Test
	public void testTypeHierarchyObjectNotExtendSimple() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertFalse(engine.doesExtend(type, "org.SimpleClass", true));
	}
	
	@Test
	public void testTypeHierarchySuperclassFromLibrary() throws Exception {
		IType type = javaProject.findType("org.ImplementingInterfaceThroughExtendingTypeFromLibrary");
		assertFalse(engine.doesExtend(type, "org.SimpleClass", true));
		assertTrue(engine.doesExtend(type, "org.springframework.beans.factory.config.AbstractFactoryBean", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
		assertTrue(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean", true));
	}
	
	@Test
	public void testObjectImplementsNothing() throws Exception {
		IType type = javaProject.findType("java.lang.Object");
		assertFalse(engine.doesImplement(type, "java.io.Serializable", true));
		assertFalse(engine.doesImplement(type, "org.SimpleInterface", true));
	}
	
	@Test
	public void testCombinedSubclassImplementsAndExtends() throws Exception {
		IType type = javaProject.findType("org.CombinedSubclass");
		assertTrue(engine.doesImplement(type, "org.SimpleInterface", true));
		assertTrue(engine.doesExtend(type, "org.SimpleClass", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean", true));
	}
	
	@Test
	public void testClassImplementsInterfaceThroughSubInterface() throws Exception {
		IType type = javaProject.findType("org.ClassImplementingInterfaceThroughSubInterface");
		assertTrue(engine.doesImplement(type, "org.SubInterface", true));
		assertTrue(engine.doesImplement(type, "org.SimpleInterface", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean", true));
	}
	
	@Test
	public void testComplexInterfaceStructure() throws Exception {
		IType type = javaProject.findType("org.sub.ClassABCD");
		
		assertTrue(engine.doesExtend(type, "org.sub.ClassB", true));
		assertTrue(engine.doesExtend(type, "org.ClassA", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));

		assertFalse(engine.doesExtend(type, "org.SimpleClass", true));
		assertFalse(engine.doesExtend(type, "org.Subclass", true));

		assertTrue(engine.doesImplement(type, "org.InterfaceA", true));
		assertTrue(engine.doesImplement(type, "org.InterfaceB", true));
		assertTrue(engine.doesImplement(type, "org.InterfaceC", true));
		assertTrue(engine.doesImplement(type, "org.InterfaceD", true));
		assertTrue(engine.doesImplement(type, "org.sub.InterfaceAB", true));
		assertTrue(engine.doesImplement(type, "org.sub.InterfaceCD", true));
		
		assertFalse(engine.doesImplement(type, "org.SimpleInterface", true));
		assertFalse(engine.doesImplement(type, "org.springframework.beans.factory.FactoryBean", true));
	}
	
	@Test
	public void testInnerClassImplementingsInterface() throws Exception {
		IType type = javaProject.findType("org.OuterClassA$InnerClassA");
		assertTrue(engine.doesImplement(type, "org.SimpleInterface", true));
		assertFalse(engine.doesExtend(type, "org.SimpleClass", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
	}
	
	@Test
	public void testClassExtendsInnerClass() throws Exception {
		IType type = javaProject.findType("org.SubclassingInnerClassB");
		assertFalse(engine.doesImplement(type, "org.OuterClassB$InnerInterfaceB", true));
		assertTrue(engine.doesExtend(type, "org.OuterClassB$InnerClassB", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
	}
	
	@Test
	public void testClassImplementsInnerInterface() throws Exception {
		IType type = javaProject.findType("org.ImplementingInnerInterfaceB");
		assertTrue(engine.doesImplement(type, "org.OuterClassB$InnerInterfaceB", true));
		assertFalse(engine.doesExtend(type, "org.OuterClassB$InnerClassB", true));
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
	}
	
	@Test
	public void testGetSupertypeOfClass() throws Exception {
		IType type = javaProject.findType("org.ImplementingInterfaceThroughExtendingTypeFromLibrary");
		assertEquals("org.springframework.beans.factory.config.AbstractFactoryBean", engine.getSupertype(type, true));
		assertEquals("java.lang.Object", engine.getSupertype(project, "org.springframework.beans.factory.config.AbstractFactoryBean", true));
	}
	
	@Test
	public void testGetSupertypeOfInterface() throws Exception {
		IType type = javaProject.findType("org.SimpleInterface");
		assertEquals("java.lang.Object", engine.getSupertype(type, true));
	}
	
	@Test
	public void testGetInterfacesOfClass() throws Exception {
		String[] interfaces = engine.getInterfaces(project, "org.sub.ClassB", true);
		assertEquals(1, interfaces.length);
		assertEquals("org.sub.InterfaceAB", interfaces[0]);
	}
	
	@Test
	public void testGetInterfacesOfInterface() throws Exception {
		String[] interfaces = engine.getInterfaces(project, "org.sub.InterfaceAB", true);
		assertEquals(2, interfaces.length);
		assertEquals("org.InterfaceA", interfaces[0]);
		assertEquals("org.InterfaceB", interfaces[1]);
	}
	
	@Test
	public void testAdditionalCaseWithLongDoubleConstantsInClass() throws Exception {
		IType type = javaProject.findType("org.CaseWithLongAndDoubleConstants");
		assertEquals("java.lang.Object", engine.getSupertype(type, true));
	}
	
	@Test
	public void testUseCachedElementsFirst() throws Exception {
		AccessLoggingClassReaderFactory readerFactory = new AccessLoggingClassReaderFactory(classReaderFactory);
		engine.setClassReaderFactory(readerFactory);
		
		engine.getSupertype(project, "org.sub.ClassB", true);
		engine.getSupertype(project, "org.sub.InterfaceAB", true);
		
		IType type = javaProject.findType("org.sub.ClassABCD");
		assertTrue(engine.doesImplement(type, "org.InterfaceB", true));
		
		AccessLoggingClassReader reader = readerFactory.getReader(project);
		assertTrue(reader.classAccessed("org/sub/ClassABCD"));
		assertTrue(reader.classAccessed("org/sub/ClassB"));
		assertTrue(reader.classAccessed("org/sub/InterfaceAB"));
		
		assertFalse(reader.classAccessed("org/ClassA"));
		assertFalse(reader.classAccessed("org/sub/InterfaceCD"));
		assertFalse(reader.classAccessed("org/InterfaceC"));
		assertFalse(reader.classAccessed("org/InterfaceD"));
		assertFalse(reader.classAccessed("org/InterfaceA"));
		assertFalse(reader.classAccessed("org/InterfaceB"));
	}
	
	@Test
	public void testDontAccessCacheTwiceForClassHierarchy() throws Exception {
		AccessLoggingTypeHierarchyElementCacheFactory cacheFactory = new AccessLoggingTypeHierarchyElementCacheFactory();
		engine.setTypeHierarchyElementCacheFactory(cacheFactory);
		
		IType type = javaProject.findType("org.sub.ClassABCD");
		assertTrue(engine.doesExtend(type, "org.ClassA", true));
		
		AccessLoggingTypeHierarchyElementCache[] caches = cacheFactory.getCaches();
		assertEquals(1, caches.length);
		
		assertEquals(1, caches[0].classAccessed("org/sub/ClassABCD"));
		assertEquals(1, caches[0].classAccessed("org/sub/ClassB"));
		assertEquals(0, caches[0].classAccessed("org/ClassA"));
		
		assertTrue(engine.doesExtend(type, "java.lang.Object", true));
		assertEquals(2, caches[0].classAccessed("org/sub/ClassABCD")); // first class is always accessed
		assertEquals(1, caches[0].classAccessed("org/sub/ClassB")); // this is not being accessed again
		assertEquals(1, caches[0].classAccessed("org/ClassA")); // this one is accessed for the first time
	}
	
	private static class AccessLoggingClassReaderFactory implements TypeHierarchyClassReaderFactory {
		
		private TypeHierarchyClassReaderFactory readerFactory;
		private Map<IProject, AccessLoggingClassReader> readers;

		public AccessLoggingClassReaderFactory(TypeHierarchyClassReaderFactory readerFactory) {
			this.readers = new HashMap<IProject, AccessLoggingClassReader>();
			this.readerFactory = readerFactory;
		}

		public TypeHierarchyClassReader createClassReader(IProject project) {
			TypeHierarchyClassReader reader = readerFactory.createClassReader(project);
			AccessLoggingClassReader accessLoggingClassReader = new AccessLoggingClassReader(reader);
			this.readers.put(project, accessLoggingClassReader);
			return accessLoggingClassReader;
		}
		
		public AccessLoggingClassReader getReader(IProject project) {
			return this.readers.get(project);
		}
		
	}
	
	private static class AccessLoggingClassReader implements TypeHierarchyClassReader {

		private TypeHierarchyClassReader reader;
		private List<String> accessedClasses;

		public AccessLoggingClassReader(TypeHierarchyClassReader reader) {
			this.reader = reader;
			this.accessedClasses = new ArrayList<String>();
		}

		public TypeHierarchyElement readTypeHierarchyInformation(char[] fullyQualifiedClassName, IProject project) {
			this.accessedClasses.add(new String(fullyQualifiedClassName));
			return this.reader.readTypeHierarchyInformation(fullyQualifiedClassName, project);
		}
		
		public boolean classAccessed(String className) {
			return this.accessedClasses.contains(className);
		}

		public void cleanup() {
		}
		
	}
 	
	private static class AccessLoggingTypeHierarchyElementCacheFactory implements TypeHierarchyElementCacheFactory {
		
		private List<AccessLoggingTypeHierarchyElementCache> caches;

		public AccessLoggingTypeHierarchyElementCacheFactory() {
			this.caches = new ArrayList<AccessLoggingTypeHierarchyElementCache>();
		}

		public TypeHierarchyElementCache createTypeHierarchyElementCache() {
			AccessLoggingTypeHierarchyElementCache cache = new AccessLoggingTypeHierarchyElementCache();
			caches.add(cache);
			return cache;
		}
		
		public AccessLoggingTypeHierarchyElementCache[] getCaches() {
			return (AccessLoggingTypeHierarchyElementCache[]) caches.toArray(new AccessLoggingTypeHierarchyElementCache[caches.size()]);
		}
		
	}
	
	private static class AccessLoggingTypeHierarchyElementCache extends TypeHierarchyElementCache {
		
		private List<String> accessLog = new ArrayList<String>();

		@Override
		public TypeHierarchyElement get(char[] fullyQualifiedClassName) {
			accessLog.add(new String(fullyQualifiedClassName));
			return super.get(fullyQualifiedClassName);
		}
		
		public int classAccessed(String className) {
			int result = 0;
			for (String accessedClass : accessLog) {
				if (accessedClass.equals(className)) {
					result++;
				}
			}
			return result;
		}
		
	}
	
//	@Test
//	public void testExternalClassFile() throws Exception {
//		BytecodeTypeHierarchyClassReader reader = new BytecodeTypeHierarchyClassReader(null);
//		TypeHierarchyElement element = reader.readTypeHierarchy(new FileInputStream("randomclassfile.class"));
//		assertNotNull(element);
//	}

}
