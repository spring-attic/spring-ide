/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		// if (model instanceof ISubFlowState)
		// part = new SubFlowStatePart();
		// else
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