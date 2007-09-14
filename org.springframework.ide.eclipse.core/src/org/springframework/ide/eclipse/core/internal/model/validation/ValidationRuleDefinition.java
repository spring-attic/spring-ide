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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.ide.eclipse.core.PersistablePreferenceObjectSupport;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Wraps a {@link IValidationRule} and all the information from it's definition
 * via the corresponding extension point.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidationRuleDefinition extends
		PersistablePreferenceObjectSupport {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ENABLEMENT_PREFIX = "validator.rule.enable.";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private String description;

	private String id;

	private String name;

	private IValidationRule rule;

	private String validatorId;

	public ValidationRuleDefinition(String validatorID,
			IConfigurationElement element) throws CoreException {
		this.validatorId = validatorID;
		init(element);
	}

	public ValidationRuleDefinition(String validatorId, String id, String name,
			String description) {
		this.validatorId = validatorId;
		this.id = id;
		this.name = name;
		this.description = description;
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

	@Override
	protected String getPreferenceId() {
		return ENABLEMENT_PREFIX + id;
	}

	public IValidationRule getRule() {
		return rule;
	}

	public String getValidatorId() {
		return validatorId;
	}

	private void init(IConfigurationElement element) throws CoreException {
		Object executable = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (executable instanceof IValidationRule) {
			rule = (IValidationRule) executable;
		}
		id = element.getContributor().getName() + "."
				+ element.getAttribute(ID_ATTRIBUTE) + "-" + validatorId;
		name = element.getAttribute(NAME_ATTRIBUTE);
		description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
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
	public String toString() {
		return id + " (" + rule.getClass().getName() + ")";
	}
}
