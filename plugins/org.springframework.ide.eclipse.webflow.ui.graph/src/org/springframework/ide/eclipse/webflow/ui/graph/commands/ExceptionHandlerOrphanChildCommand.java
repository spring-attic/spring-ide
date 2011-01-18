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
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;

/**
 * @author Christian Dupuis
 */
public class ExceptionHandlerOrphanChildCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler child;
	
	/**
	 * 
	 */
	private IState parent;

	/**
	 * 
	 */
	private int index;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		parent = (IState) child.getElementParent();
		index = parent.getExceptionHandlers().indexOf(child);
		parent.removeExceptionHandler(child);
	}

	/**
	 * 
	 * 
	 * @param child 
	 */
	public void setChild(IExceptionHandler child) {
		this.child = child;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		child.setElementParent(parent);
		parent.addExceptionHandler(child, index);
	}

}
