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
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
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

	private BeanDefinitionRegistry incompleteRegistry;
	private BeanDefinitionRegistry completeRegistry;

	public BeansValidationContext(IBeansConfig config,
			IResourceModelElement contextElement) {
		super(config, contextElement);

		// Add parsing errors to list of validation errors
		addProblems(((BeansConfig) config).getProblems());
		
		incompleteRegistry = createRegistry(config, contextElement, false);
		completeRegistry = createRegistry(config, contextElement, true);
	}

	public BeanDefinitionRegistry getIncompleteRegistry() {
		return incompleteRegistry;
	}

	public BeanDefinitionRegistry getCompleteRegistry() {
		return completeRegistry;
	}

	private BeanDefinitionRegistry createRegistry(IBeansConfig config,
			IResourceModelElement contextElement, boolean fillCompletely) {
		DefaultBeanDefinitionRegistry registry =
				new DefaultBeanDefinitionRegistry();
		if (contextElement instanceof BeansConfigSet) {
			IBeansConfigSet configSet = (IBeansConfigSet) contextElement;
			if (fillCompletely) {
				registry.setAllowAliasOverriding(true);
				registry.setAllowBeanDefinitionOverriding(true);
			}
			else {
				registry.setAllowAliasOverriding(configSet
						.isAllowAliasOverriding());
				registry.setAllowBeanDefinitionOverriding(configSet
						.isAllowBeanDefinitionOverriding());
			}
			for (IBeansConfig csConfig : configSet.getConfigs()) {
				if (!fillCompletely && config.equals(csConfig)) {
					break;
				}
				BeansModelUtils.register(csConfig, registry);
			}
		}
		return registry;
	}
}
