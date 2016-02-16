/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.internal.model.InputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class InputAttributeValidationRule implements
		IValidationRule<InputAttribute, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof InputAttribute
				&& context instanceof WebflowValidationContext;
	}

	public void validate(InputAttribute attribute,
			WebflowValidationContext context, IProgressMonitor monitor) {
		if (!StringUtils.hasText(attribute.getName())) {
			context.error(attribute, "NO_NAME_ATTRIBUTE",
					"Element 'input-attribute' requires 'name' attribute");
		}
		if (StringUtils.hasText(attribute.getScope())
				&& !WebflowValidationRuleUtils.SCOPE_TYPES.contains(attribute
						.getScope())) {
			context.error(attribute, "INVALID_SCOPE", MessageUtils.format(
					"Invalid scope \"{0}\" specified", attribute.getScope()));
		}
	}
}
