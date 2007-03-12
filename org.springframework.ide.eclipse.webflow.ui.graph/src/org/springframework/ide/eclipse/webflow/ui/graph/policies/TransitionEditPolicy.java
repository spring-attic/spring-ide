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

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.webflow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddTransitionActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteIfTransitionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteStateTransitionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.SplitIfTransitionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.SplitStateTransitionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.IfTransitionPart;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StateTransitionPart;

/**
 * 
 */
public class TransitionEditPolicy extends ConnectionEditPolicy {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	public void eraseTargetFeedback(Request request) {
		if (REQ_CREATE.equals(request.getType()))
			getConnectionFigure().setLineWidth(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (REQ_CREATE.equals(request.getType()))
			return getSplitTransitionCommand(request);
		else if (EditPropertiesAction.EDITPROPERTIES_REQUEST.equals(request
				.getType())) {
			return getEditPropertiesCommand();
		}
		return super.getCommand(request);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	private PolylineConnection getConnectionFigure() {
		if (getHost() instanceof StateTransitionPart) {
			return ((PolylineConnection) ((StateTransitionPart) getHost())
					.getFigure());
		}
		else if (getHost() instanceof IfTransitionPart) {
			return ((PolylineConnection) ((IfTransitionPart) getHost())
					.getFigure());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getDeleteCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	protected Command getDeleteCommand(GroupRequest request) {
		if (getHost().getModel() instanceof StateTransition) {
			DeleteStateTransitionCommand cmd = new DeleteStateTransitionCommand();
			StateTransition t = (StateTransition) getHost().getModel();
			cmd.setTransition(t);
			cmd.setSource(t.getFromState());
			cmd.setTarget(t.getToState());
			return cmd;
		}
		else if (getHost().getModel() instanceof IfTransition) {
			IfTransition t = (IfTransition) getHost().getModel();
			if (!t.isThen()) {
				DeleteIfTransitionCommand cmd = new DeleteIfTransitionCommand();
				cmd.setTransition(t);
				cmd.setSource(t.getFromIf());
				cmd.setTarget(t.getToState());
				return cmd;
			}
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	protected Command getEditPropertiesCommand() {
		EditPropertiesCommand command = new EditPropertiesCommand();
		command
				.setChild((ICloneableModelElement<IWebflowModelElement>) getHost()
						.getModel());
		return command;
	}

	/**
	 * 
	 * 
	 * @param request 
	 * 
	 * @return 
	 */
	protected Command getSplitTransitionCommand(Request request) {
		if (((CreateRequest) request).getNewObject() instanceof IState) {
			if (getHost().getModel() instanceof IStateTransition) {
				SplitStateTransitionCommand cmd = new SplitStateTransitionCommand();
				cmd.setTransition(((IStateTransition) getHost().getModel()));
				cmd
						.setParent(((IWebflowState) ((StateTransitionPart) getHost())
								.getSource().getParent().getModel()));
				cmd.setNewActivity(((IState) ((CreateRequest) request)
						.getNewObject()));
				return cmd;
			}
			else if (getHost().getModel() instanceof IIfTransition) {
				SplitIfTransitionCommand cmd = new SplitIfTransitionCommand();
				cmd.setTransition(((IIfTransition) getHost().getModel()));
				cmd.setParent(((IWebflowState) ((IfTransitionPart) getHost())
						.getTarget().getParent().getModel()));
				cmd.setNewActivity(((IState) ((CreateRequest) request)
						.getNewObject()));
				return cmd;
			}
		}
		else if (((CreateRequest) request).getNewObject() instanceof IActionElement
				&& getHost().getModel() instanceof IStateTransition) {
			IActionElement action = (IActionElement) ((CreateRequest) request)
					.getNewObject();
			if (action.getType() == IActionElement.ACTION_TYPE.ACTION) {
				AddTransitionActionCommand cmd = new AddTransitionActionCommand();
				cmd.setTransition(((IStateTransition) getHost().getModel()));
				cmd.setNewAction(((IActionElement) ((CreateRequest) request)
						.getNewObject()));
				return cmd;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getTargetEditPart(org.eclipse.gef.Request)
	 */
	public EditPart getTargetEditPart(Request request) {
		if (REQ_CREATE.equals(request.getType()))
			return getHost();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
	 */
	public void showTargetFeedback(Request request) {
		if (REQ_CREATE.equals(request.getType()))
			getConnectionFigure().setLineWidth(2);
	}
}