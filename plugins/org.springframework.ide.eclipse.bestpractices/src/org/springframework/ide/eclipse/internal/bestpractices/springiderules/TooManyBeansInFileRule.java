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

import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractXmlValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.validation.IXmlValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This rule checks for cases where there are too many bean definitions in a
 * file and recommends that the definitions be decomposed into multiple files.
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class TooManyBeansInFileRule extends AbstractXmlValidationRule implements
		IValidationRule<IBeansModelElement, IBeansValidationContext> {

	public static final String INFO_MESSAGE = "There are too many beans defined in this file. Consider decomposing into multiple bean configuration files.";

	public static final String ERROR_ID = "tooManyBeandInFile";

	// Report a warning when there are more than this many beans.
	private static final int DEFAULT_MAX_BEAN_COUNT = 40;

	private int maxBeanCount = DEFAULT_MAX_BEAN_COUNT;

	public void setMaxBeanCount(int maxBeanCount) {
		this.maxBeanCount = maxBeanCount;
	}

	@Override
	protected boolean supports(Node node) {
		return node instanceof Document;
	}

	@Override
	protected void validate(Node node, IXmlValidationContext context) {

		NodeList beansNodeList = ((Document) node).getElementsByTagName("beans");
		if (beansNodeList.getLength() < 1) {
			return;
		}

		Element beansElement = (Element) beansNodeList.item(0);
		NodeList beanNodeList = beansElement.getElementsByTagName("bean");

		if (beanNodeList.getLength() > maxBeanCount) {
			context.info(node, ERROR_ID, INFO_MESSAGE);
		}
	}

}
