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

package org.springframework.ide.eclipse.web.flow.ui.editor.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.AddAndAssignSourceCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateAndAssignSourceCommand;

public class StateSourceEditPolicy extends ContainerEditPolicy {

    protected Command getAddCommand(GroupRequest request) {
        CompoundCommand cmd = new CompoundCommand();
        for (int i = 0; i < request.getEditParts().size(); i++) {
            if (getHost().getModel() instanceof ITransitionableFrom) {
                AddAndAssignSourceCommand add = new AddAndAssignSourceCommand();
                add.setParent((IWebFlowState) getHost().getParent().getModel());
                add.setSource((ITransitionableFrom) getHost().getModel());
                add.setChild(((ITransitionableTo) ((EditPart) request
                        .getEditParts().get(i)).getModel()));
                cmd.add(add);
            }
        }
        return cmd;
    }

    protected Command getCreateCommand(CreateRequest request) {
        CreateAndAssignSourceCommand cmd = new CreateAndAssignSourceCommand();
        cmd.setParent((IWebFlowState) getHost().getParent().getModel());
        cmd.setChild((ITransitionableTo) request.getNewObject());
        cmd.setSource((ITransitionableFrom) getHost().getModel());
        return cmd;
    }

    public EditPart getTargetEditPart(Request request) {
        if (REQ_CREATE.equals(request.getType()))
            return getHost();
        if (REQ_ADD.equals(request.getType()))
            return getHost();
        if (REQ_MOVE.equals(request.getType()))
            return getHost();
        return super.getTargetEditPart(request);
    }
}