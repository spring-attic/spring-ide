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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;

/**
 * Validates a given {@link IBean}'s bean definition.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanDefinitionRule extends AbstractBeanValidationRule {

	@Override
	public void validate(IBean bean, IBeansValidationContext context,
			IProgressMonitor monitor) {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		try {
			bd.validate();
		} catch (BeanDefinitionValidationException e) {
			context.error(bean, "INVALID_BEAN_DEFINITION",
					"Invalid bean definition: " + e.getMessage());
		}
	}
}

