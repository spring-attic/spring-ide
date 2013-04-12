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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansJavaConfigTest {
	
	private IProject project;
	private IBeansModel model;
	private IBeansProject beansProject;
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
	public void testBasicConfigBeans() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, IBeansConfig.Type.MANUAL);
		
		assertEquals("org.test.spring.SimpleConfigurationClass", config.getElementName());
		
		Set<IBean> beans = BeansModelUtils.getBeans(config);
		assertEquals(2, beans.size());

		IBean configBean = BeansModelUtils.getBean("simpleConfigurationClass", config);
		assertEquals("simpleConfigurationClass", configBean.getElementName());
		
		IBean bean = BeansModelUtils.getBean("simpleScannedBean", config);
		assertEquals("simpleScannedBean", bean.getElementName());
	}

	@Test
	public void testComponentScanningWithEnableAnnotations() throws Exception {
		IType configClass = javaProject.findType("org.test.advanced.AdvancedConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, IBeansConfig.Type.MANUAL);
		
		assertEquals("org.test.advanced.AdvancedConfigurationClass", config.getElementName());

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
