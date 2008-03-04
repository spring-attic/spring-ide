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
package org.springframework.ide.eclipse.core.internal.model.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.ide.eclipse.core.PersistablePreferenceObjectSupport;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.util.StringUtils;

/**
 * Wraps a {@link IValidationRule} and all the information from it's definition
 * via the corresponding extension point.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidationRuleDefinition extends PersistablePreferenceObjectSupport {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ENABLEMENT_PREFIX = "validator.rule.enable.";

	private static final String PROPERTY_PREFIX = "validator.rule.property.";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String VALUE_ATTRIBUTE = "value";

	private String description;

	private String id;

	private String name;

	private IValidationRule rule;

	private String validatorId;

	private Map<String, String> propertyValues;

	private Map<String, String> originalPropertyValues;
	
	private Map<String, String> propertyDescriptions;

	public ValidationRuleDefinition(String validatorID, IConfigurationElement element)
			throws CoreException {
		this.validatorId = validatorID;
		init(element);
	}

	public ValidationRuleDefinition(String validatorId, String id, String name, String description) {
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
		if (propertyValues.size() > 0) {
 			BeanWrapper wrapper = new BeanWrapperImpl(rule);
			for (Map.Entry<String, String> entry : propertyValues.entrySet()) {
				try {
					wrapper.setPropertyValue(entry.getKey(), entry.getValue());
				}
				catch (BeansException e) {
					SpringCore.log(e);
				}
			}
		}
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
		id = element.getContributor().getName() + "." + element.getAttribute(ID_ATTRIBUTE) + "-"
				+ validatorId;
		name = element.getAttribute(NAME_ATTRIBUTE);
		description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		String enabledByDefault = element.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			setEnabledByDefault(Boolean.valueOf(enabledByDefault));
		}
		else {
			setEnabledByDefault(true);
		}

		// get configuration data
		propertyValues = new HashMap<String, String>();
		propertyDescriptions = new HashMap<String, String>();
		IConfigurationElement[] configurationDataElements = element.getChildren(PROPERTY_ELEMENT);
		for (IConfigurationElement configurationDataElement : configurationDataElements) {
			String propertyName = configurationDataElement.getAttribute(NAME_ATTRIBUTE);
			propertyValues.put(propertyName,
					configurationDataElement.getAttribute(VALUE_ATTRIBUTE));
			String desc = configurationDataElement.getAttribute(DESCRIPTION_ATTRIBUTE);
			if (StringUtils.hasText(desc)) {
				propertyDescriptions.put(propertyName, desc);
			}
		}
		originalPropertyValues = new HashMap<String, String>(propertyValues);
	}

	public Map<String, String> getPropertyValues() {
		return new HashMap<String, String>(propertyValues);
	}

	@Override
	public String toString() {
		return id + " (" + rule.getClass().getName() + ")";
	}

	@Override
	public boolean isEnabled(IProject project) {
		readSpecificConfiguration(project);
		return super.isEnabled(project);
	}

	protected void readSpecificConfiguration(IProject project) {
		if (project != null && hasProjectSpecificOptions(project)) {
			for (Map.Entry<String, String> entry : originalPropertyValues.entrySet()) {
				String value = SpringCorePreferences.getProjectPreferences(project).getString(
						PROPERTY_PREFIX + entry.getKey(), entry.getValue());
				propertyValues.put(entry.getKey(), value);
			}
		}
		else {
			for (Map.Entry<String, String> entry : originalPropertyValues.entrySet()) {
				String value = SpringCore.getDefault().getPluginPreferences().getString(
						PROPERTY_PREFIX + entry.getKey());
				if (StringUtils.hasText(value)) {
					propertyValues.put(entry.getKey(), value);
				}
			}
		}
	}

	public void setSpecificConfiguration(Map<String, String> newPropertyValues, IProject project) {
		if (project != null && hasProjectSpecificOptions(project)) {
			for (Map.Entry<String, String> entry : originalPropertyValues.entrySet()) {
				SpringCorePreferences.getProjectPreferences(project).putString(
						PROPERTY_PREFIX + entry.getKey(), entry.getValue());
			}
		}
		else {
			for (Map.Entry<String, String> entry : newPropertyValues.entrySet()) {
				SpringCore.getDefault().getPluginPreferences().setValue(
						PROPERTY_PREFIX + entry.getKey(), entry.getValue());
			}
		}
	}
	
	public String getPropertyDescription(String propertyName) {
		if (propertyDescriptions.containsKey(propertyName)) {
			return propertyDescriptions.get(propertyName);
		}
		return propertyName;
	}

}
