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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;

/**
 * 
 */
public class DeleteCommand extends Command {

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
	private List<ITransition> sourceConnections = new ArrayList<ITransition>();

	/**
	 * 
	 */
	private List<ITransition> targetConnections = new ArrayList<ITransition>();

	/**
	 * 
	 */
	private boolean undo = true;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		return undo;
	}

	/**
	 * 
	 * 
	 * @param a 
	 */
	private void deleteConnections(IState a) {
		if (a instanceof IWebflowState) {
			List children = ((IWebflowState) a).getStates();
			for (int i = 0; i < children.size(); i++)
				deleteConnections((IState) children.get(i));
		}
		if (a instanceof ITransitionableFrom) {
			sourceConnections.addAll(((ITransitionableFrom) a)
					.getOutputTransitions());
			for (int i = 0; i < sourceConnections.size(); i++) {
				ITransition t = (ITransition) sourceConnections.get(i);
				((ITransitionableFrom) a).removeOutputTransition(t);
				t.getToState().removeInputTransition(t);
			}
		}
		if (a instanceof ITransitionableTo)
			targetConnections.addAll(((ITransitionableTo) a)
					.getInputTransitions());
		for (int i = 0; i < targetConnections.size(); i++) {
			if (targetConnections.get(i) instanceof IStateTransition) {
				IStateTransition t = (IStateTransition) targetConnections
						.get(i);
				t.getFromState().removeOutputTransition(t);
				((ITransitionableTo) a).removeInputTransition(t);
			}
			else if (targetConnections.get(i) instanceof IfTransition) {
				IfTransition t = (IfTransition) targetConnections.get(i);
				if (t.isThen()) {
					t.getFromIf().removeThenTransition();
				}
				else {
					t.getFromIf().removeElseTransition();
				}
				((ITransitionableTo) a).removeInputTransition(t);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (child instanceof IDecisionState
				&& ((IDecisionState) child).getIfs().size() > 0) {
			MessageDialog
					.openError(
							Activator.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							"Error deleting state",
							"The decision state '"
									+ this.child.getId()
									+ "' has at least one if criteria. \n\nPlease delete all if criterias before deleting the decision state.");
			this.undo = false;
			return;
		}
		if (this.child instanceof ITransitionableTo) {
			ITransitionableTo to = (ITransitionableTo) this.child;
			for (int i = 0; i < to.getInputTransitions().size(); i++) {
				ITransition tran = (ITransition) to.getInputTransitions()
						.get(i);
				if (tran instanceof IIfTransition
						&& ((IIfTransition) tran).isThen()) {
					MessageDialog
							.openError(
									Activator.getDefault().getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell(),
									"Error deleting state",
									"The state '"
											+ this.child.getId()
											+ "' has at least one incoming transition from an if criteria. \n\nRetry the deletion after retargeting the if criteria's then target to another state.");
					this.undo = false;
					return;
				}
			}
		}
		primExecute();
	}

	/**
	 * 
	 */
	protected void primExecute() {
		deleteConnections(child);
		if (child instanceof IInlineFlowState) {
			index = parent.getInlineFlowStates().indexOf(child);
			parent.removeInlineFlowState((IInlineFlowState) child);
		}
		else {
			index = parent.getStates().indexOf(child);
			parent.removeState(child);
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
	 */
	private void restoreConnections() {
		for (int i = 0; i < sourceConnections.size(); i++) {
			IStateTransition t = (IStateTransition) sourceConnections.get(i);
			t.getToState().addInputTransition(t);
			t.getFromState().addOutputTransition(t);
		}
		sourceConnections.clear();
		for (int i = 0; i < targetConnections.size(); i++) {
			if (targetConnections.get(i) instanceof IStateTransition) {
				IStateTransition t = (IStateTransition) targetConnections
						.get(i);
				t.getToState().addInputTransition(t);
				t.getFromState().addOutputTransition(t);
			}
			else if (targetConnections.get(i) instanceof IIfTransition) {
				IIfTransition t = (IIfTransition) targetConnections.get(i);
				t.getToState().addInputTransition(t);
				if (t.isThen()) {
					t.getFromIf().setThenTransition(t);
				}
				else {
					t.getFromIf().setElseTransition(t);
				}
			}
		}
		targetConnections.clear();
	}

	/**
	 * 
	 * 
	 * @param a 
	 */
	public void setChild(IState a) {
		child = a;
	}

	/**
	 * 
	 * 
	 * @param sa 
	 */
	public void setParent(IWebflowModelElement sa) {
		if (sa instanceof IWebflowState) {
			parent = (IWebflowState) sa;
		}
		else if (sa instanceof IInlineFlowState) {
			parent = ((IInlineFlowState) sa).getWebFlowState();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		if (child instanceof IInlineFlowState) {
			parent.addInlineFlowState((IInlineFlowState) child, index);
		}
		else {
			parent.addState(child, index);
		}
		restoreConnections();
	}
}