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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 */
public class JdtUtilsTest {

	private IProject project;
	
	@Before
	public void setup() throws Exception {
		project = StsTestUtil.createPredefinedProject("find-type-testcases", "org.springframework.ide.eclipse.beans.core.tests");
	}

	@After
	public void cleanup() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testGetJavaSourceType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaSourceNamedInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType$SourceLevelInnerType");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType$SourceLevelInnerType", type.getFullyQualifiedName());
		
		IType parentType = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType");
		assertNotNull(parentType);
		assertEquals(parentType, type.getParent());
	}
/*
	@Test
	public void testGetJavaSourceAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType$1");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType$1", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaSourceDoubleAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType$1$1");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType$1$1", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaSourceNamedInnerClassOfAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaSourceAnonymousInnerClassOfNamedInnerClassOfAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass$1");
		assertTrue(type.exists());
		assertEquals("find.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass$1", type.getFullyQualifiedName());
	}
*/
	@Test
	public void testGetJavaBinaryType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaBinaryNamedInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType$SourceLevelInnerType");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType$SourceLevelInnerType", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaBinaryAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType$1");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType$1", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaBinaryDoubleAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType$1$1");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType$1$1", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaBinaryNamedInnerClassOfAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass", type.getFullyQualifiedName());
	}

	@Test
	public void testGetJavaBinaryAnonymousInnerClassOfNamedInnerClassOfAnonymousInnerType() {
		IType type = JdtUtils.getJavaType(project, "find.binary.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass$1");
		assertTrue(type.exists());
		assertEquals("find.binary.type.tests.SourceLevelType$1$NamedInnerClassOfAnonymousInnerClass$1", type.getFullyQualifiedName());
	}

}
