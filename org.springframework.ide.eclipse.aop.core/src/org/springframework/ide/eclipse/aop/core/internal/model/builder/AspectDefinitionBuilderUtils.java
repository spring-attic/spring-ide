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
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;

/**
 * Factory class that creates *fresh* instances of non-thread safe
 * {@link IAspectDefinitionBuilder}.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AspectDefinitionBuilderUtils {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String ASPECT_DEFINITION_BUILDER_ELEMENT = "aspectDefinitionBuilder";

	private static final String ASPECT_DEFINITION_BUILDER_EXTENSION_POINT = Activator.PLUGIN_ID
			+ ".aspectdefinitionbuilder";

	public static Set<IAspectDefinitionBuilder> getAspectDefinitionBuilder() {
		return loadAspectDefinitionBuilder();
	}

	private static Set<IAspectDefinitionBuilder> loadAspectDefinitionBuilder() {
		Set<IAspectDefinitionBuilder> builders = new HashSet<IAspectDefinitionBuilder>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				ASPECT_DEFINITION_BUILDER_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (ASPECT_DEFINITION_BUILDER_ELEMENT.equals(config.getName())
							&& config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object handler = config.createExecutableExtension(CLASS_ATTRIBUTE);
							if (handler instanceof IAspectDefinitionBuilder) {
								builders.add((IAspectDefinitionBuilder) handler);
							}
						}
						catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
		return builders;
	}
}
