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
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
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
		IWebflowModelElement parent = (IWebflowModelElement) child.getElementParent();
		((ISubflowState) parent).setAttributeMapper(null);
	}

	/**
	 * @param newState
	 */
	public void setNewState(IWebflowModelElement newState) {
		this.newState = newState;
	}

}
