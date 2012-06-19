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
package org.springframework.ide.eclipse.quickfix;

import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.validator.BeanValidator;
import org.springframework.ide.eclipse.quickfix.validator.ClassAttributeValidator;
import org.springframework.ide.eclipse.quickfix.validator.NamespaceElementsValidator;
import org.springframework.ide.eclipse.quickfix.validator.PropertyValidator;


/**
 * Support class for getting the right validator for a node of a bean
 * configuration
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class NodeQuickfixSupport extends QuickfixSupport {

	@Override
	protected void init() {
		setValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, new ClassAttributeValidator());
		setValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG, new BeanValidator());
		setValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_PROPERTY, new PropertyValidator());
		setValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_ALIAS, new BeanValidator());

		setValidator("!" + BEAN_NAMESPACE, null, new NamespaceElementsValidator());
	}

}
