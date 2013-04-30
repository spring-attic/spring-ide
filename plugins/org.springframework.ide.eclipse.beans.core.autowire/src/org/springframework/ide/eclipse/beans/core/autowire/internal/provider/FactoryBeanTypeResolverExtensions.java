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
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.IFactoryBeanTypeResolver;

/**
 * Managing the available extensions for IFactoryBeanTypeResolver
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
public class FactoryBeanTypeResolverExtensions {
	
	private static final String FACTORY_TYPE_RESOLVERS_EXTENSION_POINT = "org.springframework.ide.eclipse.beans.core.autowire" + ".factorybeantyperesolvers";
	private static final Object FACTORY_TYPE_RESOLVER_ELEMENT = "factoryBeanTypeResolver";
	private static final String CLASS_ATTRIBUTE = "class";
	
	private static IFactoryBeanTypeResolver[] resolvers;

	public static void setFactoryBeanTypeResolvers(IFactoryBeanTypeResolver[] resolvers) {
		FactoryBeanTypeResolverExtensions.resolvers = resolvers;
	}
	
	public static IFactoryBeanTypeResolver[] getFactoryBeanTypeResolvers() {
		if (resolvers == null) {
			initializeResolvers();
		}

		return resolvers;
	}

	private static void initializeResolvers() {
		List<IFactoryBeanTypeResolver> resolverExtensions = new ArrayList<IFactoryBeanTypeResolver>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				FACTORY_TYPE_RESOLVERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (FACTORY_TYPE_RESOLVER_ELEMENT.equals(config.getName())
							&& config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object handler = config.createExecutableExtension(CLASS_ATTRIBUTE);
							if (handler instanceof IFactoryBeanTypeResolver) {
								resolverExtensions.add((IFactoryBeanTypeResolver) handler);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		resolvers = (IFactoryBeanTypeResolver[]) resolverExtensions.toArray(new IFactoryBeanTypeResolver[resolverExtensions.size()]);
	}
	
}
