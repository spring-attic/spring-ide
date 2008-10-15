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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Static;

/**
 * Validates a given {@link IBean}'s factory bean and factory method.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanFactoryRule extends AbstractBeanMethodValidationRule {

	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean).getBeanDefinition();
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean, context
				.getContextElement());

		// Validate a bean's factory bean and factory method
		String mergedClassName = mergedBd.getBeanClassName();
		if (mergedClassName != null && !SpringCoreUtils.hasPlaceHolder(mergedClassName)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(),
					mergedClassName);
			if (type != null) {

				// Validate factory bean and it's non-static factory method
				if (bd.getFactoryBeanName() != null) {
					if (bd.getFactoryMethodName() == null) {
						context.error(bean, "NO_FACTORY_METHOD",
								"A factory bean requires a factory method");
					}
					else {
						validateFactoryBean(bean, bd.getFactoryBeanName(), bd
								.getFactoryMethodName(), context);
					}
				}

				// Validate bean's static factory method with bean class from
				// merged bean definition - skip factory methods with
				// placeholders
				else {
					String methodName = bd.getFactoryMethodName();
					if (methodName != null && !SpringCoreUtils.hasPlaceHolder(methodName)) {

						// Use constructor argument values of root bean as
						// arguments for static factory method
						int argCount = (!bd.isAbstract()
								&& bd.getAutowireMode() != AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR ? bd
								.getConstructorArgumentValues().getArgumentCount()
								: -1);
						validateFactoryMethod(bean, mergedClassName, methodName, argCount,
								Static.DONT_CARE, context);
					}
				}
			}
		}
		else if (mergedClassName == null) {
			if (bd.getParentName() == null) {
				context.error(bean, "BEAN_WITHOUT_CLASS_OR_PARENT",
						"Factory method needs class from root " + "or parent bean");
			}
		}
	}

	protected void validateFactoryBean(IBean bean, String beanName, String methodName,
			IBeansValidationContext context) {
		if (beanName != null && !SpringCoreUtils.hasPlaceHolder(beanName)) {
			try {
				AbstractBeanDefinition factoryBd = (AbstractBeanDefinition) context
						.getCompleteRegistry().getBeanDefinition(beanName);
				// Skip validating factory beans which are created by another
				// factory bean
				if (factoryBd.getFactoryBeanName() == null) {
					if (factoryBd.isAbstract() || factoryBd.getBeanClassName() == null) {
						context.error(bean, "INVALID_FACTORY_BEAN", "Referenced factory bean '"
								+ beanName + "' is invalid (abstract or no bean class)");
					}
					else {

						// Validate non-static factory method in factory bean
						// Factory beans with factory methods can only be
						// validated during runtime - so skip them
						if (factoryBd.getFactoryMethodName() == null) {
							validateFactoryMethod(bean, factoryBd.getBeanClassName(), methodName,
									-1, Static.NO, context);
						}
					}
				}
			}
			catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					context.error(bean, "UNDEFINED_FACTORY_BEAN", "Factory bean '" + beanName
							+ "' not found");
				}
			}
		}
	}

	protected void validateFactoryMethod(IBean bean, String className, String methodName,
			int argCount, Static statics, IBeansValidationContext context) {
		if (className != null && !SpringCoreUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(),
					className);

			// Skip factory-method validation for factory beans which are
			// Spring factory beans as well and for those aspectOf methods
			if (type != null && !ValidationRuleUtils.ASPECT_OF_METHOD_NAME.equals(methodName)
					&& !Introspector.doesImplement(type, FactoryBean.class.getName())) {
				validateMethod(bean, type, MethodType.FACTORY, methodName, argCount, statics,
						context);
			}
		}
	}

}
