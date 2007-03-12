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
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class StateTransitionCreateCommand extends Command {

	/**
	 * 
	 */
	protected ITransitionableFrom source;

	/**
	 * 
	 */
	protected ITransitionableTo target;

	/**
	 * 
	 */
	protected StateTransition transition;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		if (source.equals(target))
			return false;
		if (source instanceof IEndState || source instanceof IDecisionState
				|| source instanceof IAttributeMapper
				|| source instanceof IActionElement) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		transition = new StateTransition();
		transition.createNew(source, (IWebflowState) source.getElementParent());
		transition.setOn("*");
		transition.setFromState(source);
		transition.setToState(target);
		source.addOutputTransition(transition);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public ITransitionableFrom getSource() {
		return source;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public ITransitionableTo getTarget() {
		return target;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public ITransition getTransition() {
		return transition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		source.addOutputTransition(transition);
		target.addInputTransition(transition);
	}

	/**
	 * 
	 * 
	 * @param activity 
	 */
	public void setSource(ITransitionableFrom activity) {
		source = activity;
	}

	/**
	 * 
	 * 
	 * @param activity 
	 */
	public void setTarget(ITransitionableTo activity) {
		target = activity;
	}

	/**
	 * 
	 * 
	 * @param transition 
	 */
	public void setTransition(StateTransition transition) {
		this.transition = transition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		source.removeOutputTransition(transition);
		target.removeInputTransition(transition);
	}
}