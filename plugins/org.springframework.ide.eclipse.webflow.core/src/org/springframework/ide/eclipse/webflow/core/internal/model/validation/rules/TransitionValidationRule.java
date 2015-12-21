/*******************************************************************************
 * Copyright (c) 2007 - 2012 Spring IDE Developers
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
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class TransitionValidationRule implements
		IValidationRule<StateTransition, WebflowValidationContext> {

	private static final String EXPRESSION_PREFIX = "${";
	
	private static final String SPEL_EXPRESSION_PREFIX = "#{";

	private static final String EXPRESSION_SUFFIX = "}";

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof StateTransition && context instanceof WebflowValidationContext;
	}

	public void validate(StateTransition state, WebflowValidationContext context,
			IProgressMonitor monitor) {
		if (context.isVersion1()) {
			if (!StringUtils.hasText(state.getToStateId())) {
				context.error(state, "NO_TO_ATTRIBUTE",
						"Element 'transition' requires 'to' attribute");
			}
			if (state.getToState() == null
					&& state.getToStateId() != null
					&& !((state.getToStateId().startsWith(EXPRESSION_PREFIX) || 
							state.getToStateId().startsWith(SPEL_EXPRESSION_PREFIX)) && 
							state.getToStateId().endsWith(EXPRESSION_SUFFIX))) {
				context.error(state, "NO_VALID_TO_ATTRIBUTE", MessageUtils.format(
						"Element 'transition' references a non-exiting state \"{0}\"", state
								.getToStateId()));
			}
		}
		else {
			if (state.getToState() == null
					&& state.getToStateId() != null
					&& !((state.getToStateId().startsWith(EXPRESSION_PREFIX) || 
							state.getToStateId().startsWith(SPEL_EXPRESSION_PREFIX)) && 
							state.getToStateId().endsWith(EXPRESSION_SUFFIX))) {
				if (context
						.getStateFromParentFlow(state.getToStateId(), context.getWebflowConfig()) == null) {
					context.error(state, "NO_VALID_TO_ATTRIBUTE", MessageUtils.format(
							"Element 'transition' references a non-exiting state \"{0}\"", state
									.getToStateId()));
				}
			}
		}
	}
}
