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
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.ide.eclipse.beans.core.groovy.tests.Activator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansJavaGroovyConfigTest {
	
	private IProject project;
	private IBeansModel model;
	private IBeansProject beansProject;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", Activator.PLUGIN_ID);
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}
	
	@Test
	public void testConfigWithoutClass() throws Exception {
		BeansJavaConfig config = new BeansJavaConfig(beansProject, null, "org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		assertNull(config.getConfigClass());
		assertEquals("org.test.spring.SimpleConfigurationClass", config.getConfigClassName());
		
		IModelElement[] children = config.getElementChildren();
		assertEquals(0, children.length);

		assertNull(config.getElementResource());
	}

	@Test
	public void testBasicConfigBeans() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, "org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		assertEquals("java:org.test.spring.SimpleConfigurationClass", config.getElementName());
		
		IBean configBean = BeansModelUtils.getBean("simpleConfigurationClass", config);
		assertEquals("simpleConfigurationClass", configBean.getElementName());
		
		IBean bean = BeansModelUtils.getBean("simpleScannedBean", config);
		assertEquals("simpleScannedBean", bean.getElementName());

		IBean processor1 = BeansModelUtils.getBean(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME, config);
		assertNotNull(processor1);
		
		IBean processor2 = BeansModelUtils.getBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME, config);
		assertNotNull(processor2);

		IBean processor3 = BeansModelUtils.getBean(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME, config);
		assertNotNull(processor3);
		
		IBean processor4 = BeansModelUtils.getBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME, config);
		assertNotNull(processor4);
		
		IBean processor5 = BeansModelUtils.getBean(AnnotationConfigUtils.PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME, config);
		assertNotNull(processor5);

		Set<IBean> beans = BeansModelUtils.getBeans(config);
		assertEquals(7, beans.size());

		IModelElement[] children = config.getElementChildren();
		assertEquals(7, children.length);
	}

	@Test
	public void testComponentScanningWithEnableAnnotations() throws Exception {
		IType configClass = javaProject.findType("org.test.advanced.AdvancedConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, "org.test.advanced.AdvancedConfigurationClass", IBeansConfig.Type.MANUAL);
		
		assertEquals("java:org.test.advanced.AdvancedConfigurationClass", config.getElementName());

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

	@Test
	public void testComponentScanningWithProfile() throws Exception {
		IType configClass = javaProject.findType("org.test.profile.ProfileConfigurationClass");
		BeansJavaConfig config = new BeansJavaConfig(beansProject, configClass, "org.test.profile.ProfileConfigurationClass", IBeansConfig.Type.MANUAL);
		
		IBean simpleBean = BeansModelUtils.getBean("simpleScannedBean", config);
		assertEquals("simpleScannedBean", simpleBean.getElementName());

		IBean configClassBean = BeansModelUtils.getBean("profileConfigurationClass", config);
		assertEquals("profileConfigurationClass", configClassBean.getElementName());
		
		assertEquals(1, getProfiles(simpleBean).size());
		assertEquals("testProfile", getProfiles(simpleBean).iterator().next());
		
		assertEquals(0, getProfiles(configClassBean).size());
	}

	protected Set<String> getProfiles(IModelElement element) {
		Set<String> profiles = new HashSet<String>();
		while (element != null) {
			if (element instanceof IProfileAwareBeansComponent) {
				profiles.addAll(((IProfileAwareBeansComponent) element).getProfiles());
			}
			element = element.getElementParent();
		}
		
		return profiles;
	}

}
