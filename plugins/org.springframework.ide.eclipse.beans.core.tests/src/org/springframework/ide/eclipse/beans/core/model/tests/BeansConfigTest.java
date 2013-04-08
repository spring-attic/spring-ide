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
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansConfigTest {
	
	private IProject project;
	private IBeansModel model;
	private IBeansProject beansProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testBasicConfigBeans() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		Set<IBean> beans = BeansModelUtils.getBeans(config);
		assertEquals(1, beans.size());

		IBean bean = beans.iterator().next();
		assertEquals("simpleBean", bean.getElementName());
	}

	@Test
	public void testAdvancedConfigBeans() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "advanced-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		Set<IBean> beans = BeansModelUtils.getBeans(config);
		assertEquals(2, beans.size());

		assertNotNull(BeansModelUtils.getBean("simpleBean", config));
		
		IBean aspectjBean = BeansModelUtils.getBean("org.springframework.aop.config.internalAutoProxyCreator", config);
		assertNotNull(aspectjBean);
		assertEquals("org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator", aspectjBean.getClassName());
	}

	@Test
	public void testBasicComponentScanning() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "basic-component-scanning.xml", IBeansConfig.Type.MANUAL);
		
		IBean bean = BeansModelUtils.getBean("simpleScannedBean", config);
		assertEquals("simpleScannedBean", bean.getElementName());
	}

	@Test
	public void testComponentScanningWithEnableAnnotations() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "advanced-component-scanning.xml", IBeansConfig.Type.MANUAL);
		
		IBean simpleBean = BeansModelUtils.getBean("simpleScannedBean", config);
		assertEquals("simpleScannedBean", simpleBean.getElementName());

		IBean bean = BeansModelUtils.getBean("advancedConfigurationClass", config);
		assertEquals("advancedConfigurationClass", bean.getElementName());
		
		IBean transactionBean = BeansModelUtils.getBean("org.springframework.transaction.config.internalTransactionAdvisor", config);
		assertEquals("org.springframework.transaction.config.internalTransactionAdvisor", transactionBean.getElementName());

		IBean aspectjBean = BeansModelUtils.getBean("org.springframework.aop.config.internalAutoProxyCreator", config);
		assertNotNull(aspectjBean);
		assertEquals("org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator", aspectjBean.getClassName());
	}

}
