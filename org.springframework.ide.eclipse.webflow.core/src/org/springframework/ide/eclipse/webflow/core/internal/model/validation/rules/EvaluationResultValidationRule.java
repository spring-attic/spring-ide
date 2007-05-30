/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import java.util.ArrayList;
import java.util.List;

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
 * @since 2.0
 */
public class EvaluationResultValidationRule implements
		IValidationRule<EvaluationResult, WebflowValidationContext> {

	private static final List<String> SCOPE_TYPES;

	static {
		SCOPE_TYPES = new ArrayList<String>();
		SCOPE_TYPES.add("request");
		SCOPE_TYPES.add("flash");
		SCOPE_TYPES.add("flow");
		SCOPE_TYPES.add("conversation");
	}

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof EvaluationResult
				&& context instanceof WebflowValidationContext;
	}

	public void validate(EvaluationResult result,
			WebflowValidationContext context, IProgressMonitor monitor) {

		if (!StringUtils.hasText(result.getName())) {
			context.error(this, "NO_ATTRIBUTE_ATTRIBUTE", result,
					"Element  'evaluate-result' requires 'name' attribute");
		}
		if (StringUtils.hasText(result.getScope())
				&& !SCOPE_TYPES.contains(result.getScope())) {
			context.error(this, "NO_VALUE_ATTRIBUTE", result, MessageUtils
					.format("Element 'set' requires 'value' attribute", result
							.getScope()));
		}
	}
}
