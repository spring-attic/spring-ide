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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansProjectTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;
	
	private BeansModel realModel;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		model.addProject(beansProject);
		
		realModel = (BeansModel) BeansCorePlugin.getModel();
		BeansCorePlugin.setModel(model);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
		BeansCorePlugin.setModel(realModel);
	}

	@Test
	public void testBeansProjectXMLConfig() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
		assertTrue(config instanceof BeansConfig);
	}
	
	@Test
	public void testBeansProjectXMLConfigWithConfigSet() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		beansProject.addConfigSet(configSet);
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
		assertTrue(config instanceof BeansConfig);
	}
	
	@Test
	public void testBeansProjectXMLConfigWithExternalConfigSet() throws Exception {
		IProject secondProject = StsTestUtil.createPredefinedProject("beans-config-tests-2", "org.springframework.ide.eclipse.beans.core.tests");
		BeansProject secondBeansProject = new BeansProject(model, secondProject);
		model.addProject(secondBeansProject);
		
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("second-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		configSet.addConfig("/beans-config-tests-2/second-bean-config.xml");
		beansProject.addConfigSet(configSet);
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(2, configs.size());
		
		secondProject.delete(true, null);
	}
	
	@Test
	public void testBeansProjectJavaConfig() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("java:org.test.spring.SimpleConfigurationClass", config.getElementName());
		assertTrue(config instanceof BeansJavaConfig);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		assertEquals(type, ((BeansJavaConfig)config).getConfigClass());
	}
	
	@Test
	public void testBeansProjectMixedConfigs() throws Exception {
		Set<String> configs = new HashSet<String>();
		configs.add("basic-bean-config.xml");
		configs.add("java:org.test.spring.SimpleConfigurationClass");
		beansProject.setConfigs(configs);
		
		IBeansConfig xmlConfig = beansProject.getConfig("basic-bean-config.xml");
		IBeansConfig javaConfig = beansProject.getConfig("java:org.test.spring.SimpleConfigurationClass");
		
		assertEquals("basic-bean-config.xml", xmlConfig.getElementName());
		assertEquals("java:org.test.spring.SimpleConfigurationClass", javaConfig.getElementName());

		assertTrue(xmlConfig instanceof BeansConfig);
		assertTrue(javaConfig instanceof BeansJavaConfig);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		assertEquals(type, ((BeansJavaConfig)javaConfig).getConfigClass());
	}
	
	@Test
	public void testBeansProjectMixedConfigSet() throws Exception {
		IProject secondProject = StsTestUtil.createPredefinedProject("beans-config-tests-2", "org.springframework.ide.eclipse.beans.core.tests");
		BeansProject secondBeansProject = new BeansProject(model, secondProject);
		model.addProject(secondBeansProject);
		
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("second-bean-config.xml", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("java:org.test.spring.ExternalConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		configSet.addConfig("/beans-config-tests-2/java:org.test.spring.ExternalConfigurationClass");
		beansProject.addConfigSet(configSet);
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(2, configs.size());
		
		secondProject.delete(true, null);
	}
	
	@Test
	public void testBeansProjectXMLConfigFileRemoved() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		IFile configFile = (IFile) project.findMember("basic-bean-config.xml");
		beansProject.removeConfig(configFile);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(0, configs.size());
	}
	
	@Test
	public void testBeansProjectXMLConfigFileRemovedWithConfigSet() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		beansProject.addConfigSet(configSet);
		
		IFile configFile = (IFile) project.findMember("basic-bean-config.xml");
		beansProject.removeConfig(configFile);
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(0, configs.size());
	}
	
	@Test
	public void testBeansProjectXMLConfigFileRemovedWithExternalConfigSet() throws Exception {
		IProject secondProject = StsTestUtil.createPredefinedProject("beans-config-tests-2", "org.springframework.ide.eclipse.beans.core.tests");
		BeansProject secondBeansProject = new BeansProject(model, secondProject);
		model.addProject(secondBeansProject);
		
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("second-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		configSet.addConfig("/beans-config-tests-2/second-bean-config.xml");
		beansProject.addConfigSet(configSet);
		
		IFile configFile = (IFile) secondProject.findMember("second-bean-config.xml");
		beansProject.removeConfig(configFile);
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
		assertTrue(config instanceof BeansConfig);
		
		secondProject.delete(true, null);
	}
	
	@Test
	public void testBeansProjectXMLConfigFileRemovedViaName() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.removeConfig("basic-bean-config.xml");
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(0, configs.size());
	}

	@Test
	public void testBeansProjectXMLConfigFileRemovedViaNameFailing() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.removeConfig("test.xml");
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
		assertTrue(config instanceof BeansConfig);
	}
	
	@Test
	public void testBeansProjectJavaConfigFileRemoved() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		beansProject.removeConfig((IFile) type.getResource());
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(0, configs.size());
	}
	
	@Test
	public void testBeansProjectJavaConfigFileRemovedViaName() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		beansProject.removeConfig("java:org.test.spring.SimpleConfigurationClass");
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(0, configs.size());
	}

	@Test
	public void testBeansProjectJavaConfigFileRemovedWithConfigSet() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("java:org.test.spring.SimpleConfigurationClass");
		beansProject.addConfigSet(configSet);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		beansProject.removeConfig((IFile) type.getResource());
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(0, configs.size());
	}
	
	@Test
	public void testBeansProjectMixedConfigSetRemovedExternalJavaConfig() throws Exception {
		IProject secondProject = StsTestUtil.createPredefinedProject("beans-config-tests-2", "org.springframework.ide.eclipse.beans.core.tests");
		IJavaProject secondJavaProject = JdtUtils.getJavaProject(secondProject);
		BeansProject secondBeansProject = new BeansProject(model, secondProject);
		model.addProject(secondBeansProject);
		
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("second-bean-config.xml", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("java:org.test.spring.ExternalConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansConfigSet configSet = new BeansConfigSet(beansProject, "test-set", IBeansConfigSet.Type.MANUAL);
		configSet.addConfig("basic-bean-config.xml");
		configSet.addConfig("java:org.test.spring.ExternalConfigurationClass");
		beansProject.addConfigSet(configSet);
		
		IType type = secondJavaProject.findType("org.test.spring.ExternalConfigurationClass");
		beansProject.removeConfig((IFile) type.getResource());
		
		IBeansConfigSet set = beansProject.getConfigSet("test-set");
		Set<IBeansConfig> configs = set.getConfigs();
		assertEquals(1, configs.size());
		
		secondProject.delete(true, null);
	}
	
	@Test
	public void testGetBeanConfigsSimpleXMLConfig() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		IFile xmlFile = (IFile) project.findMember("basic-bean-config.xml");
		Set<IBeansConfig> configs = beansProject.getConfigs(xmlFile, false);
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testGetBeanConfigsSimpleWithTwoXMLConfigs() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("advanced-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		IFile xmlFile1 = (IFile) project.findMember("basic-bean-config.xml");
		Set<IBeansConfig> configs = beansProject.getConfigs(xmlFile1, false);
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());

		IFile xmlFile2 = (IFile) project.findMember("advanced-bean-config.xml");
		configs = beansProject.getConfigs(xmlFile2, false);
		assertEquals(1, configs.size());
		config = configs.iterator().next();
		assertEquals("advanced-bean-config.xml", config.getElementName());
	}
	
	@Test
	public void testGetBeanConfigsMixed() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);

		IFile xmlFile = (IFile) project.findMember("basic-bean-config.xml");
		Set<IBeansConfig> configs = beansProject.getConfigs(xmlFile, false);
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());

		IFile javaFile = (IFile) project.findMember("src/org/test/spring/SimpleConfigurationClass.java");
		configs = beansProject.getConfigs(javaFile, false);
		assertEquals(1, configs.size());
		config = configs.iterator().next();
		assertEquals("java:org.test.spring.SimpleConfigurationClass", config.getElementName());
	}
	
	@Test
	public void testGetBeanConfigsTwoInnerClassConfigs() throws Exception {
		beansProject.addConfig("java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass1", IBeansConfig.Type.MANUAL);
		beansProject.addConfig("java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass2", IBeansConfig.Type.MANUAL);

		IFile javaFile = (IFile) project.findMember("src/org/test/spring/TwoInnerConfigurationClasses.java");
		Set<IBeansConfig> configs = beansProject.getConfigs(javaFile, false);
		assertEquals(2, configs.size());

		Iterator<IBeansConfig> iterator = configs.iterator();
		IBeansConfig config1 = iterator.next();
		IBeansConfig config2 = iterator.next();
		
		assertTrue("java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass1".equals(config1.getElementName()) ||
				"java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass1".equals(config2.getElementName()));
		assertTrue("java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass2".equals(config1.getElementName()) ||
				"java:org.test.spring.TwoInnerConfigurationClasses$InnerConfigClass2".equals(config2.getElementName()));
	}
	
	@Test
	public void testGetBeanConfigsWithExternalFile() throws Exception {
		IProject secondProject = StsTestUtil.createPredefinedProject("beans-config-tests-2", "org.springframework.ide.eclipse.beans.core.tests");
		BeansProject secondBeansProject = new BeansProject(model, secondProject);
		model.addProject(secondBeansProject);
		
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		secondBeansProject.addConfig("second-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		IFile xmlFile = (IFile) secondProject.findMember("second-bean-config.xml");
		Set<IBeansConfig> configs = beansProject.getConfigs(xmlFile, false);
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("second-bean-config.xml", config.getElementName());
		
		secondProject.delete(true, null);
	}
	
	@Test
	public void testGetBeanConfigsWithIncludedConfigs() throws Exception {
		beansProject.addConfig("importing-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		IBeansConfig config = beansProject.getConfig("importing-bean-config.xml");
		Set<IBeansImport> imports = config.getImports();
		assertEquals(1, imports.size());
		
		IFile importedFile = (IFile) project.findMember("advanced-bean-config.xml");
		Set<IBeansConfig> configs = beansProject.getConfigs(importedFile, false);
		assertEquals(0, configs.size());
		
		configs = beansProject.getConfigs(importedFile, true);
		assertEquals(1, configs.size());
		IBeansConfig importedConfig = configs.iterator().next();
		assertEquals("advanced-bean-config.xml", importedConfig.getElementName());
	}
	
}
