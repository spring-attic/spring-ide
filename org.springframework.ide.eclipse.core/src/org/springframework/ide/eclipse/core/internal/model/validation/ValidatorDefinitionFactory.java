/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model.validation;

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
 * Helper class that loads {@link ValidatorDefinition}s from the Platforms extension point registry.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidatorDefinitionFactory {

	public static final String VALIDATORS_EXTENSION_POINT = SpringCore.PLUGIN_ID + ".validators";

	public static final String VALIDATOR_ELEMENT = "validator";

	/**
	 * Returns all contributed {@link ValidatorDefinition}.
	 */
	public static List<ValidatorDefinition> getValidatorDefinitions() {
		List<ValidatorDefinition> validatorDefinitions = new ArrayList<ValidatorDefinition>();
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(VALIDATORS_EXTENSION_POINT)
				.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if (VALIDATOR_ELEMENT.equals(element.getName())) {
					try {
						ValidatorDefinition validatorDefinition = new ValidatorDefinition(element);
						validatorDefinitions.add(validatorDefinition);
					}
					catch (CoreException e) {
						SpringCore.log(e);
					}
				}
			}
		}
		Collections.sort(validatorDefinitions, new Comparator<ValidatorDefinition>() {

			public int compare(ValidatorDefinition v1, ValidatorDefinition v2) {
				if (v1.getOrder() == v2.getOrder()) {
					return v1.getName().compareTo(v2.getName());
				}
				else {
					return Integer.valueOf(v1.getOrder()).compareTo(Integer.valueOf(v2.getOrder()));
				}
			}
		});
		return validatorDefinitions;
	}

	/**
	 * Returns a specific {@link ValidatorDefinition} or null if the requested one can't be found.
	 * @param validatorId the id of the desired {@link ValidatorDefinition}
	 */
	public static ValidatorDefinition getValidatorDefinition(String validatorId) {
		for (ValidatorDefinition validator : getValidatorDefinitions()) {
			if (validator.getID().equals(validatorId)) {
				return validator;
			}
		}
		return null;
	}

}
