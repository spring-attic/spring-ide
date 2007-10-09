/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.objectweb.asm.ClassReader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.ValidationRuleUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.util.Assert;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating all
 * relevant information used during validation.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansValidationContext extends AbstractValidationContext {

	private static final char KEY_SEPARATOR_CHAR = '/';

	private BeanDefinitionRegistry incompleteRegistry;

	private BeanDefinitionRegistry completeRegistry;

	private ClassReaderFactory classReaderFactory;

	private Map<String, BeanDefinition> beanLookupCache;

	public BeansValidationContext(IBeansConfig config,
			IResourceModelElement contextElement) {
		super(config, contextElement);

		incompleteRegistry = createRegistry(config, contextElement, false);
		completeRegistry = createRegistry(config, contextElement, true);

		beanLookupCache = new HashMap<String, BeanDefinition>();
	}

	public BeanDefinitionRegistry getIncompleteRegistry() {
		return incompleteRegistry;
	}

	public BeanDefinitionRegistry getCompleteRegistry() {
		return completeRegistry;
	}

	private BeanDefinitionRegistry createRegistry(IBeansConfig config,
			IResourceModelElement contextElement, boolean fillCompletely) {
		DefaultBeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
		if (contextElement instanceof BeansConfigSet) {
			IBeansConfigSet configSet = (IBeansConfigSet) contextElement;
			if (fillCompletely) {
				registry.setAllowAliasOverriding(true);
				registry.setAllowBeanDefinitionOverriding(true);
			}
			else {
				registry.setAllowAliasOverriding(configSet
						.isAllowAliasOverriding());
				registry.setAllowBeanDefinitionOverriding(configSet
						.isAllowBeanDefinitionOverriding());
			}
			for (IBeansConfig csConfig : configSet.getConfigs()) {
				if (!fillCompletely && config.equals(csConfig)) {
					break;
				}
				BeansModelUtils.register(csConfig, registry);
			}
		}
		else {
			registry.setAllowAliasOverriding(false);
			registry.setAllowBeanDefinitionOverriding(false);
			if (fillCompletely) {
				BeansModelUtils.register(config, registry);
			}
		}
		return registry;
	}

	/**
	 * Returns a {@link ClassReaderFactory}.
	 * <p>
	 * The purpose of this method is to enable caching of {@link ClassReader}
	 * instances throughout the entire validation process.
	 * @return a {@link ClassReaderFactory} instance
	 * @since 2.0.1
	 */
	public ClassReaderFactory getClassReaderFactory() {
		synchronized (this) {
			if (this.classReaderFactory == null) {
				this.classReaderFactory = new CachingClassReaderFactory(
						JdtUtils.getClassLoader(getRootElement()
								.getElementResource().getProject(), false));
			}
		}
		return this.classReaderFactory;
	}

	/**
	 * Returns the corresponding {@link IProject} that is the parent of the
	 * validation target.
	 */
	public IProject getRootElementProject() {
		return (getRootElement().getElementResource() != null ? getRootElement()
				.getElementResource().getProject()
				: null);
	}

	/**
	 * Returns the corresponding {@link IResource} of the validation target.
	 */
	public IResource getRootElementResource() {
		return getRootElement().getElementResource();
	}

	/**
	 * Checks if a bean matching the given <code>beanName</code> and
	 * <code>beanClass</code> is registered in this context.
	 * @param beanName the name of the bean to look for
	 * @param beanClass the class of the bean to look for
	 * @return true if a bean with matching criteria is registered in this
	 * context; false otherwise
	 */
	public boolean isBeanRegistered(String beanName, String beanClass) {
		Assert.notNull(beanName);
		Assert.notNull(beanClass);

		String key = beanClass + KEY_SEPARATOR_CHAR + beanName;
		if (beanLookupCache.containsKey(key)) {
			return beanLookupCache.get(key) != null;
		}
		BeanDefinition bd = ValidationRuleUtils.getBeanDefinition(beanName, 
				beanClass, this);
		// as we don't use a Hashtable we can insert null values
		beanLookupCache.put(key, bd);
		return bd != null;
	}
}