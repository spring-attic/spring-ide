/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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

	@Override
	public String toString() {
		return (name != null ? name : projectBuilder.getClass().getName());
	}
}
