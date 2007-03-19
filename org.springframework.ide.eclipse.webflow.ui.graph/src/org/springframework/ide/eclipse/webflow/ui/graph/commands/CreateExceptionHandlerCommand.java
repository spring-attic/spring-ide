/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
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