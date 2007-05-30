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

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class TransitionValidationRule implements
		IValidationRule<StateTransition, WebflowValidationContext> {

	private static final String EXPRESSION_PREFIX = "${";

	private static final String EXPRESSION_SUFFIX = "}";

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof StateTransition
				&& context instanceof WebflowValidationContext;
	}

	public void validate(StateTransition state,
			WebflowValidationContext context, IProgressMonitor monitor) {

		if (!StringUtils.hasText(state.getToStateId())) {
			context.error(this, "NO_TO_ATTRIBUTE", state,
					"Element 'transition' requires 'to' attribute");
		}
		else if (state.getToState() == null
				&& (!(state.getToStateId().startsWith(EXPRESSION_PREFIX) && state
						.getToStateId().endsWith(EXPRESSION_SUFFIX)))) {
			context.error(this, "NO_VALID_TO_ATTRIBUTE", state,
				MessageUtils.format("Element 'transition' references a non-exiting state \"{0}\"", 
					state.getToStateId()));
		}
	}
}
