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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansValueHolder;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBean}'s or {@link IBeansValueHolder}'s bean
 * reference(s).
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanReferenceRule implements
		IValidationRule<IBeansModelElement, BeansValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof Bean || element instanceof IBeansValueHolder
				|| element instanceof IBeansList
				|| element instanceof IBeansSet
				|| element instanceof IBeansMap);
	}

	public void validate(IBeansModelElement element,
			BeansValidationContext context, IProgressMonitor monitor) {
		if (element instanceof Bean) {
			validateBean((Bean) element, context);
		} else if (element instanceof IBeansValueHolder) {
			IBeansValueHolder holder = (IBeansValueHolder) element;
			validateValue(holder, holder.getValue(), context);
		} else if (element instanceof IBeansList) {
			IBeansList list = (IBeansList) element;
			for (Object entry : list.getList()) {
				validateValue(list, entry, context);
			}
		} else if (element instanceof IBeansSet) {
			IBeansSet set = (IBeansSet) element;
			for (Object entry : set.getSet()) {
				validateValue(set, entry, context);
			}
		} else if (element instanceof IBeansMap) {
			IBeansMap map = (IBeansMap) element;
			for (Object entry : map.getMap().values()) {
				validateValue(map, entry, context);
			}
		}
	}

	private void validateBean(Bean bean, BeansValidationContext context) {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) bean
				.getBeanDefinition();

		// Validate depends-on beans
		if (bd.getDependsOn() != null) {
			for (String beanName : bd.getDependsOn()) {
				validateDependsOnBean(bean, beanName, context);
			}
		}
	}

	private void validateDependsOnBean(IBean bean, String beanName,
			BeansValidationContext context) {
		if (beanName != null && !ValidationRuleUtils.hasPlaceHolder(beanName)) {
			try {
				AbstractBeanDefinition dependsBd = (AbstractBeanDefinition)
						context.getCompleteRegistry()
								.getBeanDefinition(beanName);
				if (dependsBd.isAbstract()
						|| (dependsBd.getBeanClassName() == null && dependsBd
								.getFactoryBeanName() == null)) {
					context.error(bean, "INVALID_DEPENDS_ON_BEAN",
							"Referenced depends-on bean '" + beanName
							+ "' is invalid (abstract or no bean class and no "
							+ "factory bean)");
				}
			} catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					context.error(bean, "UNDEFINED_DEPENDS_ON_BEAN",
							"Depends-on bean '" + beanName + "' not found");
				}
			}
		}
	}

	private void validateValue(IBeansModelElement element, Object value,
			BeansValidationContext context) {
		String beanName = null;
		if (value instanceof RuntimeBeanReference) {
			beanName = ((RuntimeBeanReference) value).getBeanName();
		}
		else if (value instanceof BeanReference) {
			beanName = ((BeanReference) value).getBeanName();
		}
		if (beanName != null) {
			try {
				AbstractBeanDefinition refBd = (AbstractBeanDefinition) context
						.getCompleteRegistry().getBeanDefinition(beanName);
				if (refBd.isAbstract() || (refBd.getBeanClassName() == null
						&& refBd.getFactoryBeanName() == null)) {
					context.error(element, "INVALID_REFERENCED_BEAN",
							"Referenced bean '" + beanName + "' is invalid "
									+ "(abstract or no bean class and "
									+ "no factory bean)");
				}
			}
			catch (NoSuchBeanDefinitionException e) {

				// Display a warning if the bean ref contains a placeholder
				if (ValidationRuleUtils.hasPlaceHolder(beanName)) {
					context.warning(element, "UNDEFINED_REFERENCED_BEAN",
							"Referenced bean '" + beanName + "' not found");
				}
				// Handle factory bean references
				else if (ValidationRuleUtils.isFactoryBeanReference(beanName)) {
					String tempBeanName = beanName.replaceFirst(
							ValidationRuleUtils.FACTORY_BEAN_REFERENCE_REGEXP,
							"");
					try {
						BeanDefinition def = context.getCompleteRegistry()
								.getBeanDefinition(tempBeanName);
						String beanClassName = ((AbstractBeanDefinition) def)
								.getBeanClassName();
						if (beanClassName != null) {
							IType type = JdtUtils.getJavaType(BeansModelUtils
									.getProject(element).getProject(),
									beanClassName);
							if (type != null) {
								if (!Introspector.doesImplement(type,
										FactoryBean.class.getName())) {
									context.error(element,
											"INVALID_FACTORY_BEAN",
											"Referenced factory bean '"
												+ tempBeanName
												+ "' does not implement the "
												+ "interface 'FactoryBean'");
								}
							}
							else {
								context.warning(element,
										"INVALID_REFERENCED_BEAN",
										"Referenced factory bean '"
										+ tempBeanName
										+ "' implementation class not found");
							}
						}
					}
					catch (NoSuchBeanDefinitionException be) {
						context.warning(element, "UNDEFINED_FACTORY_BEAN",
								"Referenced factory bean '" + tempBeanName
										+ "' not found");
					}
				}
				else {
					context.warning(element, "UNDEFINED_REFERENCED_BEAN",
							"Referenced bean '" + beanName + "' not found");
				}
			}
		}
	}
}
