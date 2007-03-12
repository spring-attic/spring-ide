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
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;

/**
 * 
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