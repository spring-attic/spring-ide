/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.tests;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BeansModelUtilsTest {

	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private BeansModel originalModel;
	private IJavaProject javaProject;
	
	private BeansConfigId getConfigForFileName(String fName) {
        return BeansConfigFactory.getConfigId(project.getFile(fName));
    }

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-model-utils-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		model.addProject(beansProject);
		
		originalModel = (BeansModel) BeansCorePlugin.getModel();
		BeansCorePlugin.setModel(model);
		
		beansProject.addConfig(getConfigForFileName("basic-bean-config.xml"), IBeansConfig.Type.MANUAL);
		beansProject.addConfig(getConfigForFileName("basic-bean-config-2.xml"), IBeansConfig.Type.MANUAL);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
		BeansCorePlugin.setModel(originalModel);
	}
	
	@Test
	public void testNotInvolvedClassInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.NotInvolvedClass");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(0, beans.size());
	}

	@Test
	public void testNotInvolvedClassInGetConfigsByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.NotInvolvedClass");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBeansConfig> beans = BeansModelUtils.getConfigsByContainingTypes(resource, null);
		assertEquals(0, beans.size());
	}

	@Test
	public void testSimpleAndDirectBeanClassInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SimpleBeanClass");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(2, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean1 = iterator.next();
		IBean bean2 = iterator.next();

		assertTrue("simpleBean1".equals(bean1.getElementName()) || "simpleBean1".equals(bean2.getElementName()));
		assertTrue("simpleBean2".equals(bean1.getElementName()) || "simpleBean2".equals(bean2.getElementName()));
	}

	@Test
	public void testSimpleAndDirectBeanClassInGetConfigsByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SimpleBeanClass");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBeansConfig> beans = BeansModelUtils.getConfigsByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBeansConfig> iterator = beans.iterator();
		IBeansConfig config = iterator.next();
		assertEquals(beansProject.getConfig(getConfigForFileName("basic-bean-config.xml")), config);
	}

	@Test
	public void testDifferentBeanClassInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.DifferentBeanClass");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean = iterator.next();

		assertEquals("differentBean", bean.getElementName());
	}

	@Test
	public void testSuperTypeChangedInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SuperType");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean = iterator.next();
		assertEquals("supertypeChangedBean", bean.getElementName());
	}

	@Test
	public void testSuperTypeChangedInGetConfigsByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SuperType");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBeansConfig> beans = BeansModelUtils.getConfigsByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBeansConfig> iterator = beans.iterator();
		IBeansConfig config = iterator.next();
		assertEquals(beansProject.getConfig(getConfigForFileName("basic-bean-config.xml")), config);
	}

	@Test
	public void testSuperInterfaceChangedInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SuperInterface");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean = iterator.next();
		assertEquals("supertypeChangedBean", bean.getElementName());
	}

	@Test
	public void testSuperInterfaceChangedInGetConfigsByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SuperInterface");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBeansConfig> beans = BeansModelUtils.getConfigsByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBeansConfig> iterator = beans.iterator();
		IBeansConfig config = iterator.next();
		assertEquals(beansProject.getConfig(getConfigForFileName("basic-bean-config.xml")), config);
	}

	@Test
	public void testFactoryMethodBeanClassInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.FactoryMethodBean");
		IResource resource = type.getResource();
		assertNotNull(resource);

		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(0, beans.size());
	}

	@Test
	public void testFactoryMethodReturnTypeInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.FactoryCreatedBean");
		IResource resource = type.getResource();
		assertNotNull(resource);

		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(2, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean1 = iterator.next();
		IBean bean2 = iterator.next();

		assertTrue("factoryMethodCreatedBean".equals(bean1.getElementName()) || "factoryMethodCreatedBean".equals(bean2.getElementName()));
		assertTrue("factoryBeanCreatedBean".equals(bean1.getElementName()) || "factoryBeanCreatedBean".equals(bean2.getElementName()));
	}

	@Test
	public void testSupertypeFactoryMethodReturnTypeInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.SupertypeMethodCreatedBean");
		IResource resource = type.getResource();
		assertNotNull(resource);

		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(1, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean = iterator.next();
		assertEquals("supertypeFactoryMethodCreatedBean", bean.getElementName());
	}

	@Test
	public void testChainedFactoryMethodInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.ChainedFactoryCreatedBean");
		IResource resource = type.getResource();
		assertNotNull(resource);

		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(3, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean1 = iterator.next();
		IBean bean2 = iterator.next();
		IBean bean3 = iterator.next();

		assertTrue("chainedFactoryCreatedBean".equals(bean1.getElementName()) || "chainedFactoryCreatedBean".equals(bean2.getElementName()) || "chainedFactoryCreatedBean".equals(bean3.getElementName()));
		assertTrue("chainedFactoryBean1".equals(bean1.getElementName()) || "chainedFactoryBean1".equals(bean2.getElementName()) || "chainedFactoryBean1".equals(bean3.getElementName()));
		assertTrue("chainedFactoryBean2".equals(bean1.getElementName()) || "chainedFactoryBean2".equals(bean2.getElementName()) || "chainedFactoryBean2".equals(bean3.getElementName()));
	}
	
	@Test
	public void testBeanClassAcrossConfigsInGetBeansByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.UsedAcrossBeanConfigs");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBean> beans = BeansModelUtils.getBeansByContainingTypes(resource, null);
		assertEquals(2, beans.size());
		
		Iterator<IBean> iterator = beans.iterator();
		IBean bean1 = iterator.next();
		IBean bean2 = iterator.next();

		assertTrue("acrossConfigsBean1".equals(bean1.getElementName()) || "acrossConfigsBean1".equals(bean2.getElementName()));
		assertTrue("acrossConfigsBean2".equals(bean1.getElementName()) || "acrossConfigsBean2".equals(bean2.getElementName()));
	}

	@Test
	public void testBeanClassAcrossConfigsInGetConfigsByType() throws Exception {
		IType type = javaProject.findType("org.test.spring.UsedAcrossBeanConfigs");
		IResource resource = type.getResource();
		assertNotNull(resource);
		
		Set<IBeansConfig> beans = BeansModelUtils.getConfigsByContainingTypes(resource, null);
		assertEquals(2, beans.size());
		
		Iterator<IBeansConfig> iterator = beans.iterator();
		IBeansConfig config1 = iterator.next();
		IBeansConfig config2 = iterator.next();

		IBeansConfig realConfig1 = beansProject.getConfig(getConfigForFileName("basic-bean-config.xml"));
		IBeansConfig realConfig2 = beansProject.getConfig(getConfigForFileName("basic-bean-config-2.xml"));
		
		assertTrue(realConfig1 == config1 || realConfig1 == config2);
		assertTrue(realConfig2 == config1 || realConfig2 == config2);
	}

}
