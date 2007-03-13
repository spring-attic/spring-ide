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
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
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
			oldChild.applyCloneValues(child);
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
			oldChild.applyCloneValues(undoChild);
		}
		if (oldChild instanceof IStateTransition && newTarget != null
				&& oldTarget != null) {
			((IStateTransition) oldChild).setToState(oldTarget);
		}
	}
}