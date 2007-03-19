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

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.SetAsStartStateAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteAttributeMapperCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteExceptionHandlerCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.DeleteStatePropertyCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;

/**
 * 
 */
public class StateEditPolicy extends ComponentEditPolicy {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		if (getHost().getModel() instanceof IAttribute
				&& getHost().getParent().getModel() instanceof IAttributeEnabled) {
			IAttributeEnabled parent = (IAttributeEnabled) (getHost()
					.getParent().getModel());
			DeleteStatePropertyCommand deleteCmd = new DeleteStatePropertyCommand();
			deleteCmd.setParent(parent);
			deleteCmd.setChild((IAttribute) (getHost().getModel()));
			return deleteCmd;
		}
		else if (getHost().getModel() instanceof IAttributeMapper
				&& getHost().getParent().getModel() instanceof ISubflowState) {
			ISubflowState parent = (ISubflowState) (getHost().getParent()
					.getModel());
			DeleteAttributeMapperCommand deleteCmd = new DeleteAttributeMapperCommand();
			deleteCmd.setParent(parent);
			deleteCmd.setChild((IAttributeMapper) (getHost().getModel()));
			return deleteCmd;
		}
		else if (getHost().getParent().getModel() instanceof IAttributeMapper) {
			// IAttributeMapper parent = (IAttributeMapper)
			// (getHost().getParent()
			// .getModel());
			// DeleteInputOutputCommand deleteCmd = new
			// DeleteInputOutputCommand();
			// deleteCmd.setParent(parent);
			// deleteCmd.setChild(getHost().getModel());
			// return deleteCmd;
		}
		else if (getHost().getParent().getModel() instanceof IWebflowState
				|| getHost().getParent().getModel() instanceof IInlineFlowState) {
			IWebflowModelElement parent = (IWebflowModelElement) (getHost()
					.getParent().getModel());
			DeleteCommand deleteCmd = new DeleteCommand();
			deleteCmd.setParent(parent);
			deleteCmd.setChild((IState) (getHost().getModel()));
			return deleteCmd;
		}
		else if (getHost().getModel() instanceof IActionElement) {
			DeleteActionCommand deleteCmd = new DeleteActionCommand();
			deleteCmd.setChild((IActionElement) (getHost().getModel()));
			return deleteCmd;
		}
		else if (getHost().getModel() instanceof IExceptionHandler) {
			DeleteExceptionHandlerCommand deleteCmd = new DeleteExceptionHandlerCommand();
			deleteCmd.setChild((IExceptionHandler) (getHost().getModel()));
			return deleteCmd;
		}
		else if (getHost().getParent().getModel() instanceof IDecisionState
				&& getHost().getModel() instanceof IIf) {
			IDecisionState parent = (IDecisionState) (getHost().getParent()
					.getModel());
			DeleteIfCommand deleteCmd = new DeleteIfCommand();
			deleteCmd.setParent(parent);
			deleteCmd.setChild((IIf) (getHost().getModel()));
			return deleteCmd;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (SetAsStartStateAction.STARTSTATE_REQUEST.equals(request.getType()))
			return getSetAsStartStateCommand();
		else if (EditPropertiesAction.EDITPROPERTIES_REQUEST.equals(request
				.getType())) {
			return getEditPropertiesCommand();
		}
		return super.getCommand(request);
	}

	/**
	 * @return
	 */
	protected Command getSetAsStartStateCommand() {
		SetAsStartStateCommand command = new SetAsStartStateCommand();
		command.setChild((IState) getHost().getModel());
		return command;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Command getEditPropertiesCommand() {
		EditPropertiesCommand command = new EditPropertiesCommand();
		if (getHost().getModel() instanceof IAttributeMapper) {
			command
					.setChild((ICloneableModelElement<IWebflowModelElement>) ((IAttributeMapper) getHost()
							.getModel()).getElementParent());
		}
		else {
			command
					.setChild((ICloneableModelElement<IWebflowModelElement>) getHost()
							.getModel());
		}
		return command;
	}

	/**
	 * 
	 */
	static class SetAsStartStateCommand extends
			org.eclipse.gef.commands.Command {

		/**
		 * 
		 */
		IState child = null;

		/**
		 * 
		 */
		IState oldChild = null;

		/**
		 * 
		 */
		int index = -1;

		/**
		 * 
		 */
		public SetAsStartStateCommand() {
			super("Set as start state");
		}

		/**
		 * @param child
		 */
		public void setChild(IState child) {
			this.child = child;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.gef.commands.Command#execute()
		 */
		public void execute() {
			if (child.getElementParent() instanceof IWebflowState) {
				IWebflowState state = (IWebflowState) child.getElementParent();
				oldChild = state.getStartState();
				index = state.getStates().indexOf(child);
				state.setStartState(child);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.gef.commands.Command#undo()
		 */
		public void undo() {
			if (child.getElementParent() instanceof IWebflowState) {
				IWebflowState state = (IWebflowState) child.getElementParent();
				state.setStartState(oldChild);
				state.moveState(child, index);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.gef.commands.Command#redo()
		 */
		public void redo() {
			execute();
		}
	}

}