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
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class EditPropertiesCommand extends Command {

	/**
	 * 
	 */
	IWebflowModelElement child = null;

	/**
	 * 
	 */
	ICloneableModelElement<IWebflowModelElement> oldChild = null;

	/**
	 * 
	 */
	IWebflowModelElement undoChild = null;

	/**
	 * 
	 */
	ITransitionableTo newTarget = null;

	/**
	 * 
	 */
	ITransitionableTo oldTarget = null;

	/**
	 * 
	 */
	boolean openDialog = true;

	/**
	 * 
	 */
	boolean onlyReconnect = false;

	/**
	 * 
	 */
	public EditPropertiesCommand() {
		super("Properties");
	}

	/**
	 * 
	 * 
	 * @param oldChild 
	 */
	public void setChild(ICloneableModelElement<IWebflowModelElement> oldChild) {
		this.oldChild = oldChild;
		// don't work on orginal domain model object
		this.child = oldChild.cloneModelElement();
		this.undoChild = oldChild.cloneModelElement();
	}

	/**
	 * 
	 * 
	 * @param oldChild 
	 * @param newChild 
	 */
	public void setChild(ICloneableModelElement<IWebflowModelElement> oldChild,
			IWebflowModelElement newChild) {
		setChild(oldChild, newChild, false);
	}

	/**
	 * 
	 * 
	 * @param openDialog 
	 * @param oldChild 
	 * @param newChild 
	 */
	public void setChild(ICloneableModelElement<IWebflowModelElement> oldChild,
			IWebflowModelElement newChild, boolean openDialog) {
		this.oldChild = oldChild;
		// don't work on orginal domain model object
		this.child = newChild;
		this.undoChild = oldChild.cloneModelElement();
		this.openDialog = openDialog;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		int result = -1;
		if (!onlyReconnect) {
			if (openDialog) {
				if (((IWebflowModelElement) oldChild).getElementParent() instanceof IWebflowModelElement) {
					result = DialogUtils.openPropertiesDialog(
							((IWebflowModelElement) oldChild)
									.getElementParent(),
							(IWebflowModelElement) child, false);
				}
				else {
					result = DialogUtils.openPropertiesDialog(null,
							(IWebflowModelElement) child, false);
				}
				if (result == Dialog.OK) {
					oldChild.applyCloneValues(child);
				}
			}
			else {
				oldChild.applyCloneValues(child);
			}
		}

		if (oldChild instanceof IStateTransition && newTarget != null) {
			oldTarget = ((IStateTransition) oldChild).getToState();
			((IStateTransition) oldChild).setToState(newTarget);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (!onlyReconnect) {
			oldChild.applyCloneValues(undoChild);
		}
		if (oldChild instanceof IStateTransition && newTarget != null
				&& oldTarget != null) {
			((IStateTransition) oldChild).setToState(oldTarget);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		oldChild.applyCloneValues(child);
	}

	/**
	 * 
	 * 
	 * @param newTarget 
	 */
	public void setNewTarget(ITransitionableTo newTarget) {
		this.newTarget = newTarget;
	}

	/**
	 * 
	 * 
	 * @param onlyReconnect 
	 */
	public void setOnlyReconnect(boolean onlyReconnect) {
		this.onlyReconnect = onlyReconnect;
	}
}