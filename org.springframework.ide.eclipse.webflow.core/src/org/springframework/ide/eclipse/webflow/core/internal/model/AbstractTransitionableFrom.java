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
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractTransitionableFrom extends
		AbstractTransitionableTo implements ITransitionableFrom {

	/**
	 * The output transitions.
	 */
	private List<ITransition> outputTransitions = new ArrayList<ITransition>();

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.outputTransitions = new ArrayList<ITransition>();

		if (node != null) {
			NodeList children = node.getChildNodes();
			if (children != null && children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					IDOMNode child = (IDOMNode) children.item(i);
					if ("transition".equals(child.getLocalName())) {
						IStateTransition trans = new StateTransition(
								(IWebflowState) parent);
						trans.init(child, this);
						this.outputTransitions.add(trans);
					}
				}
			}
		}
	}

	/**
	 * Gets the output transitions.
	 * 
	 * @return the output transitions
	 */
	public List<ITransition> getOutputTransitions() {
		return outputTransitions;
	}

	/**
	 * Adds the output transition.
	 * 
	 * @param transitions the transitions
	 */
	public void addOutputTransition(ITransition transitions) {
		this.outputTransitions.add(transitions);
		if (getNode() != null) {
			getNode().appendChild(transitions.getNode());
		}
		super.fireStructureChange(OUTPUTS, transitions);
	}

	/**
	 * Removes the output transition.
	 * 
	 * @param transitions the transitions
	 */
	public void removeOutputTransition(ITransition transitions) {
		this.outputTransitions.remove(transitions);
		if (getNode() != null) {
			getNode().removeChild(transitions.getNode());
		}
		super.fireStructureChange(OUTPUTS, transitions);
	}

}