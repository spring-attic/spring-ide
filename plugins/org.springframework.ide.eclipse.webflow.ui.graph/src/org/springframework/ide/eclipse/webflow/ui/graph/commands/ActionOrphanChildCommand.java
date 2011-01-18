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
package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 */
public class ActionOrphanChildCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;
	
	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/**
	 * 
	 */
	private int index;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		parent = (IWebflowModelElement) child.getElementParent();
		if (parent instanceof IEntryActions) {
			index = ((IEntryActions) parent).getEntryActions().indexOf(child);
			((IEntryActions) parent).removeEntryAction(child);
		}
		else if (parent instanceof IExitActions) {
			index = ((IExitActions) parent).getExitActions().indexOf(child);
			((IExitActions) parent).removeExitAction(child);
		}
		else if (parent instanceof IRenderActions) {
			index = ((IRenderActions) parent).getRenderActions().indexOf(child);
			((IRenderActions) parent).removeRenderAction(child);
		}
		else if (parent instanceof IActionState) {
			index = ((IActionState) parent).getActions().indexOf(child);
			((IActionState) parent).removeAction(child);
		}
		
		if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION
				&& parent instanceof IRenderActions) {
			IRenderActions state = (IRenderActions) parent;
			if (state != null
					&& state.getRenderActions().size() == 0) {
				((IViewState) state.getElementParent()).setRenderActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION
				&& parent instanceof IEntryActions) {
			IEntryActions state = (IEntryActions) parent;
			if (state != null
					&& state.getEntryActions().size() == 0) {
				((IState) state.getElementParent()).setEntryActions(null);
			}
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION
				&& parent instanceof IExitActions) {
			IExitActions state = (IExitActions) parent;
			if (state != null
					&& state.getExitActions().size() == 0) {
				((IState) state.getElementParent()).setExitActions(null);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param child 
	 */
	public void setChild(IActionElement child) {
		this.child = child;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (child.getType() == IActionElement.ACTION_TYPE.ACTION
				&& parent instanceof IActionState) {
			child.setElementParent(parent);
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.RENDER_ACTION
				&& parent instanceof IRenderActions) {
			IRenderActions state = (IRenderActions) parent;
			IViewState viewState = (IViewState) state.getElementParent();
			if (viewState.getRenderActions() == null) {
				viewState.setRenderActions(state);
			}
			child.setElementParent(state);
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.ENTRY_ACTION
				&& parent instanceof IEntryActions) {
			IEntryActions state = (IEntryActions) parent;
			IState viewState = (IState) state.getElementParent();
			if (viewState.getEntryActions() == null) {
				viewState.setEntryActions(state);
			}
			child.setElementParent(state);
		}
		else if (child.getType() == IActionElement.ACTION_TYPE.EXIT_ACTION
				&& parent instanceof IExitActions) {
			IExitActions state = (IExitActions) parent;
			IState viewState = (IState) state.getElementParent();
			if (viewState.getExitActions() == null) {
				viewState.setExitActions(state);
			}
			child.setElementParent(state);
		}
		
		if (parent instanceof IEntryActions) {
			((IEntryActions) parent).addEntryAction(child, index);
		}
		else if (parent instanceof IExitActions) {
			((IExitActions) parent).addExitAction(child, index);
		}
		else if (parent instanceof IRenderActions) {
			((IRenderActions) parent).addRenderAction(child, index);
		}
		else if (parent instanceof IActionState) {
			((IActionState) parent).addAction(child, index);
		}
	}

}
