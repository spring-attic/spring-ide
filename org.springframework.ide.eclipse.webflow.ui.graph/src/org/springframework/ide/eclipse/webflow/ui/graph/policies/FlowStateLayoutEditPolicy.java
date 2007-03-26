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
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddAttributeMapperCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddExceptionHandlerCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AddIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateActionCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateAttributeMapperCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateExceptionHandlerCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateIfCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.CreateStateCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.IfPart;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StatePart;

/**
 * 
 */
public class FlowStateLayoutEditPolicy extends LayoutEditPolicy {

	/**
	 * @param child
	 * @return
	 */
	protected Command createAddActionCommand(EditPart child) {
		IActionElement activity = (IActionElement) child.getModel();
		AddActionCommand add = new AddActionCommand();
		add.setParent((IWebflowModelElement) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	/**
	 * @param child
	 * @return
	 */
	protected Command createAddAttributeMapperCommand(EditPart child) {
		IAttributeMapper activity = (IAttributeMapper) child.getModel();
		AddAttributeMapperCommand add = new AddAttributeMapperCommand();
		add.setParent((ISubflowState) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	/**
	 * @param child
	 * @return
	 */
	protected Command createAddIfCommand(EditPart child) {
		IIf activity = (IIf) child.getModel();
		AddIfCommand add = new AddIfCommand();
		add.setParent((IDecisionState) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if (child instanceof StatePart || child instanceof IfPart)
			return new StateSelectionEditPolicy();
		return new NonResizableEditPolicy();
	}

	/**
	 * @param child
	 * @param after
	 * @return
	 */
	protected Command createMoveChildCommand(EditPart child, EditPart after) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getAddCommand(org.eclipse.gef.Request)
	 */
	protected Command getAddCommand(Request req) {
		ChangeBoundsRequest request = (ChangeBoundsRequest) req;
		List editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		for (int i = 0; i < editParts.size(); i++) {
			EditPart child = (EditPart) editParts.get(i);
			if (child.getModel() instanceof IActionElement) {
				IActionElement ae = (IActionElement) child.getModel();
				IState state = (IState) getHost().getModel();
				if ((ae.getType() == IActionElement.ACTION_TYPE.ACTION && state instanceof IActionState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION && state instanceof IViewState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION && state instanceof IState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION && state instanceof ITransitionableFrom)) {
					command.add(createAddActionCommand(child));
				}
			}
			else if (child.getModel() instanceof IExceptionHandler) {
				IState state = (IState) getHost().getModel();
				if (!(state instanceof IWebflowState) && !(state instanceof IInlineFlowState)) {
					command.add(createAddExceptionHandlerCommand(child));
				}
			}
			else if (child.getModel() instanceof IAttributeMapper
					&& getHost().getModel() instanceof ISubflowState
					&& ((ISubflowState) getHost().getModel()).getAttributeMapper() == null) {
				command.add(createAddAttributeMapperCommand(child));
			}
			else if (child.getModel() instanceof IIf
					&& getHost().getModel() instanceof IDecisionState) {
				command.add(createAddIfCommand(child));
			}
		}
		return command.unwrap();
	}

	protected Command createAddExceptionHandlerCommand(EditPart child) {
		IExceptionHandler activity = (IExceptionHandler) child.getModel();
		AddExceptionHandlerCommand add = new AddExceptionHandlerCommand();
		add.setParent((IWebflowModelElement) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		if (getHost().getModel() instanceof IWebflowState
				&& request.getNewObject() instanceof IState) {
			CreateStateCommand command = new CreateStateCommand();
			command.setParent((IWebflowState) getHost().getModel());
			command.setChild((IState) request.getNewObject());
			return command;
		}
		else if (getHost().getModel() instanceof IInlineFlowState
				&& request.getNewObject() instanceof IState) {
			CreateStateCommand command = new CreateStateCommand();
			command.setParent(((IInlineFlowState) getHost().getModel())
					.getWebFlowState());
			command.setChild((IState) request.getNewObject());
			return command;
		}
		else if (getHost().getModel() instanceof IState
				&& request.getNewObject() instanceof IActionElement) {
			IActionElement ae = (IActionElement) request.getNewObject();
			IState state = (IState) getHost().getModel();
			if ((ae.getType() == IActionElement.ACTION_TYPE.ACTION && state instanceof IActionState)
					|| (ae.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION && state instanceof IViewState)
					|| (ae.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION && state instanceof IState)
					|| (ae.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION && state instanceof ITransitionableFrom)) {
				CreateActionCommand command = new CreateActionCommand();
				command.setParent((IState) getHost().getModel());
				command.setChild((IActionElement) request.getNewObject());
				return command;
			}
			else {
				return null;
			}
		}
		else if (getHost().getModel() instanceof IState
				&& !(getHost().getModel() instanceof IWebflowState)
				&& !(getHost().getModel() instanceof IInlineFlowState)
				&& request.getNewObject() instanceof IExceptionHandler) {
			CreateExceptionHandlerCommand command = new CreateExceptionHandlerCommand();
			command.setParent((IState) getHost().getModel());
			command.setChild((IExceptionHandler) request.getNewObject());
			return command;
		}
		else if (getHost().getModel() instanceof ISubflowState
				&& request.getNewObject() instanceof IAttributeMapper) {
			if (((ISubflowState) getHost().getModel()).getAttributeMapper() == null) {
				CreateAttributeMapperCommand command = new CreateAttributeMapperCommand();
				command.setParent((ISubflowState) getHost().getModel());
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
	 */
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getMoveChildrenCommand(org.eclipse.gef.Request)
	 */
	protected Command getMoveChildrenCommand(Request request) {
		return null;
	}

}
