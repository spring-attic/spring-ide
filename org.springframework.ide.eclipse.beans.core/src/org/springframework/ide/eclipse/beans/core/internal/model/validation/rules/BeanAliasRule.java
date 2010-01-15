/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractNonInfrastructureBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBeanAlias}'s alias and associated bean name.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanAliasRule extends AbstractNonInfrastructureBeanValidationRule implements
		IValidationRule<IBeanAlias, IBeansValidationContext> {

	@Override
	protected boolean supportsModelElementForNonInfrastructureBean(IModelElement element,
			IBeansValidationContext context) {
		return element instanceof IBeanAlias;
	}

	public void validate(IBeanAlias alias, IBeansValidationContext context, IProgressMonitor monitor) {
		IBeansConfigSet configSet = (context.getContextElement() instanceof IBeansConfigSet ? (IBeansConfigSet) context
				.getContextElement() : null);
		// Validate bean overriding
		if (context.getIncompleteRegistry().containsBeanDefinition(alias.getElementName())) {
			if (configSet == null || BeansModelUtils.getConfig(alias).getBean(alias.getElementName()) != null) {
				context.error(alias, "BEAN_OVERRIDE", "Overrides another bean in the same config file");
			}
			else if (!configSet.isAllowBeanDefinitionOverriding()) {
				context.error(alias, "BEAN_OVERRIDE", "Overrides another bean in config set '"
						+ configSet.getElementName() + "'");
			}
		}

		// Validate alias overriding within config
		for (IBeanAlias al : BeansModelUtils.getConfig(alias).getAliases()) {
			if (al == alias) {
				continue;
			}
			else if (al.getElementName().equals(alias.getElementName())) {
				context.error(alias, "ALIAS_OVERRIDE", "Overrides another alias in the same config file");
				break;
			}
		}

		// Validate alias within config set
		if (configSet != null) {

			// Validate alias overriding
			if (!configSet.isAllowAliasOverriding()) {
				for (IBeansConfig config : configSet.getConfigs()) {
					if (config == BeansModelUtils.getConfig(alias)) {
						break;
					}
					if (config.getAlias(alias.getElementName()) != null) {
						context.error(alias, "ALIAS_OVERRIDE", "Overrides another alias in config set '"
								+ configSet.getElementName() + "'");
						break;
					}
				}
			}

			// Check if corresponding bean exists
			if (!configSet.isIncomplete() && !context.getCompleteRegistry().containsBeanDefinition(alias.getBeanName())) {
				context.warning(alias, "UNDEFINED_REFERENCED_BEAN", "Referenced bean '" + alias.getBeanName()
						+ "' not found in config set '" + configSet.getElementName() + "'");
			}
		}
	}
}
