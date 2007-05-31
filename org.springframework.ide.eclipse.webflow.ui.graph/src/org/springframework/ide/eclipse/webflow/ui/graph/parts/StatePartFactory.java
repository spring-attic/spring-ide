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
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class StatePartFactory implements EditPartFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof IWebflowState
				&& !(model instanceof ISubflowState || model instanceof IInlineFlowState))
			part = new FlowPart();
		else if (model instanceof IActionState)
			part = new ActionStatePart();
		else if (model instanceof IViewState)
			part = new ActionStatePart();
		else if (model instanceof ISubflowState)
			part = new ActionStatePart();
		else if (model instanceof IDecisionState)
			part = new DecisionStatePart();
		else if (model instanceof IInlineFlowState)
			part = new InlineFlowStatePart();
		else if (model instanceof IState)
			part = new ActionStatePart();
		else if (model instanceof IIfTransition)
			part = new IfTransitionPart();
		else if (model instanceof IStateTransition)
			part = new StateTransitionPart();
		else if (model instanceof IActionElement)
			part = new StatePart();
		else if (model instanceof IExceptionHandler)
			part = new StatePart();
		else if (model instanceof IAttributeMapper)
			part = new StatePart();
		else if (model instanceof IIf)
			part = new IfPart();
		else
			part = null;
		part.setModel(model);
		return part;
	}
}
