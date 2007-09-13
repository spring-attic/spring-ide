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
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Base class for validating a given {@link IBean}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractBeanValidationRule implements
		IValidationRule<IBean, BeansValidationContext> {

	public final boolean supports(IModelElement element,
			IValidationContext context) {
		return context instanceof BeansValidationContext
				&& element instanceof Bean
				&& supportsBean((IBean) element,
						(BeansValidationContext) context);
	}

	/**
	 * Template method to be overridden by subclasses
	 * @param element the element to check if it is supported by this rule
	 * @return true if rule supports given element
	 * @since 2.0.2
	 */
	protected boolean supportsBean(IBean bean, BeansValidationContext context) {
		return true;
	}

	public abstract void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor);
}
