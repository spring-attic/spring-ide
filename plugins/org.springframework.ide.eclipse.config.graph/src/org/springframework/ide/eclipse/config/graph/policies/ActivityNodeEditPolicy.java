/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.graph.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.commands.AbstractConnectionCreateCommand;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;


/**
 * Created on Jul 17, 2003
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ActivityNodeEditPolicy extends GraphicalNodeEditPolicy {

	/**
	 * Returns the model associated with the EditPart on which this EditPolicy
	 * is installed
	 * @return the model
	 */
	protected Activity getActivity() {
		return (Activity) getHost().getModel();
	}

	/**
	 * Returns the ActivityPart on which this EditPolicy is installed
	 * @return the
	 */
	protected ActivityPart getActivityPart() {
		return (ActivityPart) getHost();
	}

	/**
	 * @see GraphicalNodeEditPolicy#getConnectionCompleteCommand(CreateConnectionRequest)
	 */
	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		AbstractConnectionCreateCommand cmd = (AbstractConnectionCreateCommand) request.getStartCommand();
		cmd.setTarget(getActivity());
		return cmd;
	}

	/**
	 * @see GraphicalNodeEditPolicy#getConnectionCreateCommand(CreateConnectionRequest)
	 */
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		return null;
	}

	/**
	 * @see GraphicalNodeEditPolicy#getReconnectSourceCommand(ReconnectRequest)
	 */
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		// Activity source = getActivity();
		// ReconnectSourceCommand cmd = new
		// ReconnectSourceCommand(source.getDiagram().getTextEditor());
		// cmd.setTransition((Transition)
		// request.getConnectionEditPart().getModel());
		// cmd.setSource(source);
		// return cmd;
		return null;
	}

	/**
	 * @see GraphicalNodeEditPolicy#getReconnectTargetCommand(ReconnectRequest)
	 */
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		// Activity target = getActivity();
		// ReconnectTargetCommand cmd = new
		// ReconnectTargetCommand(target.getDiagram().getTextEditor());
		// cmd.setTransition((Transition)
		// request.getConnectionEditPart().getModel());
		// cmd.setTarget(target);
		// return cmd;
		return null;
	}

}
