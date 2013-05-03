/*******************************************************************************
 * Copyright (c) 2005, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;

/**
 * Default implementation of the {@link BeanDefinitionRegistry} interface: a full-fledged bean factory based on bean
 * definitions.
 * <p>
 * Typical usage is registering all bean definitions first (possibly read from a bean definition file), before accessing
 * beans. Bean definition lookup is therefore an inexpensive operation in a local bean definition table.
 * <p>
 * Can be used as a standalone bean factory, or as a superclass for custom bean factories. Note that readers for
 * specific bean definition formats are typically implemented separately rather than as bean factory subclasses.
 * <p>
 * <b>Creation of bean instances is not supported!!!</b>
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class DefaultBeanDefinitionRegistry extends AbstractBeanFactory implements BeanDefinitionRegistry {
	/**
	 * Whether to allow re-registration of a different definition with the same name
	 */
	private boolean allowBeanDefinitionOverriding = true;

	/**
	 * Whether to allow re-registration of a different alias with the same name
	 */
	private boolean allowAliasOverriding = true;

	/** Map of bean definition objects, keyed by bean name */
	private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();

	/** List of bean definition names, in registration order */
	private final List<String> beanDefinitionNames = new ArrayList<String>();

	/**
	 * Create a new DefaultBeanDefinitionRegistry.
	 */
	public DefaultBeanDefinitionRegistry() {
		super();
	}

	/**
	 * Create a new DefaultBeanDefinitionRegistry with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 */
	public DefaultBeanDefinitionRegistry(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Set if it should be allowed to override bean definitions by registering a different definition with the same
	 * name, automatically replacing the former. If not, an exception will be thrown. Default is true.
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Set if it should be allowed to override aliases by registering a different alias with the same name,
	 * automatically replacing the former. If not, an exception will be thrown. Default is true.
	 */
	public void setAllowAliasOverriding(boolean allowAliasOverriding) {
		this.allowAliasOverriding = allowAliasOverriding;
	}

	public int getBeanDefinitionCount() {
		return beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return beanDefinitionNames.toArray(new String[beanDefinitionNames.size()]);
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		String transformedBeanName = transformedBeanName(beanName);
		return beanDefinitionNames.contains(transformedBeanName);
	}

	/**
	 * Return a RootBeanDefinition for the given bean name, by merging with the parent if the given original bean
	 * definition is a child bean definition.
	 * 
	 * @param beanName the name of the bean definition
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
		String transformedBeanName = transformedBeanName(beanName);
		BeanDefinition bd = beanDefinitionMap.get(transformedBeanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return getMergedBeanDefinition(transformedBeanName, bd);
	}

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		Object oldBeanDefinition = beanDefinitionMap.get(beanName);
		if (oldBeanDefinition != null) {
			if (!allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException("Overrides bean '" + oldBeanDefinition + "'");
			}
		}
		else {
			beanDefinitionNames.add(beanName);
		}
		beanDefinitionMap.put(beanName, beanDefinition);
	}

	@Override
	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		try {
			super.registerAlias(beanName, alias);
		}
		catch (IllegalArgumentException e) {
			throw new BeanDefinitionStoreException(DefaultBeanDefinitionDocumentReader.ALIAS_ELEMENT, beanName, e
					.getMessage());
		}
		catch (BeanDefinitionStoreException e) {
			if (!allowAliasOverriding) {
				throw e;
			}
		}
	}

	@Override
	public Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
			throws BeansException {
		throw new BeanCreationException(beanName, "Not implemented");
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer();
		Iterator<String> bdNames = beanDefinitionNames.iterator();
		while (bdNames.hasNext()) {
			text.append(bdNames.next());
			if (bdNames.hasNext()) {
				text.append(", ");
			}
		}
		return text.toString();
	}

	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		if (beanDefinitionMap.containsKey(beanName)) {
			beanDefinitionMap.remove(beanName);
			beanDefinitionNames.remove(beanName);
		}
	}

	public void registerQualifierType(Class<?> cls) {
	}

	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return null;
	}
}
