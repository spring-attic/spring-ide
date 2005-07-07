/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IIfTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

public class StatePartFactory implements EditPartFactory {

    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        //if (model instanceof ISubFlowState)
        //    part = new SubFlowStatePart();
        //else 
        if (model instanceof IWebFlowState && !(model instanceof ISubFlowState))
            part = new WebFlowPart();
        else if (model instanceof IActionState)
            part = new ActionStatePart();
        else if (model instanceof IDecisionState)
            part = new DecisionStatePart();
        else if (model instanceof IState)
            part = new StatePart();
        else if (model instanceof IIfTransition)
            part = new IfTransitionPart();
        else if (model instanceof IStateTransition)
            part = new StateTransitionPart();
        else if (model instanceof IAction)
            part = new StatePart();
        else if (model instanceof IAttributeMapper)
            part = new StatePart();
        else if (model instanceof IIf)
            part = new IfPart();
        part.setModel(model);
        return part;
    }
}