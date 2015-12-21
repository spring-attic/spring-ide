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
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SubflowStateValidationRule implements
		IValidationRule<SubflowState, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof SubflowState && context instanceof WebflowValidationContext;
	}

	public void validate(SubflowState state, WebflowValidationContext context,
			IProgressMonitor monitor) {

		if (!StringUtils.hasText(state.getFlow())) {
			if (context.isVersion1()) {
				context.error(state, "NO_FLOW_ATTRIBUTE",
						"Element 'subflow-state' requires unique '"
								+ (context.isVersion1() ? "flow" : "subflow") + "' attribute");
			}
			else {
				IState parentState = context.getStateFromParentState(state.getAttribute(state
						.getNode(), "parent"));
				if (parentState == null
						|| (parentState instanceof ISubflowState && !StringUtils
								.hasText(((ISubflowState) parentState).getFlow()))) {
					context.error(state, "NO_FLOW_ATTRIBUTE",
							"Element 'subflow-state' requires unique '"
									+ (context.isVersion1() ? "flow" : "subflow") + "' attribute");
				}
			}
		}
		else if (!WebflowModelUtils.getWebflowConfigNames(context.getWebflowConfig().getProject())
				.contains(state.getFlow())
				&& !WebflowModelUtils.getWebflowConfigNames(
						WebflowModelUtils.getWebflowState(state, true)).contains(state.getFlow())) {
			context.error(state, "FLOW_REFERENCE_INVALID", MessageUtils.format(
					"Referenced flow \"{0}\" cannot be found", state.getFlow()));
		}
	}
}
