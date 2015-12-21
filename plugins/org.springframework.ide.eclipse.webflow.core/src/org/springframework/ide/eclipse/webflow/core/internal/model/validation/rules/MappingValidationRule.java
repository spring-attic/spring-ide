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
import org.springframework.ide.eclipse.webflow.core.internal.model.Mapping;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class MappingValidationRule implements
		IValidationRule<Mapping, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Mapping
				&& context instanceof WebflowValidationContext;
	}

	public void validate(Mapping mapping, WebflowValidationContext context,
			IProgressMonitor monitor) {

		if (!StringUtils.hasText(mapping.getSource())) {
			context
					.error(mapping, "NO_INPUT_ATTRIBUTE_ATTRIBUTE",
							"Element 'mapping' element requires 'input-attribute' attribute");
		}
		if (!StringUtils.hasText(mapping.getTarget())
				&& !StringUtils.hasText(mapping.getTargetCollection())) {
			context
					.error(
							mapping,
							"INVALID_USAGE_OF_TARGET_ATTRIBUTE",
							"Using 'target' and 'target-collection' attributes is not allowed on 'mapping' element");
		}
		if (StringUtils.hasText(mapping.getTo())
				&& WebflowValidationRuleUtils.getJavaType(mapping.getTo(),
						context) == null) {
			context.error(mapping, "NO_CLASS_FOUND", MessageUtils.format(
					"Class 'to' \"{0}\" cannot be resolved", mapping.getTo()));
		}
		if (StringUtils.hasText(mapping.getFrom())
				&& WebflowValidationRuleUtils.getJavaType(mapping.getFrom(),
						context) == null) {
			context.error(mapping, "NO_CLASS_FOUND", MessageUtils.format(
					"Class 'from' \"{0}\" cannot be resolved", mapping
							.getFrom()));
		}
	}
}
