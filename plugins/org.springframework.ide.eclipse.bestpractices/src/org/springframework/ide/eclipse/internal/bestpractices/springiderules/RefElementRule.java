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

import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractXmlValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IXmlValidationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This rule checks for usage of ref elements and suggests that a ref attribute
 * of the property element be used instead.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class RefElementRule extends AbstractXmlValidationRule {

	public static final String INFO_MESSAGE = "Consider using the ref attribute of the property element for readability. E.g. <property=\"foo\" ref=\"myBeanId\">";

	public static final String ERROR_ID = "refElementRule";

	@Override
	protected boolean supports(Node node) {
		return node instanceof Element && BeanDefinitionParserDelegate.REF_ELEMENT.equals(node.getNodeName())
				&& BeanDefinitionParserDelegate.PROPERTY_ELEMENT.equals(node.getParentNode().getNodeName());
	}

	@Override
	protected void validate(Node node, IXmlValidationContext context) {
		context.info(node, ERROR_ID, INFO_MESSAGE);
	}

}
