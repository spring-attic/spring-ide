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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractTransitionableTo extends AbstractState implements
		ITransitionableTo {

	/**
	 * The input transitions.
	 */
	private List<ITransition> inputTransitions = new ArrayList<ITransition>();

	/**
	 * 
	 * 
	 * @param node
	 * @param parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public List<ITransition> getInputTransitions() {
		return inputTransitions;
	}

	/**
	 * 
	 * 
	 * @param transitions
	 */
	public void addInputTransition(ITransition transitions) {
		this.inputTransitions.add(transitions);
		super.fireStructureChange(INPUTS, transitions);
	}

	/**
	 * 
	 * 
	 * @param transitions
	 */
	public void removeInputTransition(ITransition transitions) {
		this.inputTransitions.remove(transitions);
		super.fireStructureChange(INPUTS, transitions);
	}

	/**
	 * 
	 * 
	 * @param id
	 */
	@Override
	public void setId(String id) {
		String oldId = getId();
		IState oldStartState = ((WebflowState) parent).getStartState();
		setAttribute("id", id);
		if (!id.equals(oldId) && parent instanceof WebflowState) {
			// we are the startstate and rename the id
			if (this.equals(oldStartState)) {
				((WebflowState) parent).setStartState(this);
			}
		}
		if (this.inputTransitions != null && this.inputTransitions.size() > 0) {
			for (ITransition trans : this.inputTransitions) {
				trans.setToState(this);
			}
		}
	}
}
