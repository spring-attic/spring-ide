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
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class StateTransition extends Transition implements IStateTransition,
		ICloneableModelElement<IStateTransition> {

	/**
	 * The actions.
	 */
	private List<IActionElement> actions = null;

	/**
	 * The Constructor.
	 */
	public StateTransition() {
		super(null);
	}

	/**
	 * The Constructor.
	 * 
	 * @param webflowState the webflow state
	 */
	public StateTransition(IWebflowState webflowState) {
		super(webflowState);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.AbstractModelElement#init(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
	 * org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param node 
	 * @param parent 
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.actions = new ArrayList<IActionElement>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("action".equals(child.getLocalName())) {
					Action action = new Action();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ACTION);
					this.actions.add(action);
				}
				else if ("bean-action".equals(child.getLocalName())) {
					BeanAction action = new BeanAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ACTION);
					this.actions.add(action);
				}
				else if ("evaluate-action".equals(child.getLocalName())) {
					EvaluateAction action = new EvaluateAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ACTION);
					this.actions.add(action);
				}
				else if ("set".equals(child.getLocalName())) {
					Set action = new Set();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ACTION);
					this.actions.add(action);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#addAction(org.springframework.ide.eclipse.webflow.core.model.IActionElement)
	 */
	/**
	 * 
	 * 
	 * @param action 
	 */
	public void addAction(IActionElement action) {
		if (!this.actions.contains(action)) {
			this.actions.add(action);
			WebflowModelUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.actions
					.indexOf(action)), action);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.flow.core.model.IAction,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#addAction(org.springframework.ide.eclipse.webflow.core.model.IActionElement,
	 * int)
	 */
	/**
	 * 
	 * 
	 * @param i 
	 * @param action 
	 */
	public void addAction(IActionElement action, int i) {
		if (!this.actions.contains(action)) {
			this.actions.add(i, action);
			WebflowModelUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#getActions()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#getActions()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public List<IActionElement> getActions() {
		return this.actions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#getOn()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getOn() {
		return getAttribute("on");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#removeAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#removeAction(org.springframework.ide.eclipse.webflow.core.model.IActionElement)
	 */
	/**
	 * 
	 * 
	 * @param action 
	 */
	public void removeAction(IActionElement action) {
		if (this.actions.contains(action)) {
			this.actions.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#setOn(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param on 
	 */
	public void setOn(String on) {
		setAttribute("on", on);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#getFromState()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#getFromState()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public ITransitionableFrom getFromState() {
		return (ITransitionableFrom) this.parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ITransition#setFromState(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#setFromState(org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom)
	 */
	/**
	 * 
	 * 
	 * @param fromState 
	 */
	public void setFromState(ITransitionableFrom fromState) {
		Node parent = this.node.getParentNode();
		if (parent != null) {
			parent.removeChild(this.node);
		}
		WebflowModelUtils.insertNode(getNode(), fromState.getNode());
		this.parent = fromState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelElement#getElementParent()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	@Override
	public IWebflowModelElement getElementParent() {
		return this.parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#createNew(org.springframework.ide.eclipse.webflow.core.model.IState,
	 * org.springframework.ide.eclipse.webflow.core.model.IWebflowState)
	 */
	/**
	 * 
	 * 
	 * @param webflowState 
	 * @param parent 
	 */
	public void createNew(IState parent, IWebflowState webflowState) {
		this.webflowState = webflowState;
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("transition");
		init(node, parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement#cloneModelElement()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public IStateTransition cloneModelElement() {
		StateTransition state = new StateTransition(this.webflowState);
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param element 
	 */
	public void applyCloneValues(IStateTransition element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setOn(element.getOn());
			init(element.getNode(), parent);
			WebflowModelUtils.removeTextChildren(getNode());
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
		}
	}

	/**
	 * Inits the from clone.
	 * 
	 * @param cloneNode the clone node
	 */
	public void initFromClone(IDOMNode cloneNode) {

		removeAll();

		List<Node> nodesToAdd = new ArrayList<Node>();

		IDOMNode newNode = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("transition");
		setAttribute(newNode, "on", getAttribute(cloneNode, "on"));
		setAttribute(newNode, "to", getAttribute(cloneNode, "to"));

		NodeList children = cloneNode.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				nodesToAdd.add(child);

				if ("action".equals(child.getLocalName())) {
					Action action = new Action();
					action.init(child, this);
					this.actions.add(action);
				}
				else if ("bean-action".equals(child.getLocalName())) {
					BeanAction action = new BeanAction();
					action.init(child, this);
					this.actions.add(action);
				}
				else if ("evaluate-action".equals(child.getLocalName())) {
					EvaluateAction action = new EvaluateAction();
					action.init(child, this);
					this.actions.add(action);
				}
				else if ("set".equals(child.getLocalName())) {
					Set action = new Set();
					action.init(child, this);
					this.actions.add(action);
				}
			}
		}

		for (Node n : nodesToAdd) {
			if (n.getParentNode() != null) {
				n.getParentNode().removeChild(n);
			}
			newNode.appendChild(n);
		}
		if (getNode().getParentNode() != null) {
			parent.getNode().replaceChild(newNode, getNode());
		}
		WebflowModelUtils.removeTextChildren(newNode);
		node = newNode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IStateTransition#removeAll()
	 */
	/**
	 * 
	 */
	public void removeAll() {
		for (IActionElement action : this.actions) {
			getNode().removeChild(action.getNode());
		}
		this.actions = new ArrayList<IActionElement>();
	}
}