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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Some helper methods that deal with loading extension point contributions.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeansConfigLocatorFactory {
	
	/** The beansconfig locator extension point */
	public static final String BEANSCONFIG_LOCATORS_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID
			+ ".beansconfiglocators";

	/**
	 * Returns a {@link List} with all registered {@link BeansConfigLocatorDefinition}s.
	 */
	public static List<BeansConfigLocatorDefinition> getBeansConfigLocatorDefinitions() {
		List<BeansConfigLocatorDefinition> handlers = new ArrayList<BeansConfigLocatorDefinition>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(BEANSCONFIG_LOCATORS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					try {
						BeansConfigLocatorDefinition builderDefinition =
								new BeansConfigLocatorDefinition(config);
						handlers.add(builderDefinition);
					}
					catch (CoreException e) {
						SpringCore.log(e);
					}
				}
			}
		}
		
		// Sort definitions based on there defined order
		Collections.sort(handlers, new Comparator<BeansConfigLocatorDefinition>(){

			public int compare(BeansConfigLocatorDefinition o1, BeansConfigLocatorDefinition o2) {
				return o1.getOrder().compareTo(o2.getOrder());
			}});
		
		return handlers;
		
	}

}