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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddAndAssignSourceCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateAndAssignSourceCommand;

/**
 * 
 */
public class StateSourceEditPolicy extends ContainerEditPolicy {

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.GroupRequest)
     */
    protected Command getAddCommand(GroupRequest request) {
        CompoundCommand cmd = new CompoundCommand();
        for (int i = 0; i < request.getEditParts().size(); i++) {
            if (getHost().getModel() instanceof ITransitionableFrom) {
                AddAndAssignSourceCommand add = new AddAndAssignSourceCommand();
                add.setParent((IWebflowState) getHost().getParent().getModel());
                add.setSource((ITransitionableFrom) getHost().getModel());
                add.setChild(((ITransitionableTo) ((EditPart) request
                        .getEditParts().get(i)).getModel()));
                cmd.add(add);
            }
        }
        return cmd;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    protected Command getCreateCommand(CreateRequest request) {
        CreateAndAssignSourceCommand cmd = new CreateAndAssignSourceCommand();
        cmd.setParent((IWebflowState) getHost().getParent().getModel());
        cmd.setChild((ITransitionableTo) request.getNewObject());
        cmd.setSource((ITransitionableFrom) getHost().getModel());
        return cmd;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getTargetEditPart(org.eclipse.gef.Request)
     */
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
