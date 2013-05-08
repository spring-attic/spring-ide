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
package org.springframework.ide.eclipse.beans.core.autowire;

import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Interface to allow extensions to contribute type resolvers for factory beans..
 * This is useful for cases where the type of the produced bean cannot be inferred
 * directly from the factory bean class.
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
public interface IFactoryBeanTypeResolver {
	
	public Class<?> resolveBeanTypeFromFactory(IBean factoryBean, Class<?> factoryBeanClass);

}
