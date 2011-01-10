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
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * @author Christian Dupuis
 */
public class SplitStateTransitionCommand extends Command {

	/**
	 * 
	 */
	private int index;

	/**
	 * 
	 */
	private IState newActivity;

	/**
	 * 
	 */
	private IStateTransition newTransition;

	/**
	 * 
	 */
	private ITransitionableFrom oldSource;

	/**
	 * 
	 */
	private ITransitionableTo oldTarget;

	/**
	 * 
	 */
	private IWebflowState parent;

	/**
	 * 
	 */
	private IStateTransition oldTransition;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {

		newActivity.createNew(parent);

		int result = DialogUtils
				.openPropertiesDialog(parent, newActivity, true);
		if (result != Dialog.OK) {
			return;
		}

		index = parent.getStates().indexOf(oldSource) + 1;
		parent.addState(newActivity, index);
		oldTarget.removeInputTransition(oldTransition);
		oldTransition.setToState((ITransitionableTo) newActivity);
		
		
		newTransition = new StateTransition();
		newTransition.createNew(newActivity, (IWebflowState) newActivity.getElementParent());
		newTransition.setOn("*");
		newTransition.setFromState((ITransitionableFrom) newActivity);
		newTransition.setToState(oldTarget);
		((ITransitionableFrom) newActivity).addOutputTransition(newTransition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		parent.addState(newActivity, index);
		oldTarget.removeInputTransition(oldTransition);
		oldTransition.setToState((ITransitionableTo) newActivity);
		
		newTransition = new StateTransition();
		newTransition.createNew(newActivity, (IWebflowState) newActivity.getElementParent());
		newTransition.setOn("*");
		newTransition.setFromState((ITransitionableFrom) newActivity);
		newTransition.setToState(oldTarget);
		((ITransitionableFrom) newActivity).addOutputTransition(newTransition);
	}

	/**
	 * 
	 * 
	 * @param activity 
	 */
	public void setNewActivity(IState activity) {
		newActivity = activity;
	}

	/**
	 * 
	 * 
	 * @param activity 
	 */
	public void setParent(IWebflowState activity) {
		parent = activity;
	}

	/**
	 * 
	 * 
	 * @param transition 
	 */
	public void setTransition(IStateTransition transition) {
		this.oldTransition = transition;
		oldSource = transition.getFromState();
		oldTarget = transition.getToState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		((ITransitionableTo) newActivity).removeInputTransition(oldTransition);
		oldTransition.setToState((ITransitionableTo) oldTarget);
		((ITransitionableTo) oldTarget).removeInputTransition(newTransition);
		((ITransitionableFrom) newActivity).removeOutputTransition(newTransition);
		//((ITransitionableFrom) oldSource).addOutputTransition(oldTransition);
		parent.removeState(newActivity);
	}
}
