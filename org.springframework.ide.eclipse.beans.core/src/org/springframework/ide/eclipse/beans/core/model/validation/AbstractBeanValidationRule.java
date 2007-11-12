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
package org.springframework.ide.eclipse.beans.core.model.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Base class for validating a given {@link IBean}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractBeanValidationRule extends
		AbstractNonInfrastructureBeanValidationRule implements
		IValidationRule<IBean, IBeansValidationContext> {

	@Override
	protected final boolean supportsModelElementForNonInfrastructureBean(
			IModelElement element, IBeansValidationContext context) {
		return element instanceof Bean
				&& supportsBean((IBean) element, context);
	}

	/**
	 * Template method to be overridden by subclasses
	 * @param element the element to check if it is supported by this rule
	 * @return true if rule supports given element
	 * @since 2.0.2
	 */
	protected boolean supportsBean(IBean bean, IBeansValidationContext context) {
		return true;
	}

	/**
	 * Execute the concrete validation logic of the given {@link IBean} under
	 * the given {@link IBeansValidationContext}.
	 */
	public abstract void validate(IBean bean, IBeansValidationContext context,
			IProgressMonitor monitor);
}
