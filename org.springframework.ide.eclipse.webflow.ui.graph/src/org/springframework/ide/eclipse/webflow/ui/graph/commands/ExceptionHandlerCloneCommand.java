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
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class ExceptionHandlerCloneCommand extends Command {

	/**
	 * 
	 */
	private IExceptionHandler oldChild;

	/**
	 * 
	 */
	private IExceptionHandler child;

	/**
	 * 
	 */
	private IWebflowModelElement newState;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		child = ((ExceptionHandler) oldChild).cloneModelElement();
		child.setElementParent(newState);
		((IState) newState).addExceptionHandler(child);
	}

	/**
	 * @param child
	 */
	public void setChild(IExceptionHandler child) {
		this.oldChild = child;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement parent = child.getElementParent();
		((IState) parent).removeExceptionHandler(child);
	}

	/**
	 * @param newState
	 */
	public void setNewState(IWebflowModelElement newState) {
		this.newState = newState;
	}

}