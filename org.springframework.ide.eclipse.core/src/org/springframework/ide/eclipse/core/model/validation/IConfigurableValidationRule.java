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
package org.springframework.ide.eclipse.core.model.validation;

import java.util.Properties;

import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Extension to {@link IValidationRule} that allows to be configured by external
 * configuration data from the
 * <code>org.springframework.ide.eclipse.core.validators</code> extension
 * point.
 * @author Christian Dupuis
 * @since 2.0.4
 */
public interface IConfigurableValidationRule<E extends IModelElement, C extends IValidationContext>
		extends IValidationRule<E, C> {

	/**
	 * Configures the {@link IValidationRule} instance with the given key value
	 * pairs in the {@link Properties} instance.
	 * @param configurationData the configuration data taken from the extension
	 * point contribution
	 */
	void configure(Properties configurationData);

}
