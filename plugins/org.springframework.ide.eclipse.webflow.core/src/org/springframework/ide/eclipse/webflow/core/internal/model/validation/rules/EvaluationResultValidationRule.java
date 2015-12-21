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
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluationResult;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class EvaluationResultValidationRule implements
		IValidationRule<EvaluationResult, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof EvaluationResult
				&& context instanceof WebflowValidationContext;
	}

	public void validate(EvaluationResult result,
			WebflowValidationContext context, IProgressMonitor monitor) {

		if (!StringUtils.hasText(result.getName())) {
			context.error(result, "NO_ATTRIBUTE_ATTRIBUTE",
					"Element  'evaluate-result' requires 'name' attribute");
		}
		if (StringUtils.hasText(result.getScope())
				&& !WebflowValidationRuleUtils.SCOPE_TYPES.contains(result.getScope())) {
			context.error(result, "NO_VALUE_ATTRIBUTE", MessageUtils
					.format("Element 'set' requires 'value' attribute", result
							.getScope()));
		}
	}
}
