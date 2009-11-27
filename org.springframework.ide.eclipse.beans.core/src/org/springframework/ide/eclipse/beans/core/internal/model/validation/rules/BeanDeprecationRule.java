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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Validates a given {@link IBean}'s bean class for deprecation.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class BeanDeprecationRule extends AbstractBeanValidationRule {

	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
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
	}
}
