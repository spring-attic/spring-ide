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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;

/**
 * Validates a given root {@link IBean}'s name and aliases.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanDefinitionHolderRule extends AbstractBeanValidationRule {

	@Override
	protected boolean supportsBean(IBean bean, IBeansValidationContext context) {
		return !bean.isInnerBean();
	}

	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {

		// only validate bean override for non-infrastructure beans
		if (bean instanceof Bean && !bean.isInfrastructure()) {
			validateBeanNameAndAlias((Bean) bean, context);
		}
	}

	/**
	 * Validates if the given {@link BeanDefinition} nested in the passed {@link IBean} can be
	 * registered in the <code>context</code>.
	 * <p>
	 * The implementation relies on the fact that a {@link BeanDefinitionRegistry} throws a
	 * {@link BeanDefinitionStoreException} if the bean name is already chosen.
	 */
	private void validateBeanNameAndAlias(Bean bean, IBeansValidationContext context) {
		try {
			context.getIncompleteRegistry().registerBeanDefinition(bean.getElementName(),
					bean.getBeanDefinition());
		}
		catch (BeanDefinitionStoreException e) {
			if (context.getContextElement() instanceof IBeansConfigSet) {
				IBeansConfigSet configSet = (IBeansConfigSet) context.getContextElement();
				context.error(bean, "BEAN_OVERRIDE", "Overrides another bean named '"
						+ bean.getElementName() + "' in config set '" + configSet.getElementName()
						+ "'");
			}
			else if (context.getContextElement() instanceof IBeansConfig) {
				for (IBean existingBean : ((IBeansConfig) context.getContextElement()).getBeans()) {
					if (existingBean != bean
							&& existingBean.getElementName().equals(bean.getElementName())) {
						if (existingBean.getElementResource().equals(bean.getElementResource())) {
							context.error(bean, "BEAN_OVERRIDE", "Overrides another bean named '"
									+ bean.getElementName() + "' in the same config file");
						}
					}
				}
			}
			else {
				context.error(bean, "BEAN_OVERRIDE", "Overrides another bean named '"
						+ bean.getElementName() + "' in the same config file");
			}
		}

		// Validate bean aliases
		if (bean.getAliases() != null) {
			for (String alias : bean.getAliases()) {
				try {
					context.getIncompleteRegistry().registerAlias(bean.getElementName(), alias);
				}
				catch (BeanDefinitionStoreException e) {
					context.error(bean, "INVALID_ALIAS", e.getMessage());
				}
			}
		}
	}
}
