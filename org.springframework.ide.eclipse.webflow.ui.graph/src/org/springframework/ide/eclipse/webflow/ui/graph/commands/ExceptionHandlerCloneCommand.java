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
package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class ExceptionHandlerCloneCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler oldChild;

	/**
	 * 
	 */
	private IExceptionHandler child;

	/**
	 * 
	 */
	private IWebflowModelElement newState;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		child = ((ExceptionHandler) oldChild).cloneModelElement();
		child.setElementParent(newState);
		((IState) newState).addExceptionHandler(child);
	}

	/**
	 * @param child
	 */
	public void setChild(IExceptionHandler child) {
		this.oldChild = child;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement parent = (IWebflowModelElement) child.getElementParent();
		((IState) parent).removeExceptionHandler(child);
	}

	/**
	 * @param newState
	 */
	public void setNewState(IWebflowModelElement newState) {
		this.newState = newState;
	}

}
