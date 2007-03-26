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
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
 */
public class AddTransitionActionCommand extends Command {

	/**
	 * 
	 */
	private IActionElement child;

	/**
	 * 
	 */
	private IStateTransition parent;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (child instanceof Action) {
			((Action) child).createNew(parent);
		}
		else if (child instanceof BeanAction) {
			((BeanAction) child).createNew(parent);
		}
		else if (child instanceof EvaluateAction) {
			((EvaluateAction) child).createNew(parent);
		}
		else if (child instanceof Set) {
			((Set) child).createNew(parent);
		}
		if (DialogUtils.openPropertiesDialog(null, child, true) != Dialog.OK) {
			return;
		}

		if (child.getType() == IActionElement.ACTION_TYPE.ACTION) {
				((IStateTransition) parent).addAction(child);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		parent.addAction(child);
	}

	/**
	 * 
	 * 
	 * @param activity 
	 */
	public void setNewAction(IActionElement activity) {
		child = activity;
	}

	/**
	 * 
	 * 
	 * @param transition 
	 */
	public void setTransition(IStateTransition transition) {
		this.parent = transition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		parent.removeAction(child);
	}

}
