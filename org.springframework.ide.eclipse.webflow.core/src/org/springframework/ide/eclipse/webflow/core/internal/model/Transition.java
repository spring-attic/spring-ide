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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class Transition extends AbstractModelElement implements
		ITransition {

	/**
	 * The webflow state.
	 */
	protected IWebflowState webflowState;

	/**
	 * The target state.
	 */
	protected ITransitionableTo targetState;

	/**
	 * The source state.
	 */
	protected ITransitionableFrom sourceState;

	/**
	 * The Constructor.
	 * @param webflowState the webflow state
	 */
	public Transition(IWebflowState webflowState) {
		this.webflowState = webflowState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#getToState()
	 */
	/**
	 * Gets the to state.
	 * @return the to state
	 */
	public ITransitionableTo getToState() {
		if (this.targetState == null) {
			this.targetState = (ITransitionableTo) WebflowModelXmlUtils
					.getStateById(webflowState, getToStateId());
		}
		return targetState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ITransition#setToState(org.springframework.ide.eclipse.web.core.model.ITransitionableState)
	 */
	/**
	 * Sets the to state.
	 * @param state the to state
	 */
	public void setToState(ITransitionableTo state) {
		ITransitionableTo newTargetState = null;
		if (state != null) {
			newTargetState = (ITransitionableTo) WebflowModelXmlUtils
					.getStateById(webflowState, state.getId());
		}
		if (newTargetState != null && !newTargetState.equals(targetState)) {
			if (targetState != null) {
				targetState.removeInputTransition(this);
			}
			setAttribute("to", state.getId());
			super.fireStructureChange(OUTPUTS, state);
			targetState = newTargetState;
			if (targetState != null) {
				targetState.addInputTransition(this);
			}
		}
		else {
			if (state != null) {
				setAttribute("to", state.getId());
			}
			else {
				targetState = newTargetState;
			}
			super.fireStructureChange(OUTPUTS, state);
		}
	}

	/**
	 * Gets the to state id.
	 * @return the to state id
	 */
	public String getToStateId() {
		return getAttribute("to");
	}
}