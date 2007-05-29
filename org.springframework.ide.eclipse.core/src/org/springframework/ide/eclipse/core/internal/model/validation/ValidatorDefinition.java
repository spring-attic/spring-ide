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
package org.springframework.ide.eclipse.core.internal.model.validation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.model.validation.IValidator;

/**
 * Wraps an {@link IValidator} and all the information from it's definition
 * via the corresponding extension point.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidatorDefinition {

	private static final String ENABLEMENT_PREFIX = "validator.rule.enable.";
	private static final String CLASS_ATTRIBUTE = "class";
	private static final String ID_ATTRIBUTE = "id";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";
	private static final String DESCRIPTION_ATTRIBUTE = "description";
	private static final String ICON_ATTRIBUTE = "icon";

	private IValidator validator;
	private String id;
	private String name;
	private boolean isEnabled = true;
	private String description;
	private String iconUri;
	private String namespaceUri;

	public ValidatorDefinition(IConfigurationElement element)
			throws CoreException {
		init(element);
	}

	@SuppressWarnings("unchecked")
	private void init(IConfigurationElement element) throws CoreException {
		Object executable = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (executable instanceof IValidator) {
			validator = (IValidator) executable;
		}
		id = element.getContributor().getName() + "."
				+ element.getAttribute(ID_ATTRIBUTE);
		name = element.getAttribute(NAME_ATTRIBUTE);
		description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		iconUri = element.getAttribute(ICON_ATTRIBUTE);
		namespaceUri = element.getDeclaringExtension().getNamespaceIdentifier();
		String enabledByDefault = element
				.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			isEnabled = Boolean.valueOf(enabledByDefault);
		}
	}

	public IValidator getValidator() {
		return validator;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getIconUri() {
		return iconUri;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	/**
	 * Returns true if the wrapped {@link IValidator} is enabled.
	 */
	public boolean isEnabled(IProject project) {
		return SpringCorePreferences.getProjectPreferences(project).getBoolean(
				ENABLEMENT_PREFIX + id, isEnabled);
	}

	public void setEnabled(boolean isEnabled, IProject project) {
		SpringCorePreferences.getProjectPreferences(project).putBoolean(
				ENABLEMENT_PREFIX + id, isEnabled);
		this.isEnabled = isEnabled;
	}

	@Override
	public String toString() {
		return id + " (" + validator.getClass().getName() + ")";
	}
}
