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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class CreateStateCommand extends Command {

	/**
	 * 
	 */
	private IState child;

	/**
	 * 
	 */
	private int index = -1;

	/**
	 * 
	 */
	private IWebflowState parent;

	/**
	 * 
	 */
	private List<ITransition> targetConnections = new ArrayList<ITransition>();

	/**
	 * 
	 */
	private int result;

	private void deleteConnections(IState a) {
		if (a instanceof ITransitionableTo)
			targetConnections.addAll(((ITransitionableTo) a)
					.getInputTransitions());
		for (int i = 0; i < targetConnections.size(); i++) {
			if (targetConnections.get(i) instanceof IStateTransition) {
				IStateTransition t = (IStateTransition) targetConnections
						.get(i);
				t.setToState(null);
				t.getFromState().fireStructureChange(
						WebflowModelElement.OUTPUTS, t);
			}
			if (targetConnections.get(i) instanceof IIfTransition) {
				IIfTransition t = (IIfTransition) targetConnections.get(i);
				t.setToState(null);
				((IWebflowModelElement) t.getElementParent()).fireStructureChange(
						WebflowModelElement.OUTPUTS, t);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// create xml element
		child.createNew(parent);
		result = DialogUtils.openPropertiesDialog(parent, child, true);
		if (result != Dialog.OK) {
			return;
		}
		if (index > 0) {
			parent.addState(child, index);
		}
		else {
			parent.addState(child);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		if (result != Dialog.OK) {
			return;
		}
		if (index > 0)
			parent.addState(child, index);
		else
			parent.addState(child);
	}

	/**
	 * @param activity
	 */
	public void setChild(IState activity) {
		child = activity;
	}

	/**
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * @param sa
	 */
	public void setParent(IWebflowState sa) {
		parent = sa;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		parent.removeState(child);
		deleteConnections(child);
	}
}
