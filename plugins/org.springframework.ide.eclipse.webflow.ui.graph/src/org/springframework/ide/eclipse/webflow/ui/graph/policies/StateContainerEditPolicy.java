/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
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
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ActionCloneCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ActionOrphanChildCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AttributeMapperCloneCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.AttributeMapperOrphanChildCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ExceptionHandlerCloneCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.ExceptionHandlerOrphanChildCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.IfOrphanChildCommand;

/**
 * @author Christian Dupuis
 */
public class StateContainerEditPolicy extends ContainerEditPolicy {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getOrphanChildrenCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	protected Command getOrphanChildrenCommand(GroupRequest request) {
		List parts = request.getEditParts();
		CompoundCommand result = new CompoundCommand();
		for (int i = 0; i < parts.size(); i++) {
			EditPart part = (EditPart) parts.get(i);
			if (part.getModel() instanceof IActionElement) {
				ActionOrphanChildCommand orphan = new ActionOrphanChildCommand();
				orphan.setChild((IActionElement) ((EditPart) parts.get(i))
						.getModel());
				result.add(orphan);
			}
			else if (part.getModel() instanceof IExceptionHandler) {
				ExceptionHandlerOrphanChildCommand orphan = new ExceptionHandlerOrphanChildCommand();
				orphan.setChild((IExceptionHandler) ((EditPart) parts.get(i))
						.getModel());
				result.add(orphan);
			}
			else if (part.getModel() instanceof IAttributeMapper) {
				AttributeMapperOrphanChildCommand orphan = new AttributeMapperOrphanChildCommand();
				orphan.setChild((IAttributeMapper) ((EditPart) parts.get(i))
						.getModel());
				orphan.setParent((ISubflowState) getHost().getModel());
				result.add(orphan);
			}
			else if (part.getModel() instanceof IIf) {
				IfOrphanChildCommand orphan = new IfOrphanChildCommand();
				orphan.setChild((IIf) ((EditPart) parts.get(i)).getModel());
				orphan.setParent((IDecisionState) getHost().getModel());
				result.add(orphan);
			}
		}
		return result.unwrap();
	}

	/**
	 * Override to contribute to clone requests.
	 * @param request the clone request
	 * @return the command contribution to the clone
	 */
	protected Command getCloneCommand(ChangeBoundsRequest request) {
		List parts = request.getEditParts();
		CompoundCommand result = new CompoundCommand();
		for (int i = 0; i < parts.size(); i++) {
			EditPart part = (EditPart) parts.get(i);
			if (part.getModel() instanceof IActionElement) {
				IActionElement ae = (IActionElement) part.getModel();
				IState state = (IState) getHost().getModel();
				if ((ae.getType() == IActionElement.ACTION_TYPE.ACTION && state instanceof IActionState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION && state instanceof IViewState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION && state instanceof IState)
						|| (ae.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION && state instanceof ITransitionableFrom)) {
					ActionCloneCommand orphan = new ActionCloneCommand();
					orphan.setChild((IActionElement) ((EditPart) parts.get(i))
							.getModel());
					orphan.setNewState((IWebflowModelElement) getHost()
							.getModel());
					result.add(orphan);
				}
			}
			else if (part.getModel() instanceof IExceptionHandler) {
				IState state = (IState) getHost().getModel();
				if (!(state instanceof IWebflowState)
						&& !(state instanceof IInlineFlowState)) {
					ExceptionHandlerCloneCommand orphan = new ExceptionHandlerCloneCommand();
					orphan.setChild((IExceptionHandler) ((EditPart) parts.get(i))
							.getModel());
					orphan.setNewState((IWebflowModelElement) getHost()
							.getModel());
					result.add(orphan);
				}
			}
			else if (part.getModel() instanceof IAttributeMapper) {
				IState state = (IState) getHost().getModel();
				if (state instanceof SubflowState
						&& ((SubflowState) state).getAttributeMapper() == null) {
					AttributeMapperCloneCommand orphan = new AttributeMapperCloneCommand();
					orphan.setChild((IAttributeMapper) ((EditPart) parts.get(i))
							.getModel());
					orphan.setNewState((IWebflowModelElement) getHost()
							.getModel());
					result.add(orphan);
				}
			}
		}
		return result.unwrap();
	}
}
