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
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
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