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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ActionStateValidationRule implements
		IValidationRule<ActionState, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof ActionState
				&& context instanceof WebflowValidationContext;
	}

	public void validate(ActionState state, WebflowValidationContext context,
			IProgressMonitor monitor) {
		if (state.getActions().size() == 0) {
			context.error(state, "NO_ACTIONS",
					"Element 'action-state' requires action sub elements");
		}
	}

}
