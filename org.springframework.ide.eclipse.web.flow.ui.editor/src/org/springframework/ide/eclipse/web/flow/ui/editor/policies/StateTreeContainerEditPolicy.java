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
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttribute;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IInputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IOutputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateActionCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateAttributeMapperCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreateInputOutputCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.CreatePropertyCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteActionCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteActionPropertyCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteAttributeMapperCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteInputOutputCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.DeleteStatePropertyCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.ReorderIfCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.ReorderStateCommand;

public class StateTreeContainerEditPolicy extends TreeContainerEditPolicy {

    protected Command createCreateActionCommand(IAction child, int index) {
        CreateActionCommand cmd = new CreateActionCommand();
        cmd.setParent((IActionState) getHost().getModel());
        cmd.setChild(child);
        cmd.setMove(true);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }

    protected Command createCreateAttributeMapperCommand(
            IAttributeMapper child, int index) {
        CreateAttributeMapperCommand cmd = new CreateAttributeMapperCommand();
        cmd.setParent((ISubFlowState) getHost().getModel());
        cmd.setChild(child);
        return cmd;
    }

    protected Command createCreateIfCommand(IIf child, int index) {
        CreateIfCommand cmd = new CreateIfCommand();
        cmd.setParent((IDecisionState) getHost().getModel());
        cmd.setChild(child);
        cmd.setMove(true);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }
    
    protected Command createCreateInputOutputCommand(Object child) {
        CreateInputOutputCommand cmd = new CreateInputOutputCommand();
        cmd.setParent((IAttributeMapper) getHost().getModel());
        cmd.setChild(child);
        cmd.setMove(true);
        
        return cmd;
    }

    protected Command createCreatePropertyCommand(IAttribute child, int index) {
        CreatePropertyCommand cmd = new CreatePropertyCommand();
        cmd.setParent((IWebFlowModelElement) getHost().getModel());
        cmd.setChild(child);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }

    protected Command createDeleteActionCommand(IAction child) {
        DeleteActionCommand cmd = new DeleteActionCommand();
        cmd.setParent((IActionState) child.getElementParent());
        cmd.setChild(child);
        return cmd;
    }

    protected Command createDeleteAttributeMapperCommand(IAttributeMapper child) {
        DeleteAttributeMapperCommand cmd = new DeleteAttributeMapperCommand();
        cmd.setParent((ISubFlowState) child.getElementParent());
        cmd.setChild(child);
        return cmd;
    }

    protected Command createDeleteIfCommand(IIf child) {
        DeleteIfCommand cmd = new DeleteIfCommand();
        cmd.setParent((IDecisionState) child.getElementParent());
        cmd.setChild(child);
        return cmd;
    }

    protected Command createDeleteInputOutputCommand(Object child) {
        DeleteInputOutputCommand cmd = new DeleteInputOutputCommand();
        cmd.setParent((IAttributeMapper) ((IWebFlowModelElement) child).getElementParent());
        cmd.setChild(child);
        return cmd;
    }
    
    protected Command createDeletePropertyCommand(IAttribute child) {
        if (child.getElementParent() instanceof IState) {
            DeleteStatePropertyCommand cmd = new DeleteStatePropertyCommand();
            cmd.setParent((IState) child.getElementParent());
            cmd.setChild(child);
            return cmd;
        }
        else if (child.getElementParent() instanceof IAction) {
            DeleteActionPropertyCommand cmd = new DeleteActionPropertyCommand();
            cmd.setParent((IAction) child.getElementParent());
            cmd.setChild(child);
            return cmd;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
     */
    protected Command getAddCommand(ChangeBoundsRequest request) {
        CompoundCommand command = new CompoundCommand();
        List editparts = request.getEditParts();
        int index = findIndexOfTreeItemAt(request.getLocation());

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = (EditPart) editparts.get(i);
            if (isAncestor(child, getHost()))
                command.add(UnexecutableCommand.INSTANCE);
            else {
                if (child.getModel() instanceof IAction
                        && getHost().getModel() instanceof IActionState) {
                    IAction childModel = (IAction) child.getModel();
                    command.add(createDeleteActionCommand(childModel));
                    command.add(createCreateActionCommand(childModel, index));//$NON-NLS-1$
                }
                else if (child.getModel() instanceof IAttributeMapper
                        && getHost().getModel() instanceof ISubFlowState) {
                    IAttributeMapper childModel = (IAttributeMapper) child
                            .getModel();
                    command.add(createDeleteAttributeMapperCommand(childModel));
                    command.add(createCreateAttributeMapperCommand(childModel,
                            index)); //$NON-NLS-1$
                }
                else if (child.getModel() instanceof IAttribute
                        && (getHost().getModel() instanceof IAction || getHost()
                                .getModel() instanceof IState)) {
                    IAttribute childModel = (IAttribute) child.getModel();
                    command.add(createDeletePropertyCommand(childModel));
                    command.add(createCreatePropertyCommand(childModel, index));
                }
                else if (child.getModel() instanceof IIf
                        && getHost().getModel() instanceof IDecisionState) {
                    IIf childModel = (IIf) child.getModel();
                    command.add(createDeleteIfCommand(childModel));
                    command.add(createCreateIfCommand(childModel, index));
                }
                else if ((child.getModel() instanceof IInputMapping || child.getModel() instanceof IOutputMapping)
                        && getHost().getModel() instanceof IAttributeMapper) {
                    command.add(createDeleteInputOutputCommand(child.getModel()));
                    command.add(createCreateInputOutputCommand(child.getModel()));
                }
            }
        }
        return command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    protected Command getCreateCommand(CreateRequest request) {
        return null;
    }

    protected Command getMoveChildrenCommand(ChangeBoundsRequest request) {
        CompoundCommand command = new CompoundCommand();
        List editparts = request.getEditParts();
        List children = getHost().getChildren();
        int newIndex = findIndexOfTreeItemAt(request.getLocation());

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = (EditPart) editparts.get(i);
            int tempIndex = newIndex;
            int oldIndex = children.indexOf(child);
            if (oldIndex == tempIndex || oldIndex + 1 == tempIndex) {
                command.add(UnexecutableCommand.INSTANCE);
                return command;
            }
            else if (oldIndex < tempIndex) {
                tempIndex--;
            }
            if (child.getModel() instanceof IState && tempIndex >= 0) {
                command.add(new ReorderStateCommand((IState) child.getModel(),
                        (IWebFlowState) getHost().getModel(), oldIndex,
                        tempIndex));
            }
            else if (child.getModel() instanceof IIf && tempIndex >= 0) {
                command.add(new ReorderIfCommand((IIf) child.getModel(),
                        (IDecisionState) getHost().getModel(), oldIndex,
                        tempIndex));
            }
        }
        return command;
    }

    protected boolean isAncestor(EditPart source, EditPart target) {
        if (source == target)
            return true;
        if (target.getParent() != null)
            return isAncestor(source, target.getParent());
        return false;
    }

}