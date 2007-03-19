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
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class CreateAttributeMapperCommand extends Command {

	private boolean isMove = false;

	private ISubflowState parent;

	private ISubflowState newChild;

	private ISubflowState undoChild;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		int result = 0;
		if (!isMove) {
			result = DialogUtils.openPropertiesDialog(
					((IWebflowModelElement) parent).getElementParent(),
					newChild, true, 1);
		}
		if (result == Dialog.OK) {
			IWebflowModelElement tempChild = ((ICloneableModelElement<IWebflowModelElement>) newChild)
					.cloneModelElement();
			((ICloneableModelElement<IWebflowModelElement>) parent)
					.applyCloneValues(tempChild);
		}
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
	public void setParent(ISubflowState sa) {
		this.parent = sa;

		// don't work on orginal domain model object
		this.newChild = (ISubflowState) ((ICloneableModelElement<IWebflowModelElement>) parent)
				.cloneModelElement();
		this.undoChild = (ISubflowState) ((ICloneableModelElement<IWebflowModelElement>) parent)
				.cloneModelElement();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement tempChild = ((ICloneableModelElement<IWebflowModelElement>) undoChild)
				.cloneModelElement();
		((ICloneableModelElement<IWebflowModelElement>) parent)
				.applyCloneValues(tempChild);
	}

	public void redo() {
		boolean tempMove = this.isMove;
		this.isMove = true;
		execute();
		this.isMove = tempMove;
	}
}