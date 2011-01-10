/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
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
