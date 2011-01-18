/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Helper class that loads {@link ProjectBuilderDefinition} from the Platforms extension point registry.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ProjectBuilderDefinitionFactory {

	public static final String BUILDERS_EXTENSION_POINT = SpringCore.PLUGIN_ID + ".builders";

	public static List<ProjectBuilderDefinition> getProjectBuilderDefinitions() {
		List<ProjectBuilderDefinition> builderDefinitions = new ArrayList<ProjectBuilderDefinition>();
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(BUILDERS_EXTENSION_POINT)
				.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					ProjectBuilderDefinition builderDefinition = new ProjectBuilderDefinition(element);
					builderDefinitions.add(builderDefinition);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
				catch (Exception e) {
					// ignore this
				}
			}
		}

		// Sort definitions based on there defined order
		Collections.sort(builderDefinitions, new Comparator<ProjectBuilderDefinition>() {

			public int compare(ProjectBuilderDefinition o1, ProjectBuilderDefinition o2) {
				return o1.getOrder().compareTo(o2.getOrder());
			}
		});

		return builderDefinitions;
	}
}
