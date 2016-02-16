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
import org.springframework.ide.eclipse.quickfix.validator.BeanReferenceValidator;
import org.springframework.ide.eclipse.quickfix.validator.ClassAttributeValidator;
import org.springframework.ide.eclipse.quickfix.validator.ConstructorArgNameValidator;
import org.springframework.ide.eclipse.quickfix.validator.FactoryBeanValidator;
import org.springframework.ide.eclipse.quickfix.validator.FactoryMethodValidator;
import org.springframework.ide.eclipse.quickfix.validator.InitDestroyMethodValidator;
import org.springframework.ide.eclipse.quickfix.validator.NamespaceElementsValidator;
import org.springframework.ide.eclipse.quickfix.validator.PropertyValidator;


/**
 * Support class for getting the right validator for an attribute of a bean
 * configuration
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class AttributeQuickfixSupport extends QuickfixSupport {

	@Override
	protected void init() {
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_CLASS,
				new ClassAttributeValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_DESTROY_METHOD,
				new InitDestroyMethodValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_FACTORY_METHOD,
				new FactoryMethodValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_INIT_METHOD,
				new InitDestroyMethodValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_FACTORY_BEAN,
				new FactoryBeanValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_DEPENDS_ON,
				new BeanReferenceValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_BEAN, BeansSchemaConstants.ATTR_PARENT,
				new BeanReferenceValidator());

		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_PROPERTY, BeansSchemaConstants.ATTR_NAME,
				new PropertyValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_PROPERTY, BeansSchemaConstants.ATTR_REF,
				new BeanReferenceValidator());

		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG, BeansSchemaConstants.ATTR_REF,
				new BeanReferenceValidator());
		setAttributeValidator(BEAN_NAMESPACE, BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG,
				BeansSchemaConstants.ATTR_NAME, new ConstructorArgNameValidator());

		// TODO: uncomment when BeanAliasRule works properly
		// setAttributeValidator(BEAN_NAMESPACE,
		// BeansSchemaConstants.ELEM_ALIAS, BeansSchemaConstants.ATTR_ALIAS,
		// new BeanAliasValidator());

		setAttributeValidator("!" + BEAN_NAMESPACE, null, null, new NamespaceElementsValidator());
	}
}
