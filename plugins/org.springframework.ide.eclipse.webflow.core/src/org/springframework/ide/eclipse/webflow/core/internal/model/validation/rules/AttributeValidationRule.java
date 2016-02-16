/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.Attribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AttributeValidationRule implements
		IValidationRule<Attribute, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Attribute && context instanceof WebflowValidationContext;
	}

	public void validate(Attribute attribute, WebflowValidationContext context,
			IProgressMonitor monitor) {
		if (context.isVersion1()) {
			if (!StringUtils.hasText(attribute.getName())) {
				context.error(attribute, "NO_NAME_ATTRIBUTE",
						"Element 'attribute' requires 'name' attribute");
			}
			if (StringUtils.hasText(attribute.getType())
					&& WebflowValidationRuleUtils.getJavaType(attribute.getType(), context) == null) {
				context.error(attribute, "NO_TYPE_FOUND", MessageUtils.format(
						"Attribute 'type' \"{0}\" cannot be resolved", attribute.getType()));
			}
			if (!StringUtils.hasText(attribute.getValue())) {
				context.error(attribute, "NO_VALUE_ATTRIBUTE",
						"Element 'attribute' requires a 'value'");
			}
		}
		else {
			if (!StringUtils.hasText(attribute.getName())) {
				context.error(attribute, "NO_NAME_ATTRIBUTE",
				"Element 'attribute' requires 'name' attribute");
			}
			if (StringUtils.hasText(attribute.getType())
					&& WebflowValidationRuleUtils.getJavaType(attribute.getType(), context) == null) {
				context.error(attribute, "NO_TYPE_FOUND", MessageUtils.format(
						"Attribute 'type' \"{0}\" cannot be resolved", attribute.getType()));
			}
		}
	}
}
