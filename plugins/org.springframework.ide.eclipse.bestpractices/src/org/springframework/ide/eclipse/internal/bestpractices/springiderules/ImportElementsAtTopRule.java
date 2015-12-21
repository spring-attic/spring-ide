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
import org.w3c.dom.Node;

/**
 * This rule checks for <import> elements that appear before bean definitions.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ImportElementsAtTopRule extends AbstractXmlValidationRule {

	public static final String INFO_MESSAGE = "Imports should appear before bean definitions.";

	public static final String ERROR_ID = "importElementsAtTop";

	@Override
	protected boolean supports(Node node) {
		return node.getNodeName().equals("import");
	}

	@Override
	protected void validate(Node node, IXmlValidationContext context) {
		Node currNode = node.getPreviousSibling();
		while (currNode != null) {
			if (currNode.getNodeName().equalsIgnoreCase(BeanDefinitionParserDelegate.BEAN_ELEMENT)) {
				context.info(node, ERROR_ID, INFO_MESSAGE);
				return;
			}
			currNode = currNode.getPreviousSibling();
		}
	}

}
