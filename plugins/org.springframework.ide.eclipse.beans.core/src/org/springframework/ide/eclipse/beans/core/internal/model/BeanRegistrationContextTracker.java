/*******************************************************************************
 * Copyright (c) 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

/**
 * Used by BeansConfig to keep track of bean registrations as they happen. We need this
 * to detect implicitly defined beans, and associating them with the correct
 * BeansComponent (i.e. we want to associate implictly defined beans with the {@link CompositeComponentDefinition}
 * that was the 'current' one when the implicit bean got registered.
 *
 * @author Kris De Volder
 */
public class BeanRegistrationContextTracker {

	private Stack<CompositeComponentDefinition> contexts;
	private Map<CompositeComponentDefinition, BeanDefinitionRegistry> registries;

	public BeanRegistrationContextTracker() {
		clear();
	}

	/**
	 * Called when starting to process bean definitions inside a given
	 */
	public void enter(CompositeComponentDefinition context) {
		contexts.push(context);
	}

	/**
	 * Called when finished processing bean definitions in a given context.
	 */
	public void exit(CompositeComponentDefinition context) {
		Assert.isTrue(context==contexts.pop());
	}

	/**
	 * Called when a bean gets registered.
	 */
	public void registerBean(String beanName, BeanDefinition def) {
		BeanDefinitionRegistry registry = getRegistry();
		if (registry!=null) {
			registry.registerBeanDefinition(beanName, def);
		}
	}

	protected BeanDefinitionRegistry getRegistry() {
		if (!contexts.isEmpty()) {
			CompositeComponentDefinition currentContext = contexts.peek();
			BeanDefinitionRegistry r = registries.get(currentContext);
			if (r==null) {
				r = createRegistry();
				registries.put(currentContext, r);
			}
			return r;
		}
		return null;
	}

	protected SimpleBeanDefinitionRegistry createRegistry() {
		SimpleBeanDefinitionRegistry r = new SimpleBeanDefinitionRegistry();
		return r;
	}

	public void clear() {
		contexts = new Stack<CompositeComponentDefinition>();
		registries = new HashMap<CompositeComponentDefinition, BeanDefinitionRegistry>();
	}

	public CompositeComponentDefinition currentContext() {
		if (!contexts.isEmpty()) {
			return contexts.peek();
		}
		return null;
	}

	public BeanDefinitionRegistry getRegistry(CompositeComponentDefinition context) {
		if (registries!=null) {
			return registries.get(context);
		}
		return null;
	}

}
