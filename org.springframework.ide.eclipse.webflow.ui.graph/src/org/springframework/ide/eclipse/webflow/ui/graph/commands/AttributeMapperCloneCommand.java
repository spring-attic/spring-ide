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
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 */
public class AttributeMapperCloneCommand extends Command {

	/**
	 * 
	 */
	private IAttributeMapper oldChild;

	/**
	 * 
	 */
	private IAttributeMapper child;

	/**
	 * 
	 */
	private IWebflowModelElement newState;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		child = ((AttributeMapper) oldChild).cloneModelElement();
		child.setElementParent(newState);
		((ISubflowState) newState).setAttributeMapper(child);
	}

	/**
	 * @param child
	 */
	public void setChild(IAttributeMapper child) {
		this.oldChild = child;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		IWebflowModelElement parent = child.getElementParent();
		((ISubflowState) parent).setAttributeMapper(null);
	}

	/**
	 * @param newState
	 */
	public void setNewState(IWebflowModelElement newState) {
		this.newState = newState;
	}

}