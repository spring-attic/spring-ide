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
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * @author Christian Dupuis
 */
public class CreateExceptionHandlerCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler child;

	/**
	 * 
	 */
	private int index = -1;

	/**
	 * 
	 */
	private boolean isMove = false;

	private boolean createNew = true;

	/**
	 * 
	 */
	private IState parent;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (createNew) {
			child.createNew(parent);
		}
		if (!isMove) {
			if (DialogUtils.openPropertiesDialog(null, child, true) != Dialog.OK) {
				return;
			}
		}
		if (index > 0) {
			((IState) parent).addExceptionHandler(child, index);
		}
		else {
			((IState) parent).addExceptionHandler(child);
		}
	}

	/**
	 * @param action
	 */
	public void setChild(IExceptionHandler action) {
		child = action;
	}

	/**
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * @param isMove
	 */
	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	/**
	 * @param sa
	 */
	public void setParent(IState sa) {
		parent = sa;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		((IState) parent).removeExceptionHandler(child);
	}
	
	public void redo() {
		boolean tempMove = this.isMove;
		boolean tempCreateNew = this.createNew;
		this.isMove = true;
		this.createNew = false;
		execute();
		this.isMove = tempMove;
		this.createNew = tempCreateNew;
	}
}
