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
package org.springframework.ide.eclipse.data.internal.validation;

import org.springframework.ide.eclipse.beans.core.autowire.IFactoryBeanTypeResolver;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.data.SpringDataUtils;

/**
 * Extension that provides exact repository type resolution for the repository factory beans.
 * This is used to provide more exact validations, for example for autowired injection points.
 * 
 * @see IFactoryBeanTypeResolver
 *
 * @author Martin Lippert
 * @since 3.3.0
 */
public class RepositoryFactoryBeanTypeResolver implements IFactoryBeanTypeResolver {

	public Class<?> resolveBeanTypeFromFactory(IBean factoryBean, Class<?> factoryBeanClass) {
		if (factoryBean != null && SpringDataUtils.isRepositoryBean(factoryBean)) {
			String interfaceName = SpringDataUtils.getRepositoryInterfaceName(factoryBean);
			if (interfaceName != null) {
				try {
					Class<?> repoInterfaceClass = ClassUtils.loadClass(interfaceName);
					return repoInterfaceClass;
				}
				catch (Exception e) {
				}
			}
		}
		
		return null;
	}

}
