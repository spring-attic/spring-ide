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

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.ISetup;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.SetAsStartStateAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteActionCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteAttributeMapperCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteInputOutputCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteSetupCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteStatePropertyCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.EditPropertiesCommand;

public class StateEditPolicy extends ComponentEditPolicy {

    protected Command createDeleteCommand(GroupRequest deleteRequest) {
        if (getHost().getModel() instanceof IProperty
                && getHost().getParent().getModel() instanceof IPropertyEnabled) {
            IPropertyEnabled parent = (IPropertyEnabled) (getHost().getParent().getModel());
            DeleteStatePropertyCommand deleteCmd = new DeleteStatePropertyCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((IProperty) (getHost().getModel()));
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof ISubFlowState) {
            ISubFlowState parent = (ISubFlowState) (getHost().getParent()
                    .getModel());
            DeleteAttributeMapperCommand deleteCmd = new DeleteAttributeMapperCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((IAttributeMapper) (getHost().getModel()));
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof IAttributeMapper) {
            IAttributeMapper parent = (IAttributeMapper) (getHost().getParent()
                    .getModel());
            DeleteInputOutputCommand deleteCmd = new DeleteInputOutputCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild(getHost().getModel());
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof IViewState) {
            IViewState parent = (IViewState) (getHost().getParent()
                    .getModel());
            DeleteSetupCommand deleteCmd = new DeleteSetupCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((ISetup) getHost().getModel());
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof IWebFlowState) {
            IWebFlowState parent = (IWebFlowState) (getHost().getParent()
                    .getModel());
            DeleteCommand deleteCmd = new DeleteCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((IState) (getHost().getModel()));
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof IActionState) {
            IActionState parent = (IActionState) (getHost().getParent()
                    .getModel());
            DeleteActionCommand deleteCmd = new DeleteActionCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((IAction) (getHost().getModel()));
            return deleteCmd;
        }
        else if (getHost().getParent().getModel() instanceof IDecisionState) {
            IDecisionState parent = (IDecisionState) (getHost().getParent()
                    .getModel());
            DeleteIfCommand deleteCmd = new DeleteIfCommand();
            deleteCmd.setParent(parent);
            deleteCmd.setChild((IIf) (getHost().getModel()));
            return deleteCmd;
        }
        else
            return null;
    }

    public Command getCommand(Request request) {
        if (SetAsStartStateAction.STARTSTATE_REQUEST.equals(request.getType()))
            return getSetAsStartStateCommand();
        else if (EditPropertiesAction.EDITPROPERTIES_REQUEST.equals(request
                .getType())) {
            return getEditPropertiesCommand();
        }
        return super.getCommand(request);
    }

    protected Command getSetAsStartStateCommand() {
        SetAsStartStateCommand command = new SetAsStartStateCommand();
        command.setChild((IState) getHost().getModel());
        return command;
    }

    protected Command getEditPropertiesCommand() {
        EditPropertiesCommand command = new EditPropertiesCommand();
        command.setChild((ICloneableModelElement) getHost().getModel());
        return command;
    }

    static class SetAsStartStateCommand extends
            org.eclipse.gef.commands.Command {

        IState child = null;

        IState oldChild = null;

        int index = -1;

        public SetAsStartStateCommand() {
            super("Set as start state");
        }

        public void setChild(IState child) {
            this.child = child;
        }

        public void execute() {
            if (child.getElementParent() instanceof IWebFlowState) {
                IWebFlowState state = (IWebFlowState) child.getElementParent();
                oldChild = state.getStartState();
                index = state.getStates().indexOf(child);
                state.setStartState(child);
            }
        }

        public void undo() {
            if (child.getElementParent() instanceof IWebFlowState) {
                IWebFlowState state = (IWebFlowState) child.getElementParent();
                state.setStartState(oldChild);
                state.moveState(child, index);
            }
        }

        public void redo() {
            execute();
        }
    }

}