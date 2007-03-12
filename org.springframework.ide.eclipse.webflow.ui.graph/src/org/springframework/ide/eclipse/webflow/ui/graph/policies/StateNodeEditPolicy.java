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

package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.IfTransitionCreateCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ReconnectIfTransitionTargetCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ReconnectSourceCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ReconnectTargetCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.StateTransitionCreateCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart;

/**
 * 
 */
public class StateNodeEditPolicy extends GraphicalNodeEditPolicy {

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    protected Command getConnectionCompleteCommand(
            CreateConnectionRequest request) {
        if (request.getNewObject() instanceof IStateTransition) {
            if (getHost().getModel() instanceof ITransitionableTo) {
                StateTransitionCreateCommand cmd = (StateTransitionCreateCommand) request
                        .getStartCommand();
                cmd.setTarget((ITransitionableTo) getState());
                return cmd;
            }
        }
        else if (request.getNewObject() instanceof IIfTransition) {
            if (getHost().getModel() instanceof ITransitionableTo) {
                IfTransitionCreateCommand cmd = (IfTransitionCreateCommand) request
                        .getStartCommand();
                cmd.setTarget((ITransitionableTo) getState());
                return cmd;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        if (request.getNewObject() instanceof IStateTransition) {
            if (getHost().getModel() instanceof IEndState
                    || getHost().getModel() instanceof IActionElement
                    || getHost().getModel() instanceof IAttributeMapper
                    || getHost().getModel() instanceof IDecisionState
                    || getHost().getModel() instanceof IIf)
                return null;
            StateTransitionCreateCommand cmd = new StateTransitionCreateCommand();
            cmd.setSource((ITransitionableFrom) getHost().getModel());
            request.setStartCommand(cmd);
            return cmd;
        }
        else if (request.getNewObject() instanceof IIfTransition) {
            if (!(getHost().getModel() instanceof IIf))
                return null;
            if (((IIf) getHost().getModel()).getElseTransition() != null)
                return null;
            IfTransitionCreateCommand cmd = new IfTransitionCreateCommand();
            cmd.setSource((IIf) getHost().getModel());
            request.setStartCommand(cmd);
            return cmd;
        }
        return null;
    }

    /**
     * 
     * 
     * @return 
     */
    protected AbstractStatePart getStatePart() {
        return (AbstractStatePart) getHost();
    }

    /**
     * 
     * 
     * @return 
     */
    protected IState getState() {
        return (IState) getHost().getModel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        if (request.getConnectionEditPart().getModel() instanceof IStateTransition
                && getHost().getModel() instanceof ITransitionableFrom) {
            ReconnectSourceCommand cmd = new ReconnectSourceCommand();
            cmd.setTransition((IStateTransition) request
                    .getConnectionEditPart().getModel());
            cmd.setSource((ITransitionableFrom) getState());
            return cmd;
        }
        else if (request.getConnectionEditPart().getModel() instanceof IIfTransition
                && getHost().getModel() instanceof IIf) {
            /*
             * ReconnectIfTransitionSourceCommand cmd = new
             * ReconnectIfTransitionSourceCommand();
             * cmd.setTransition((IIfTransition) request
             * .getConnectionEditPart().getModel()); cmd.setSource((IIf)
             * getHost().getModel()); return cmd;
             */
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        if (request.getConnectionEditPart().getModel() instanceof IStateTransition
                && getHost().getModel() instanceof ITransitionableTo) {
            ReconnectTargetCommand cmd = new ReconnectTargetCommand();
            cmd.setTransition((IStateTransition) request
                    .getConnectionEditPart().getModel());
            cmd.setTarget((ITransitionableTo) getState());
            return cmd;
        }
        else if (request.getConnectionEditPart().getModel() instanceof IIfTransition
                && getHost().getModel() instanceof ITransitionableTo) {
            ReconnectIfTransitionTargetCommand cmd = new ReconnectIfTransitionTargetCommand();
            cmd.setTransition((IIfTransition) request.getConnectionEditPart()
                    .getModel());
            cmd.setTarget((ITransitionableTo) getState());
            return cmd;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getTargetConnectionAnchor(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    protected ConnectionAnchor getTargetConnectionAnchor(
            CreateConnectionRequest request) {
        EditPart target = request.getTargetEditPart();

        if (target != null
                && (target.getModel() instanceof IActionElement || target.getModel() instanceof IAttributeMapper))
            return null;
        return target instanceof NodeEditPart ? ((NodeEditPart) target)
                .getTargetConnectionAnchor(request) : null;
    }
}