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
package org.springframework.ide.eclipse.beans.core.model.validation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Class that can be used as base class for {@link IValidationRule} implementation that <b>don't</b> support
 * infrastructure beans.
 * <p>
 * A infrastructure bean is defined by its role flag at {@link BeanDefinition#getRole()}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class AbstractNonInfrastructureBeanValidationRule {

	/**
	 * Returns <code>true</code> if this rule is able to validate the given {@link IModelElement} with the specified
	 * {@link IValidationContext}.
	 * <p>
	 * First resolves the parent {@link IBean} of the given element to check that the bean is not an infrastructure
	 * bean. If no parent bean can be resolved this implementation assumes that the rule supports the element.
	 * <p>
	 * Finally the {@link #supportsModelElementForNonInfrastructureBean} is called to give subclasses the option to do
	 * further checking.
	 * @param element the element to check
	 * @param context the validation context
	 * @return true if given element is supported by this rule implementation
	 * @see #supportsModelElementForNonInfrastructureBean(IModelElement, IBeansValidationContext)
	 */
	public final boolean supports(IModelElement element, IValidationContext context) {
		IBean bean = resolveBeanFromModelElement(element);
		return context instanceof IBeansValidationContext
				&& (bean == null || (bean != null && !bean.isInfrastructure()))
				&& supportsModelElementForNonInfrastructureBean(element, (IBeansValidationContext) context);
	}

	/**
	 * Recursively resolves the parent {@link IBean} instance from the given <code>element</code>.
	 */
	private IBean resolveBeanFromModelElement(IModelElement element) {
		if (element instanceof IBean) {
			return (IBean) element;
		}
		else if (element != null) {
			return resolveBeanFromModelElement(element.getElementParent());
		}
		else {
			return null;
		}
	}

	/**
	 * Returns <code>true</code> if this rule is able to validate the given {@link IModelElement} with the specified
	 * {@link IBeansValidationContext}.
	 * <p>
	 * This default implementation simply returns <code>true</code>. Subclasses can override this template method to
	 * implement specific checking.
	 */
	protected boolean supportsModelElementForNonInfrastructureBean(IModelElement element,
			IBeansValidationContext context) {
		return true;
	}
}
