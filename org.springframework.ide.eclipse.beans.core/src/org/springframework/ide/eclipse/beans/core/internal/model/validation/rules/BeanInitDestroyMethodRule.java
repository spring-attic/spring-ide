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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Static;

/**
 * Validates a given {@link IBean}'s init and destroy method. Skips factory
 * beans.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanInitDestroyMethodRule extends
		AbstractBeanMethodValidationRule {

	@Override
	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		AbstractBeanDefinition bd = (AbstractBeanDefinition)
				((Bean) bean).getBeanDefinition();
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition)
				BeansModelUtils.getMergedBeanDefinition(bean, context
						.getContextElement());

		// For non-factory beans validate it's init-method and destroy-method
		String mergedClassName = mergedBd.getBeanClassName();
		if (mergedClassName != null
				&& !ValidationRuleUtils.hasPlaceHolder(mergedClassName)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean)
					.getProject(), mergedClassName);
			if (type != null) {

				// For non-factory beans validate bean's init-method and
				// destroy-method
				if (!Introspector.doesImplement(type, FactoryBean.class
						.getName())) {
					if (mergedBd.isEnforceInitMethod()) {
						validateMethod(bean, type,
								MethodType.INIT, bd.getInitMethodName(), 0,
								Static.DONT_CARE, context);
					}
					if (mergedBd.isEnforceDestroyMethod()) {
						validateMethod(bean, type,
								MethodType.DESTROY, bd.getDestroyMethodName(),
								0, Static.DONT_CARE, context);
					}
				}
			}
		}
	}
}
