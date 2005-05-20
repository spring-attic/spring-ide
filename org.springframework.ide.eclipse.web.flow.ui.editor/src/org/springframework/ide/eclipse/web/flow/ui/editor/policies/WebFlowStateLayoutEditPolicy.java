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

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.AddActionCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.AddAttributeMapperCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.AddIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateActionCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateAttributeMapperCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateStateCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.IfPart;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.StatePart;

public class WebFlowStateLayoutEditPolicy extends LayoutEditPolicy {

    protected Command createAddActionCommand(EditPart child) {
        IAction activity = (IAction) child.getModel();
        AddActionCommand add = new AddActionCommand();
        add.setParent((IActionState) getHost().getModel());
        add.setChild(activity);
        return add;
    }

    protected Command createAddAttributeMapperCommand(EditPart child) {
        IAttributeMapper activity = (IAttributeMapper) child.getModel();
        AddAttributeMapperCommand add = new AddAttributeMapperCommand();
        add.setParent((ISubFlowState) getHost().getModel());
        add.setChild(activity);
        return add;
    }

    protected Command createAddIfCommand(EditPart child) {
        IIf activity = (IIf) child.getModel();
        AddIfCommand add = new AddIfCommand();
        add.setParent((IDecisionState) getHost().getModel());
        add.setChild(activity);
        return add;
    }

    protected EditPolicy createChildEditPolicy(EditPart child) {
        if (child instanceof StatePart || child instanceof IfPart)
            return new StateSelectionEditPolicy();
        return new NonResizableEditPolicy();
    }

    protected Command createMoveChildCommand(EditPart child, EditPart after) {
        return null;
    }

    protected Command getAddCommand(Request req) {
        ChangeBoundsRequest request = (ChangeBoundsRequest) req;
        List editParts = request.getEditParts();
        CompoundCommand command = new CompoundCommand();
        for (int i = 0; i < editParts.size(); i++) {
            EditPart child = (EditPart) editParts.get(i);
            if (child.getModel() instanceof IAction
                    && getHost().getModel() instanceof IActionState) {
                command.add(createAddActionCommand(child));
            }
            else if (child.getModel() instanceof IAttributeMapper
                    && getHost().getModel() instanceof ISubFlowState) {
                command.add(createAddAttributeMapperCommand(child));
            }
            else if (child.getModel() instanceof IIf
                    && getHost().getModel() instanceof IDecisionState) {
                command.add(createAddIfCommand(child));
            }
        }
        return command.unwrap();
    }

    protected Command getCreateCommand(CreateRequest request) {
        if (getHost().getModel() instanceof IWebFlowState
                && request.getNewObject() instanceof IState) {
            CreateStateCommand command = new CreateStateCommand();
            command.setParent((IWebFlowState) getHost().getModel());
            command.setChild((IState) request.getNewObject());
            return command;
        }
        else if (getHost().getModel() instanceof IActionState
                && request.getNewObject() instanceof IAction) {
            CreateActionCommand command = new CreateActionCommand();
            command.setParent((IActionState) getHost().getModel());
            command.setChild((IAction) request.getNewObject());
            return command;
        }
        else if (getHost().getModel() instanceof ISubFlowState
                && request.getNewObject() instanceof IAttributeMapper) {
            if (((ISubFlowState) getHost().getModel()).getAttributeMapper() == null) {
                CreateAttributeMapperCommand command = new CreateAttributeMapperCommand();
                command.setParent((ISubFlowState) getHost().getModel());
                command.setChild((IAttributeMapper) request.getNewObject());
                return command;
            }
            else {
                return null;
            }
        }
        else if (getHost().getModel() instanceof IDecisionState
                && request.getNewObject() instanceof IIf) {
            CreateIfCommand command = new CreateIfCommand();
            command.setParent((IDecisionState) getHost().getModel());
            command.setChild((IIf) request.getNewObject());
            return command;
        }
        else {
            return null;
        }
    }

    protected Command getDeleteDependantCommand(Request request) {
        return null;
    }

    protected Command getMoveChildrenCommand(Request request) {
        return null;
    }

}