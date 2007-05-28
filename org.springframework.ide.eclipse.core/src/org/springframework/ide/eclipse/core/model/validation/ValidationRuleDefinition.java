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
package org.springframework.ide.eclipse.core.model.validation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Wraps a {@link IValidationRule} and all the information from it's definition
 * via the corresponding extension point.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ValidationRuleDefinition
		implements IValidationRule<IModelElement> {

	private static final String ENABLEMENT_PREFIX = "validator.rule.enable.";
	private static final String CLASS_ATTRIBUTE = "class";
	private static final String ID_ATTRIBUTE = "id";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";
	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private String validatorID;
	private IValidationRule<IModelElement> rule;
	private String id;
	private String name;
	private boolean isEnabled = true;
	private String description;

	public ValidationRuleDefinition(String validatorID,
			IConfigurationElement element) throws CoreException {
		this.validatorID = validatorID;
		init(element);
	}

	@SuppressWarnings("unchecked")
	private void init(IConfigurationElement element) throws CoreException {
		Object executable = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (executable instanceof IValidationRule) {
			rule = (IValidationRule<IModelElement>) executable;
		}
		id = element.getContributor().getName() + "."
				+ element.getAttribute(ID_ATTRIBUTE) + "-" + validatorID;
		name = element.getAttribute(NAME_ATTRIBUTE);
		description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		String enabledByDefault = element
				.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			isEnabled = Boolean.valueOf(enabledByDefault);
		}
	}

	public String getValidatorId() {
		return validatorID;
	}

	public IValidationRule<IModelElement> getRule() {
		return rule;
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

	/**
	 * Returns true if the wrapped {@link IValidationRule} is enabled.
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

	public boolean supports(IModelElement element) {
		return rule.supports(element);
	}

	public void validate(IModelElement element, IValidationContext context,
			IProgressMonitor monitor) {
		rule.validate(element, context, monitor);
	}

	@Override
	public String toString() {
		return id + " (" + rule.getClass().getName() + ")";
	}
}
