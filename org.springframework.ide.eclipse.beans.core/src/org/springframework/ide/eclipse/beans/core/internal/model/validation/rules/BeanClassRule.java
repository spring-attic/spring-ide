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
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBean}'s bean class. Skips child beans and bean
 * class names with placeholders.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanClassRule implements
		IValidationRule<IBean, BeansValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof IBean
				&& context instanceof BeansValidationContext);
	}

	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		String className = ((Bean) bean).getBeanDefinition().getBeanClassName();

		// Validate bean class and constructor arguments - skip child beans and
		// class names with placeholders
		if (className != null && !ValidationRuleUtils
				.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils
					.getProject(bean).getProject(), className);
			if (type == null) {
				context.error(bean, "CLASS_NOT_FOUND", "Class '" + className
						+ "' not found");
			}
		}
	}
}

