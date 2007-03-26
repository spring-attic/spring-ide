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
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class AddExceptionHandlerCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler child;

	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		child.setElementParent(parent);
		((IState) parent).addExceptionHandler(child);
	}

	/**
	 * @param newChild
	 */
	public void setChild(IExceptionHandler newChild) {
		child = newChild;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		((IState) parent).removeExceptionHandler(child);
	}

	/**
	 * @param element
	 */
	public void setParent(IWebflowModelElement element) {
		this.parent = element;
	}

}
