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
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * This rule detects usage of <code>DriverManagerDataSource</code> which is
 * discouraged in most cases because it does not support database connection
 * pooling.
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class AvoidDriverManagerDataSource implements IValidationRule<IBean, IBeansValidationContext> {

	public static final String INFO_MESSAGE = "Use of DriverManagerDataSource is discouraged in most cases because it does not pool connections. See the Javadoc for more information.";

	private static final String DRIVER_MANAGER_DATASOURCE_CLASS = "org.springframework.jdbc.datasource.DriverManagerDataSource";

	public static final String ERROR_ID = "avoidDriverManagerDataSource";

	/**
	 * Returns <code>true</code> if this rule is able to validate the given
	 * {@link IModelElement} with the specified {@link IValidationContext}.
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBean && ((IBean) element).getClassName() != null;
	}

	public void validate(IBean bean, IBeansValidationContext validationContext, IProgressMonitor progressMonitor) {
		if (DRIVER_MANAGER_DATASOURCE_CLASS.equals(bean.getClassName())) {
			validationContext.info(bean, ERROR_ID, INFO_MESSAGE);
		}
	}
}
