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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.ide.eclipse.core.SpringCorePreferences;

/**
 * Wraps contributions to the
 * <code>org.springframework.ide.eclipse.core.builders</code> extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectBuilderDefinition {

	private static final String BUILDER_PREFIX = "builders.enable.";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private String description;

	private String id;

	private boolean isEnabled = true;

	private String name;

	private IProjectBuilder projectBuilder;

	public ProjectBuilderDefinition(IConfigurationElement element)
			throws CoreException {
		init(element);
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public IProjectBuilder getProjectBuilder() {
		return projectBuilder;
	}

	private void init(IConfigurationElement element) throws CoreException {
		Object builder = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (builder instanceof IProjectBuilder) {
			projectBuilder = (IProjectBuilder) builder;
		}
		this.id = element.getAttribute(ID_ATTRIBUTE);
		this.name = element.getAttribute(NAME_ATTRIBUTE);
		this.description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		String enabledByDefault = element
				.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			this.isEnabled = Boolean.valueOf(enabledByDefault);
		}
	}

	/**
	 * Returns true if the wrapped {@link IProjectBuilder} is enabled.
	 * @param project
	 * @return
	 */
	public boolean isEnabled(IProject project) {
		return SpringCorePreferences.getProjectPreferences(project).getBoolean(
				BUILDER_PREFIX + this.id, this.isEnabled);
	}

	public void setEnabled(boolean isEnabled, IProject project) {
		SpringCorePreferences.getProjectPreferences(project).putBoolean(
				BUILDER_PREFIX + this.id, isEnabled);
		this.isEnabled = isEnabled;
	}
}
