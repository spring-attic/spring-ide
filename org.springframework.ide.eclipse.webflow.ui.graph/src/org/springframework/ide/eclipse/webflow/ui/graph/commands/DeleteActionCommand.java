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
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class DeleteActionCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;

	/**
	 * 
	 */
	private int index = -1;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		primExecute();
	}

	/**
	 * 
	 */
	protected void primExecute() {
		IWebflowModelElement parent = child.getElementParent();
		if (parent instanceof IEntryActions) {
			index = ((IEntryActions) parent).getEntryActions().indexOf(child);
			((IEntryActions) parent).removeEntryAction(child);
		}
		else if (parent instanceof IExitActions) {
			index = ((IExitActions) parent).getExitActions().indexOf(child);
			((IExitActions) parent).removeExitAction(child);
		}
		else if (parent instanceof IRenderActions) {
			index = ((IRenderActions) parent).getRenderActions().indexOf(child);
			((IRenderActions) parent).removeRenderAction(child);
		}
		else if (parent instanceof IActionState) {
			index = ((IActionState) parent).getActions().indexOf(child);
			((IActionState) parent).removeAction(child);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		primExecute();
	}

	/**
	 * 
	 * 
	 * @param a 
	 */
	public void setChild(IActionElement a) {
		child = a;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement parent = child.getElementParent();
		if (parent instanceof IEntryActions) {
			((IEntryActions) parent).addEntryAction(child, index);
		}
		else if (parent instanceof IExitActions) {
			((IExitActions) parent).addExitAction(child, index);
		}
		else if (parent instanceof IRenderActions) {
			((IRenderActions) parent).addRenderAction(child, index);
		}
		else if (parent instanceof IActionState) {
			((IActionState) parent).addAction(child, index);
		}
	}
}