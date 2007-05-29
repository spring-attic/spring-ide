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
					(IWebflowModelElement) ((IWebflowModelElement) parent).getElementParent(),
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
