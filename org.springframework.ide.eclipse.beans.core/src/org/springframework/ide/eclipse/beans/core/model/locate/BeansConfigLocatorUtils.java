/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;

/**
 * Some helper methods that deal with loading extension point contributions.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeansConfigLocatorUtils {
	
	/** The class extension point attribute */
	private static final String CLASS_ATTRIBUTE = "class";
	
	/** The beansconfig locator extension point */
	public static final String BEANSCONFIG_LOCATORS_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID
			+ ".beansconfiglocators";

	/**
	 * Returns a {@link Set} with all registered {@link IBeansConfigLocator}s.
	 */
	public static Set<IBeansConfigLocator> getBeansConfigLocators() {
		Set<IBeansConfigLocator> handlers = new HashSet<IBeansConfigLocator>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(BEANSCONFIG_LOCATORS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					String clazz = config.getAttribute(CLASS_ATTRIBUTE);
					if (clazz != null) {
						try {
							Object handler = config
									.createExecutableExtension(CLASS_ATTRIBUTE);
							if (handler instanceof IBeansConfigLocator) {
								IBeansConfigLocator namespaceHandler = (IBeansConfigLocator) handler;
								handlers.add(namespaceHandler);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return handlers;
	}

}