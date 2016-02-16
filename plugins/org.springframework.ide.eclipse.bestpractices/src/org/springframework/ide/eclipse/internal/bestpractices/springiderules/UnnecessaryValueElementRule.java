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
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractXmlValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.validation.IXmlValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

/**
 * This rule checks for short attribute values that are expressed using a value
 * element rather than a value attribute.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UnnecessaryValueElementRule extends AbstractXmlValidationRule implements
		IValidationRule<IBeansModelElement, IBeansValidationContext> {

	// Values with fewer characters than this constant are considered short
	// and will result in a warning if expressed in a value element
	private static final int DEFAULT_MIN_VALUE_LENGTH = 40;

	public static final String ERROR_ID = "unnecessaryValueElement";

	private int minValueLength = DEFAULT_MIN_VALUE_LENGTH;

	public void setMinValueLength(int minValueLength) {
		this.minValueLength = minValueLength;
	}

	@Override
	protected boolean supports(Node node) {
		return node.getNodeName().equals(BeanDefinitionParserDelegate.VALUE_ELEMENT)
				&& node.getParentNode().getNodeName().equals(BeanDefinitionParserDelegate.PROPERTY_ELEMENT);
	}

	@Override
	protected void validate(Node node, IXmlValidationContext context) {
		if (node.getFirstChild() != null && !(node.getFirstChild() instanceof CDATASection)
				&& node.getFirstChild().getNodeValue().length() < minValueLength) {

			context.info(node, ERROR_ID, "Consider using a value=\"" + node.getFirstChild().getNodeValue().trim()
					+ "\" attribute for short literals instead of a value element");

		}
	}

}
