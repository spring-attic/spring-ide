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
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 */
public class EditPropertiesCommand extends Command {

	private IWebflowModelElement child = null;

	private ITransitionableTo newTarget = null;

	private ICloneableModelElement<IWebflowModelElement> oldChild = null;

	private ITransitionableTo oldTarget = null;

	private boolean onlyReconnect = false;

	private IWebflowModelElement undoChild = null;

	public EditPropertiesCommand() {
		super("Properties");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (!onlyReconnect) {
			IWebflowModelElement tempChild = ((ICloneableModelElement<IWebflowModelElement>) child)
					.cloneModelElement();
			oldChild.applyCloneValues(tempChild);
		}
		if (oldChild instanceof IStateTransition && newTarget != null) {
			oldTarget = ((IStateTransition) oldChild).getToState();
			((IStateTransition) oldChild).setToState(newTarget);
		}
	}

	public IWebflowModelElement getChild() {
		return child;
	}

	public IWebflowModelElement getChildClone() {
		return this.child;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		execute();
	}

	public void setChild(ICloneableModelElement<IWebflowModelElement> oldChild) {
		this.oldChild = oldChild;
		// don't work on orginal domain model object
		this.child = oldChild.cloneModelElement();
		this.undoChild = oldChild.cloneModelElement();
	}

	public void setChild(ICloneableModelElement<IWebflowModelElement> oldChild,
			IWebflowModelElement newChild) {
		this.oldChild = oldChild;
		// don't work on orginal domain model object
		this.child = newChild;
		this.undoChild = oldChild.cloneModelElement();
	}

	public void setNewTarget(ITransitionableTo newTarget) {
		this.newTarget = newTarget;
	}

	public void setOnlyReconnect(boolean onlyReconnect) {
		this.onlyReconnect = onlyReconnect;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (!onlyReconnect) {
			IWebflowModelElement tempChild = ((ICloneableModelElement<IWebflowModelElement>) undoChild)
					.cloneModelElement();
			oldChild.applyCloneValues(tempChild);
		}
		if (oldChild instanceof IStateTransition && newTarget != null
				&& oldTarget != null) {
			((IStateTransition) oldChild).setToState(oldTarget);
		}
	}
}
