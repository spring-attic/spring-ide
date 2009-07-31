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
package org.springframework.ide.eclipse.beans.core.model.validation;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.objectweb.asm.ClassReader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating all relevant information used during
 * validation.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public interface IBeansValidationContext extends IValidationContext {

	/**
	 * Returns the {@link BeanDefinitionRegistry} that has been populated with the {@link BeanDefinition}s from the
	 * current xml configuration file.
	 * @return {@link BeanDefinitionRegistry} containing all definitions from the current configuration file
	 */
	BeanDefinitionRegistry getIncompleteRegistry();

	/**
	 * Returns the {@link BeanDefinitionRegistry} that has been populated with the {@link BeanDefinition}s from the
	 * complete {@link IBeansConfigSet}.
	 * @return {@link BeanDefinitionRegistry} containing all definitions from the complete {@link IBeansConfigSet}.
	 */
	BeanDefinitionRegistry getCompleteRegistry();

	/**
	 * Returns a {@link ClassReaderFactory}.
	 * <p>
	 * The purpose of this method is to enable caching of {@link ClassReader} instances throughout the entire validation
	 * process.
	 * @return a {@link ClassReaderFactory} instance
	 * @since 2.0.1
	 */
	ClassReaderFactory getClassReaderFactory();

	/**
	 * Returns the corresponding {@link IProject} that is the parent of the validation target.
	 */
	IProject getRootElementProject();

	/**
	 * Returns the corresponding {@link IResource} of the validation target.
	 */
	IResource getRootElementResource();

	/**
	 * Returns a matching {@link BeanDefinition} for the given <code>beanName</code> and <code>beanClass</code>.
	 * @param beanName the name of the bean to look for
	 * @param beanClass the class of the bean to look for
	 * @since 2.0.2
	 */
	Set<BeanDefinition> getRegisteredBeanDefinition(String beanName, String beanClass);

	/**
	 * Checks if a bean matching the given <code>beanName</code> and <code>beanClass</code> is registered in this
	 * context.
	 * @param beanName the name of the bean to look for
	 * @param beanClass the class of the bean to look for
	 * @return true if a bean with matching criteria is registered in this context; false otherwise
	 */
	boolean isBeanRegistered(String beanName, String beanClass);

}