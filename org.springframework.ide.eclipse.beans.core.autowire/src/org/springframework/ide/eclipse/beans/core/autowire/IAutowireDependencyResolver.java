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

import java.util.Set;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * Implementations of this interface can resolve autowired dependencies. 
 * @author Christian Dupuis
 * @since 2.2.7
 */
public interface IAutowireDependencyResolver {
	
	/**
	 * Resolve the given dependency, represented by the {@link DependencyDescriptor} instance. 
	 */
	void resolveDependency(DependencyDescriptor descriptor, Class<?> type, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter);

	/**
	 * Returns all bean names which bean class matches the given type. 
	 */
	String[] getBeansForType(Class<?> lookupType);
	
}
