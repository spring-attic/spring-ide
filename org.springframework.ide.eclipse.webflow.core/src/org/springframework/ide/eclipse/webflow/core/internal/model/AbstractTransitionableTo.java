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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 * 
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
		IState oldStartState = ((WebflowState) parent).getStartState();
		setAttribute("id", id);
		if (this.inputTransitions != null && this.inputTransitions.size() > 0) {
			for (ITransition trans : this.inputTransitions) {
				trans.setToState(this);
			}
		}
		if (parent instanceof WebflowState) {
			// we are the startstate and rename the id
			if (this.equals(oldStartState)) {
				((WebflowState) parent).setStartState(this);
			}
		}
	}
}