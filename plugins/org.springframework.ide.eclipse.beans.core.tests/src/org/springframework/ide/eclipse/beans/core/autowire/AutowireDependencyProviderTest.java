/*******************************************************************************
 * Copyright (c) 2005, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.FactoryBeanTypeResolverExtensions;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Tomasz Zarna
 * @author Martin Lippert
 * @since 3.3.0
 */
public class AutowireDependencyProviderTest {
	
	private IProject project;
	private BeansModel model;
	private IBeansProject beansProject;
	private BeansModel modelBackup;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("autowire", "org.springframework.ide.eclipse.beans.core.tests");
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		
		modelBackup = (BeansModel) BeansCorePlugin.getModel();
		BeansCorePlugin.setModel(model);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
		BeansCorePlugin.setModel(modelBackup);
	}

	@Test
	public void testResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testResourceInjection-context.xml", IBeansConfig.Type.MANUAL);
		
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 48 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);
		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testExtendedResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 82, 87, 93 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });
		allowedRefs.put("beanFactory", new Integer[] { 98 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 7);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedResourceInjectionWithOverriding() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testExtendedResourceInjectionWithOverriding-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 87, 93 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });
		allowedRefs.put("beanFactory", new Integer[] { 98 });
		
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 6);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedResourceInjectionWithSkippedOverriddenMethods() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testExtendedResourceInjectionWithSkippedOverriddenMethods-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 87, 93, 140 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 6);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testOptionalResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testOptionalResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 149, 161, 166 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 156, 166 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 156, 166 });
		allowedRefs.put("indexedTestBean", new Integer[] { 166 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 9);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}

	}

	@Test
	public void testOptionalResourceInjectionWithNoDependencies() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testOptionalResourceInjectionWithNoDependencies-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();

		assertTrue(references.size() == 0);
	}

	@Test
	public void testConstructorResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testConstructorResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 244, 262, 278 });
		allowedRefs.put("nestedTestBean", new Integer[] { 262 });
		allowedRefs.put("beanFactory", new Integer[] { 262 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 6);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testConstructorResourceInjectionWithMultipleCandidates() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testConstructorResourceInjectionWithMultipleCandidates-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 312, 317 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 317 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 317 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 4);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testConstructorResourceInjectionWithMultipleCandidatesAsCollection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testConstructorResourceInjectionWithMultipleCandidatesAsCollection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 356, 361 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 361 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 361 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 4);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testConstructorInjectionWithMap() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testConstructorInjectionWithMap-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 394 });
		allowedRefs.put("testBean2", new Integer[] { 394 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testFieldInjectionWithMap() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testFieldInjectionWithMap-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 407 });
		allowedRefs.put("testBean2", new Integer[] { 407 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testMethodInjectionWithMap() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testMethodInjectionWithMap-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 423 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testMethodInjectionWithMapAndMultipleMatches() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndMultipleMatches-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 423 });
		allowedRefs.put("testBean2", new Integer[] { 423 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testMethodInjectionWithMapAndMultipleMatchesButOnlyOneAutowireCandidate() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndMultipleMatchesButOnlyOneAutowireCandidate-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean2", new Integer[] { 423 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testMethodInjectionWithMapAndNoMatches() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndNoMatches-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);
	}

	@Test
	public void testObjectFactoryInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testObjectFactoryInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 441 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertEquals(1, references.size());
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testObjectFactoryQualifierInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testObjectFactoryQualifierInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 453 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testCustomAnnotationRequiredFieldResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredFieldResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 464 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("customBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testCustomAnnotationRequiredFieldResourceInjectionFailsWhenNoDependencyFound() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredFieldResourceInjectionFailsWhenNoDependencyFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testCustomAnnotationRequiredMethodResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 477 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("customBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenNoDependencyFound() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjectionFailsWhenNoDependencyFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenMultipleDependenciesFound()
			throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjectionFailsWhenMultipleDependenciesFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testCustomAnnotationOptionalFieldResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalFieldResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);
	}

	@Test
	public void testCustomAnnotationOptionalFieldResourceInjectionWhenNoDependencyFound() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalFieldResourceInjectionWhenNoDependencyFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testCustomAnnotationOptionalMethodResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 504 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("customBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testCustomAnnotationOptionalMethodResourceInjectionWhenNoDependencyFound() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjectionWhenNoDependencyFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 0);
	}

	@Test
	public void testCustomAnnotationOptionalMethodResourceInjectionWhenMultipleDependenciesFound() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjectionWhenMultipleDependenciesFound-context.xml", IBeansConfig.Type.MANUAL);

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	@Test
	public void testStringFactoryInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testStringTypeFactoryBean-context.xml", IBeansConfig.Type.MANUAL);
		
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 554 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);
		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testUnknownFactoryInjectionWithoutSpecificExtension() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testUnknownTypeFactoryBean-context.xml", IBeansConfig.Type.MANUAL);
		
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("unknownFactoryBean", new Integer[] { 580 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		FactoryBeanTypeResolverExtensions.setFactoryBeanTypeResolvers(new IFactoryBeanTypeResolver[0]);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);
	}

	@Test
	public void testUnknownFactoryInjectionWithSpecificExtension() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testUnknownTypeFactoryBean-context.xml", IBeansConfig.Type.MANUAL);
		
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("unknownFactoryBean", new Integer[] { 580 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		IFactoryBeanTypeResolver testFactoryBeanTypeResolver = new IFactoryBeanTypeResolver() {
			public Class<?> resolveBeanTypeFromFactory(IBean factoryBean, Class<?> factoryBeanClass) {
				if (factoryBeanClass.getName().equals("org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessorTests$UnknownFactoryBean")) {
					try {
						return factoryBeanClass.getClassLoader().loadClass("test.beans.TestBean");
					} catch (ClassNotFoundException e) {
						fail(e.getMessage());
					}
				}
				return null;
			}
		};
		FactoryBeanTypeResolverExtensions.setFactoryBeanTypeResolvers(new IFactoryBeanTypeResolver[] {testFactoryBeanTypeResolver});

		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("autowiredBeanWithUnknownType", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);
		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}
	
	@Test
	public void testEnvironmentInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/beans/factory/annotation/testEnvironmentInjection-context.xml", IBeansConfig.Type.MANUAL);
		
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("environment", new Integer[] { 591 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("environmentAutowiredBean", config);

//		assertTrue(references.size() == 1);
//		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 1);
		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}



}
