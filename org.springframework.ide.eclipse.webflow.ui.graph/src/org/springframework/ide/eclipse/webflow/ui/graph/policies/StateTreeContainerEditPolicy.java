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
package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateAttributeMapperCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreatePropertyCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteActionPropertyCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteAttributeMapperCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteStatePropertyCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ReorderIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ReorderStateCommand;

/**
 * 
 */
public class StateTreeContainerEditPolicy extends TreeContainerEditPolicy {

    /**
     * 
     * 
     * @param index 
     * @param child 
     * 
     * @return 
     */
    protected Command createCreateActionCommand(IActionElement child, int index) {
        CreateActionCommand cmd = new CreateActionCommand();
        cmd.setParent((IActionState) getHost().getModel());
        cmd.setChild(child);
        cmd.setMove(true);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }

    /**
     * 
     * 
     * @param index 
     * @param child 
     * 
     * @return 
     */
    protected Command createCreateAttributeMapperCommand(
            IAttributeMapper child, int index) {
        CreateAttributeMapperCommand cmd = new CreateAttributeMapperCommand();
        cmd.setParent((ISubflowState) getHost().getModel());
        return cmd;
    }

    /**
     * 
     * 
     * @param index 
     * @param child 
     * 
     * @return 
     */
    protected Command createCreateIfCommand(IIf child, int index) {
        CreateIfCommand cmd = new CreateIfCommand();
        cmd.setParent((IDecisionState) getHost().getModel());
        cmd.setChild(child);
        cmd.setMove(true);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }
    
    /**
     * 
     * 
     * @param index 
     * @param child 
     * 
     * @return 
     */
    protected Command createCreatePropertyCommand(IAttribute child, int index) {
        CreatePropertyCommand cmd = new CreatePropertyCommand();
        cmd.setParent((IWebflowModelElement) getHost().getModel());
        cmd.setChild(child);
        if (index >= 0)
            cmd.setIndex(index);
        return cmd;
    }

    /**
     * 
     * 
     * @param child 
     * 
     * @return 
     */
    protected Command createDeleteActionCommand(IActionElement child) {
        DeleteActionCommand cmd = new DeleteActionCommand();
        cmd.setChild(child);
        return cmd;
    }

    /**
     * 
     * 
     * @param child 
     * 
     * @return 
     */
    protected Command createDeleteAttributeMapperCommand(IAttributeMapper child) {
        DeleteAttributeMapperCommand cmd = new DeleteAttributeMapperCommand();
        cmd.setParent((ISubflowState) child.getElementParent());
        cmd.setChild(child);
        return cmd;
    }

    /**
     * 
     * 
     * @param child 
     * 
     * @return 
     */
    protected Command createDeleteIfCommand(IIf child) {
        DeleteIfCommand cmd = new DeleteIfCommand();
        cmd.setParent((IDecisionState) child.getElementParent());
        cmd.setChild(child);
        return cmd;
    }

       
    /**
     * 
     * 
     * @param child 
     * 
     * @return 
     */
    protected Command createDeletePropertyCommand(IAttribute child) {
        if (child.getElementParent() instanceof IState) {
            DeleteStatePropertyCommand cmd = new DeleteStatePropertyCommand();
            cmd.setParent((IState) child.getElementParent());
            cmd.setChild(child);
            return cmd;
        }
        else if (child.getElementParent() instanceof IActionElement) {
            DeleteActionPropertyCommand cmd = new DeleteActionPropertyCommand();
            cmd.setParent((IActionElement) child.getElementParent());
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
    /* (non-Javadoc)
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
                if (child.getModel() instanceof IActionElement
                        && getHost().getModel() instanceof IActionState) {
                    IActionElement childModel = (IActionElement) child.getModel();
                    command.add(createDeleteActionCommand(childModel));
                    command.add(createCreateActionCommand(childModel, index));//$NON-NLS-1$
                }
                else if (child.getModel() instanceof IAttributeMapper
                        && getHost().getModel() instanceof ISubflowState) {
                    IAttributeMapper childModel = (IAttributeMapper) child
                            .getModel();
                    command.add(createDeleteAttributeMapperCommand(childModel));
                    command.add(createCreateAttributeMapperCommand(childModel,
                            index)); //$NON-NLS-1$
                }
                else if (child.getModel() instanceof IAttribute
                        && (getHost().getModel() instanceof IActionElement || getHost()
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
                else if ((child.getModel() instanceof IInputMapper || child.getModel() instanceof IOutputMapper)
                        && getHost().getModel() instanceof IAttributeMapper) {
                    //command.add(createDeleteInputOutputCommand(child.getModel()));
                    //command.add(createCreateInputOutputCommand(child.getModel()));
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
    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    protected Command getCreateCommand(CreateRequest request) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getMoveChildrenCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
     */
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
                        (IWebflowState) getHost().getModel(), oldIndex,
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

    /**
     * 
     * 
     * @param target 
     * @param source 
     * 
     * @return 
     */
    protected boolean isAncestor(EditPart source, EditPart target) {
        if (source == target)
            return true;
        if (target.getParent() != null)
            return isAncestor(source, target.getParent());
        return false;
    }

}
