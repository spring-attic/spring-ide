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
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;

/**
 * 
 */
public class DeleteExceptionHandlerCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler child;

	/**
	 * 
	 */
	private int index = -1;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		primExecute();
	}

	/**
	 * 
	 */
	protected void primExecute() {
		IState parent = (IState) child.getElementParent();
		index = parent.getExceptionHandlers().indexOf(child);
		parent.removeExceptionHandler(child);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		primExecute();
	}

	/**
	 * @param a
	 */
	public void setChild(IExceptionHandler a) {
		child = a;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IState parent = (IState) child.getElementParent();
		parent.addExceptionHandler(child, index);
	}
}
