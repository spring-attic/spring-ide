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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.core.PersistablePreferenceObjectSupport;

/**
 * Wraps contributions to the
 * <code>org.springframework.ide.eclipse.core.builders</code> extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectBuilderDefinition extends
		PersistablePreferenceObjectSupport {

	private static final String BUILDER_PREFIX = "builders.enable.";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ICON_ATTRIBUTE = "icon";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private String description;

	private String iconUri;

	private String id;

	private String name;

	private String namespaceUri;

	private IProjectBuilder projectBuilder;

	public ProjectBuilderDefinition(IConfigurationElement element)
			throws CoreException {
		init(element);
	}

	private void cleanup(IProject project) {
		if (!isEnabled(project) && project != null) {
			try {
				getProjectBuilder().cleanup(project, new NullProgressMonitor());
			}
			catch (CoreException e) {
				// ignore
			}
		}
	}

	public String getDescription() {
		return description;
	}

	public String getIconUri() {
		return iconUri;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	@Override
	protected String getPreferenceId() {
		return BUILDER_PREFIX + this.id;
	}

	public IProjectBuilder getProjectBuilder() {
		return projectBuilder;
	}

	private void init(IConfigurationElement element) throws CoreException {
		Object builder = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (builder instanceof IProjectBuilder) {
			projectBuilder = (IProjectBuilder) builder;
		}
		this.namespaceUri = element.getDeclaringExtension()
				.getNamespaceIdentifier();
		this.id = element.getAttribute(ID_ATTRIBUTE);
		this.name = element.getAttribute(NAME_ATTRIBUTE);
		this.description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		this.iconUri = element.getAttribute(ICON_ATTRIBUTE);
		String enabledByDefault = element
				.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			setEnabledByDefault(Boolean.valueOf(enabledByDefault));
		}
		else {
			setEnabledByDefault(true);
		}
	}

	@Override
	protected void onEnablementChanged(boolean isEnabled, IProject project) {
		cleanup(project);
	}

	@Override
	public String toString() {
		return id + " (" + projectBuilder.getClass().getName() + ")";
	}
}
