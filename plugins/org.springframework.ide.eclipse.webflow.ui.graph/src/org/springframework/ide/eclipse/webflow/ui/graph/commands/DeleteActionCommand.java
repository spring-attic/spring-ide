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
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 */
public class DeleteActionCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;

	/**
	 * 
	 */
	private int index = -1;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		primExecute();
	}

	/**
	 * 
	 */
	protected void primExecute() {
		IWebflowModelElement parent = (IWebflowModelElement) child.getElementParent();
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		primExecute();
	}

	/**
	 * 
	 * 
	 * @param a 
	 */
	public void setChild(IActionElement a) {
		child = a;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement parent = (IWebflowModelElement) child.getElementParent();
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
