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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Helper class that loads {@link ValidationRuleDefinition}s from the Platforms
 * extension point registry.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ValidationRuleDefinitionFactory {

	public static final String VALIDATORS_EXTENSION_POINT = SpringCore.PLUGIN_ID
			+ ".validators";
	public static final String RULES_ELEMENT = "rules";
	public static final String VALIDATOR_ID_ATTRIBUTE = "validatorId";

	public static Set<ValidationRuleDefinition> getRuleDefinitions(
			String validatorID) {
		Set<ValidationRuleDefinition> ruleDefinitions =
				new LinkedHashSet<ValidationRuleDefinition>();
		for (IExtension extension : Platform.getExtensionRegistry()
				.getExtensionPoint(VALIDATORS_EXTENSION_POINT)
						.getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (RULES_ELEMENT.equals(element.getName())) {
					String id = element.getAttribute(VALIDATOR_ID_ATTRIBUTE);
					if (validatorID.equals(id)) {
						for (IConfigurationElement ruleElement : element
								.getChildren(RULES_ELEMENT)) {
							try {
								ValidationRuleDefinition ruleDefinition =
										new ValidationRuleDefinition(
													validatorID, ruleElement);
								ruleDefinitions.add(ruleDefinition);
							}
							catch (CoreException e) {
								SpringCore.log(e);
							}
						}
					}
				}
			}
		}
		return ruleDefinitions;
	}
}
