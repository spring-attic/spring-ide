/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.ISecured;
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
	
	private ISecured secured;

	/**
	 * @return the secured
	 */
	public ISecured getSecured() {
		return secured;
	}

	/**
	 * @param secured the secured to set
	 */
	public void setSecured(ISecured secured) {
		this.secured = secured;
	}

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
					else if ("secured".equals(child.getLocalName())) {
						ISecured secured = new Secured();
						secured.init(child, this);
						this.secured = secured;
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
