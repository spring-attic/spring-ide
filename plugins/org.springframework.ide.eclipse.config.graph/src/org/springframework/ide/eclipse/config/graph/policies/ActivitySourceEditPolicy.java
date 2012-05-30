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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.model.commands.AddAndAssignSourceCommand;
import org.springframework.ide.eclipse.config.graph.model.commands.CreateAndAssignSourceCommand;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ActivitySourceEditPolicy extends ContainerEditPolicy {

	/**
	 * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	@Override
	protected Command getAddCommand(GroupRequest request) {
		CompoundCommand cmd = new CompoundCommand();
		for (int i = 0; i < request.getEditParts().size(); i++) {
			StructuredActivity parent = (StructuredActivity) getHost().getParent().getModel();
			AddAndAssignSourceCommand add = new AddAndAssignSourceCommand(parent.getDiagram().getTextEditor());
			add.setParent(parent);
			add.setSource((Activity) getHost().getModel());
			add.setChild(((Activity) ((EditPart) request.getEditParts().get(i)).getModel()));
			cmd.add(add);
		}
		return cmd;
	}

	/**
	 * @see ContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		StructuredActivity parent = (StructuredActivity) getHost().getParent().getModel();
		CreateAndAssignSourceCommand cmd = new CreateAndAssignSourceCommand(parent.getDiagram().getTextEditor());
		cmd.setParent(parent);
		cmd.setChild((Activity) request.getNewObject());
		cmd.setSource((Activity) getHost().getModel());
		return cmd;
	}

	/**
	 * @see AbstractEditPolicy#getTargetEditPart(org.eclipse.gef.Request)
	 */
	@Override
	public EditPart getTargetEditPart(Request request) {
		if (REQ_CREATE.equals(request.getType())) {
			return getHost();
		}
		if (REQ_ADD.equals(request.getType())) {
			return getHost();
		}
		if (REQ_MOVE.equals(request.getType())) {
			return getHost();
		}
		return super.getTargetEditPart(request);
	}

}
