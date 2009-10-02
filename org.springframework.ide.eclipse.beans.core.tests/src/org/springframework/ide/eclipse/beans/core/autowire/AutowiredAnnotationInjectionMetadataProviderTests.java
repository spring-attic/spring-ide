/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;

public class AutowiredAnnotationInjectionMetadataProviderTests extends BeansCoreTestCase {

	private IResource resource;

	@Override
	protected void setUp() throws Exception {
		resource = null;
		Thread.sleep(1500);
	}

	public void testResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 48 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testExtendedResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 82, 87, 93 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });
		allowedRefs.put("beanFactory", new Integer[] { 98 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testExtendedResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testExtendedResourceInjectionWithOverriding() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 87, 93 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });
		allowedRefs.put("beanFactory", new Integer[] { 98 });
		
		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testExtendedResourceInjectionWithOverriding-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testExtendedResourceInjectionWithSkippedOverriddenMethods() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 68, 87, 93, 140 });
		allowedRefs.put("nestedTestBean", new Integer[] { 87 });

		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testExtendedResourceInjectionWithSkippedOverriddenMethods-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testOptionalResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 149, 161, 166 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 156, 166 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 156, 166 });
		allowedRefs.put("indexedTestBean", new Integer[] { 166 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testOptionalResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testOptionalResourceInjectionWithNoDependencies() throws Exception {
		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testOptionalResourceInjectionWithNoDependencies-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();

		assertTrue(references.size() == 0);
	}

	public void testConstructorResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 42, 244, 262, 278 });
		allowedRefs.put("nestedTestBean", new Integer[] { 262 });
		allowedRefs.put("beanFactory", new Integer[] { 262 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testConstructorResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testConstructorResourceInjectionWithMultipleCandidates() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 312, 317 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 317 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 317 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testConstructorResourceInjectionWithMultipleCandidates-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testConstructorResourceInjectionWithMultipleCandidatesAsCollection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 356, 361 });
		allowedRefs.put("nestedTestBean1", new Integer[] { 361 });
		allowedRefs.put("nestedTestBean2", new Integer[] { 361 });

		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testConstructorResourceInjectionWithMultipleCandidatesAsCollection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testConstructorResourceInjectionWithMultipleCandidatesAndFallback() {
		// DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		// AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		// bpp.setBeanFactory(bf);
		// bf.addBeanPostProcessor(bpp);
		// bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorsResourceInjectionBean.class));
		// TestBean tb = new TestBean();
		// bf.registerSingleton("testBean", tb);
		//
		// ConstructorsResourceInjectionBean bean = (ConstructorsResourceInjectionBean) bf.getBean("annotatedBean");
		// assertSame(tb, bean.getTestBean3());
		// assertNull(bean.getTestBean4());
		// bf.destroySingletons();
	}

	public void testConstructorResourceInjectionWithMultipleCandidatesAndDefaultFallback() {
		// DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		// AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		// bpp.setBeanFactory(bf);
		// bf.addBeanPostProcessor(bpp);
		// bf.registerBeanDefinition("annotatedBean", new RootBeanDefinition(ConstructorsResourceInjectionBean.class));
		//
		// ConstructorsResourceInjectionBean bean = (ConstructorsResourceInjectionBean) bf.getBean("annotatedBean");
		// assertNull(bean.getTestBean3());
		// assertNull(bean.getTestBean4());
		// bf.destroySingletons();
	}

	public void testConstructorInjectionWithMap() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 394 });
		allowedRefs.put("testBean2", new Integer[] { 394 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testConstructorInjectionWithMap-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testFieldInjectionWithMap() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 407 });
		allowedRefs.put("testBean2", new Integer[] { 407 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testFieldInjectionWithMap-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testMethodInjectionWithMap() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 423 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testMethodInjectionWithMap-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testMethodInjectionWithMapAndMultipleMatches() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean1", new Integer[] { 423 });
		allowedRefs.put("testBean2", new Integer[] { 423 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndMultipleMatches-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testMethodInjectionWithMapAndMultipleMatchesButOnlyOneAutowireCandidate() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean2", new Integer[] { 423 });

		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndMultipleMatchesButOnlyOneAutowireCandidate-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testMethodInjectionWithMapAndNoMatches() throws Exception {
		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testMethodInjectionWithMapAndNoMatches-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);
	}

	public void testObjectFactoryInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 441 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testObjectFactoryInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testObjectFactoryQualifierInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 453 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testObjectFactoryQualifierInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("annotatedBean");

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

	public void testCustomAnnotationRequiredFieldResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 464 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredFieldResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("customBean");

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

	public void testCustomAnnotationRequiredFieldResourceInjectionFailsWhenNoDependencyFound() throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredFieldResourceInjectionFailsWhenNoDependencyFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	public void testCustomAnnotationRequiredMethodResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 477 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("customBean");

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

	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenNoDependencyFound() throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjectionFailsWhenNoDependencyFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	public void testCustomAnnotationRequiredMethodResourceInjectionFailsWhenMultipleDependenciesFound()
			throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationRequiredMethodResourceInjectionFailsWhenMultipleDependenciesFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}

	public void testCustomAnnotationOptionalFieldResourceInjection() throws Exception {
		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalFieldResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);
	}

	public void testCustomAnnotationOptionalFieldResourceInjectionWhenNoDependencyFound() throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalFieldResourceInjectionWhenNoDependencyFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}
	
	public void testCustomAnnotationOptionalMethodResourceInjection() throws Exception {
		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 504 });

		resource = createPredefinedProjectAndGetResource("autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjection-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = config.getBean("customBean");

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

	public void testCustomAnnotationOptionalMethodResourceInjectionWhenNoDependencyFound() throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjectionWhenNoDependencyFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 0);
	}
	
	public void testCustomAnnotationOptionalMethodResourceInjectionWhenMultipleDependenciesFound() throws Exception {
		resource = createPredefinedProjectAndGetResource(
				"autowire",
				"src/org/springframework/beans/factory/annotation/testCustomAnnotationOptionalMethodResourceInjectionWhenMultipleDependenciesFound-context.xml");
		IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) resource);
		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		assertTrue(references.size() == 0);

		assertTrue(provider.getValidationProblems().size() == 1);
	}
}
