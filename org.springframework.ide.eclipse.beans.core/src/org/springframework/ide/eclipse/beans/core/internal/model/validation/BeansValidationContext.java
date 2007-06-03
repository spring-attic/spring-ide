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
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating all
 * relevant information used during validation.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansValidationContext extends AbstractValidationContext {

	private IBeansConfig config;
	private BeanDefinitionRegistry registry;

	public BeansValidationContext(IBeansConfig config,
			IResourceModelElement rootElement) {
		super(rootElement);
		this.config = config;

		// Add parsing errors to list of validation errors
		addProblems(((BeansConfig) config).getProblems()); 
	}

	public IBeansConfig getConfig() {
		return config;
	}

	public BeanDefinitionRegistry getRegistry() {
		return registry;
	}
}
