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
package org.springframework.ide.eclipse.data.jdt.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class RepositoryInformationTest {

	private IProject project;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("spring-data-testdata", "org.springframework.ide.eclipse.data.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testClassWithoutAnnotation() throws Exception {
		IType type = javaProject.findType("org.ClassWithoutAnyAnnotation");
		assertFalse(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testSimpleRepositoryDefinitionClass() throws Exception {
		IType type = javaProject.findType("org.ClassWithRepositoryDefinition");
		assertTrue(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testImplementingRepositoryInterface() throws Exception {
		IType type = javaProject.findType("org.ClassImplementingRepositoryInterface");
		assertTrue(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testImplementingRepositoryInterfaceThroughOtherInterface() throws Exception {
		IType type = javaProject.findType("org.ClassImplementingRepositoryInterfaceThroughOtherInterface");
		assertTrue(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testNoRepositoryDirectly() throws Exception {
		IType type = javaProject.findType("org.ClassNoRepository");
		assertFalse(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testNoRepositoryThroughCrudInterface() throws Exception {
		IType type = javaProject.findType("org.CrudRepo");
		assertTrue(RepositoryInformation.isSpringDataRepository(type));
	}

	@Test
	public void testMultipleExtendedInterfaces() throws Exception {
		IType type = javaProject.findType("org.InterfaceExtendingNoRepoAndRealRepo");
		assertTrue(RepositoryInformation.isSpringDataRepository(type));
	}

}
