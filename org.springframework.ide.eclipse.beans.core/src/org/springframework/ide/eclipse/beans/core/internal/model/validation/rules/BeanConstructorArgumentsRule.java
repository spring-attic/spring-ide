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
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;

/**
 * Validates a given {@link IBean}'s constructor arguments. Skips abstract
 * beans.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanConstructorArgumentsRule extends AbstractBeanValidationRule {

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof Bean && !((Bean) element).isAbstract());
	}

	@Override
	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		BeanDefinition bd = ((Bean) bean).getBeanDefinition();
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		// Validate merged constructor arguments in bean's class (child beans
		// not supported)
		String className = bd.getBeanClassName();
		if (className != null
				&& !ValidationRuleUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean)
					.getProject(), className);
			if (type != null) {
				validateConstructorArguments(bean, type, mergedBd
						.getConstructorArgumentValues(), context);
			}
		}

		// If any constructor argument defined in bean the validate the merged
		// constructor arguments in merged bean's class (child beans fully
		// supported)
		if (!bd.getConstructorArgumentValues().isEmpty()) {
			String mergedClassName = mergedBd.getBeanClassName();
			if (mergedClassName != null
					&& !ValidationRuleUtils.hasPlaceHolder(mergedClassName)) {
				IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(
						bean).getProject(), mergedClassName);
				if (type != null) {
					validateConstructorArguments(bean, type, mergedBd
							.getConstructorArgumentValues(), context);
				}
			}
		}
	}

	protected void validateConstructorArguments(IBean bean, IType type,
			ConstructorArgumentValues argumentValues,
			IValidationContext context) {

		// Skip validation if auto-wiring or a factory are involved
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		if (bd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO
				&& bd.getFactoryBeanName() == null
				&& bd.getFactoryMethodName() == null) {

			// Check for default constructor if no constructor arguments are
			// available
			int numArguments = (argumentValues == null ? 0 : argumentValues
					.getArgumentCount());
			try {
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element = BeansModelUtils
							.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}
					context.error(bean, "NO_CONSTRUCTOR",
							"No constructor with "
									+ numArguments
									+ (numArguments == 1 ? " argument"
											: " arguments")
									+ " defined in class '"
									+ type.getFullyQualifiedName() + "'");
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}
}
