/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractNonInfrastructureBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBean}'s bean class for deprecation.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class BeanDeprecationRule extends AbstractNonInfrastructureBeanValidationRule implements
		IValidationRule<IBeansModelElement, IBeansValidationContext> {

	@Override
	protected boolean supportsModelElementForNonInfrastructureBean(IModelElement element,
			IBeansValidationContext context) {
		return element instanceof IBean || element instanceof IBeanProperty;
	}

	public void validate(IBeansModelElement element, IBeansValidationContext context, IProgressMonitor monitor) {
		if (element instanceof IBean) {
			validateBean((IBean) element, context, monitor);
		}
		else if (element instanceof IBeanProperty) {
			validatePropery((IBeanProperty) element, context, monitor);
		}
	}

	private void validatePropery(IBeanProperty property, IBeansValidationContext context, IProgressMonitor monitor) {
		IBean bean = BeansModelUtils.getParentOfClass(property, IBean.class);
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		String mergedClassName = mergedBd.getBeanClassName();
		IType type = ValidationRuleUtils.extractBeanClass(mergedBd, bean, mergedClassName, context);
		if (type != null) {
			validateMethod(property, type, property.getElementName(), true, context);
		}
	}

	private void validateBean(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
		String className = ((Bean) bean).getBeanDefinition().getBeanClassName();

		// Check if bean class is marked deprecated
		if (className != null && !SpringCoreUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), className);
			try {
				if (type != null && Flags.isDeprecated(type.getFlags())) {
					context.warning(bean, "CLASS_IS_DEPRECATED", "Class '" + className + "' is marked deprecated");
				}
			}
			catch (JavaModelException e) {
			}
		}

		// Check if init and destroy methods are deprecated
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean).getBeanDefinition();
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		String mergedClassName = mergedBd.getBeanClassName();
		IType type = ValidationRuleUtils.extractBeanClass(mergedBd, bean, mergedClassName, context);
		if (type != null) {
			validateMethod(bean, type, bd.getInitMethodName(), false, context);
			validateMethod(bean, type, bd.getDestroyMethodName(), false, context);
		}
	}

	private void validateMethod(IResourceModelElement bean, IType type, String methodName, boolean setter,
			IBeansValidationContext context) {
		if (methodName != null && !SpringCoreUtils.hasPlaceHolder(methodName)) {
			try {
				IMethod method = null;
				if (setter) {
					method = Introspector.getWritableProperty(type, methodName);
				}
				else {
					method = Introspector.findMethod(type, methodName, 0, Public.DONT_CARE, Static.DONT_CARE);
				}
				if (method != null && Flags.isDeprecated(method.getFlags())) {
					context.warning(bean, "METHOD_IS_DEPRECATED", "Method '" + method.getElementName()
							+ "' is marked deprecated");
				}
			}
			catch (JavaModelException e) {
			}
		}
	}
}
