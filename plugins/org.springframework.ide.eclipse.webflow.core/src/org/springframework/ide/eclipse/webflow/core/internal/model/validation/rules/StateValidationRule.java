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
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class StateValidationRule implements
		IValidationRule<IState, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof IState && !(element instanceof WebflowState))
				&& context instanceof WebflowValidationContext;
	}

	public void validate(IState state, WebflowValidationContext context,
			IProgressMonitor monitor) {
		
		if (!StringUtils.hasText(state.getId())) {
			context.error(state, "NO_ID",
					"Element requires unique 'id' attribute");
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			context.error(state, "NO_UNIQUE_ID_GIVEN", MessageUtils.format(
					"Specified state id \"{0}\" is not unique", state.getId()));
		}
	}
}
