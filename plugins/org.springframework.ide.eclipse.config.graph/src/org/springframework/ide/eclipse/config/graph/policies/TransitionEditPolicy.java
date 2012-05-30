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
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.model.commands.DeleteConnectionCommand;


/**
 * EditPolicy for Transitions. Supports deletion and "splitting", i.e. adding an
 * Activity that splits the transition into an incoming and outgoing connection
 * to the new Activity.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class TransitionEditPolicy extends ConnectionEditPolicy {

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	// @Override
	// public void eraseTargetFeedback(Request request) {
	// if (REQ_CREATE.equals(request.getType())) {
	// getConnectionFigure().setLineWidth(1);
	// }
	// }

	/**
	 * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	// @Override
	// public Command getCommand(Request request) {
	// if (REQ_CREATE.equals(request.getType())) {
	// return getSplitTransitionCommand(request);
	// }
	// return super.getCommand(request);
	// }

	// private PolylineConnection getConnectionFigure() {
	// return ((PolylineConnection) ((TransitionPart) getHost()).getFigure());
	// }

	/**
	 * @see ConnectionEditPolicy#getDeleteCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	@Override
	protected Command getDeleteCommand(GroupRequest request) {
		Transition transition = (Transition) getHost().getModel();
		Activity source = transition.source;
		DeleteConnectionCommand cmd = new DeleteConnectionCommand(source.getDiagram().getTextEditor());
		cmd.setTransition(transition);
		cmd.setSource(source);
		cmd.setTarget(transition.target);
		return cmd;
	}

	// protected Command getSplitTransitionCommand(Request request) {
	// SplitTransitionCommand cmd = new SplitTransitionCommand();
	// cmd.setTransition(((Transition) getHost().getModel()));
	// cmd.setParent(((StructuredActivity) ((TransitionPart)
	// getHost()).getSource().getParent().getModel()));
	// cmd.setNewActivity(((Activity) ((CreateRequest)
	// request).getNewObject()));
	// return cmd;
	// }

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getTargetEditPart(org.eclipse.gef.Request)
	 */
	// @Override
	// public EditPart getTargetEditPart(Request request) {
	// if (REQ_CREATE.equals(request.getType())) {
	// return getHost();
	// }
	// return null;
	// }

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
	 */
	// @Override
	// public void showTargetFeedback(Request request) {
	// if (REQ_CREATE.equals(request.getType())) {
	// getConnectionFigure().setLineWidth(2);
	// }
	// }

}
