/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Helper class that loads {@link ProjectBuilderDefinition} from the Platforms
 * enxtension point registry.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectBuilderDefinitionFactory {

	public static final String BUILDERS_EXTENSION_POINT = SpringCore.PLUGIN_ID
			+ ".builders";

	public static List<ProjectBuilderDefinition> getProjectBuilderDefinitions() {
		List<ProjectBuilderDefinition> builderDefinitions = new ArrayList<ProjectBuilderDefinition>();
		for (IExtension extension : Platform.getExtensionRegistry()
				.getExtensionPoint(BUILDERS_EXTENSION_POINT).getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				try {
					ProjectBuilderDefinition builderHolder = new ProjectBuilderDefinition(
							element);
					builderDefinitions.add(builderHolder);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		return builderDefinitions;
	}
}
