/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.springiderules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.bestpractices.BestPracticesPluginConstants;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * This rule detects cases where a parent bean is declared and the specified
 * class is abstract. This is an error because child bean definitions that do
 * not specify a class will inherit the class from the parent bean definition.
 * If there is no concrete class that can be inherited by child bean
 * definitions, the class attribute of the parent bean should be omitted.
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class ParentBeanSpecifiesAbstractClassRule implements IValidationRule<IBean, IBeansValidationContext> {

	public static final String INFO_MESSAGE = "Parent beans should not specify abstract classes because they cannot be instantiated by child beans that inherit from this configuration.";

	public static final String ERROR_ID = "parentBeanSpecifiesAbstractClass";

	/**
	 * Returns <code>true</code> if this rule is able to validate the given
	 * {@link IModelElement} with the specified {@link IValidationContext}.
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBean;
	}

	public void validate(IBean bean, IBeansValidationContext validationContext, IProgressMonitor progressMonitor) {
		if (bean.isAbstract()) {

			IType implementationType = JdtUtils.getJavaType(validationContext.getRootElementProject(), bean
					.getClassName());
			if (implementationType != null) {
				int flags = 0;
				try {
					flags = implementationType.getFlags();
				}
				catch (JavaModelException e) {
					StatusHandler.log(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
							"Could not read JDT model flags", e));
					return;
				}

				if (Flags.isAbstract(flags)) {
					validationContext.info(bean, ERROR_ID, INFO_MESSAGE);
				}
			}
		}

	}
}