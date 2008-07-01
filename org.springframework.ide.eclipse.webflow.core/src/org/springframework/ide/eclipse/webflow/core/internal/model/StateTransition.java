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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class StateTransition extends Transition implements IStateTransition,
		ICloneableModelElement<IStateTransition> {

	private List<IActionElement> actions = null;

	public StateTransition() {
		super(null);
	}

	public StateTransition(IWebflowState webflowState) {
		super(webflowState);
	}

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
				else if ("render".equals(child.getLocalName())) {
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
				else if ("evaluate".equals(child.getLocalName())) {
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

	public void addAction(IActionElement action) {
		if (!this.actions.contains(action)) {
			this.actions.add(action);
			WebflowModelXmlUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.actions
					.indexOf(action)), action);
		}
	}

	public void addAction(IActionElement action, int i) {
		if (!this.actions.contains(action)) {
			this.actions.add(i, action);
			WebflowModelXmlUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
		}
	}

	public List<IActionElement> getActions() {
		return this.actions;
	}

	public String getOn() {
		return getAttribute("on");
	}

	public void removeAction(IActionElement action) {
		if (this.actions.contains(action)) {
			this.actions.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	public void setOn(String on) {
		setAttribute("on", on);
	}

	public ITransitionableFrom getFromState() {
		return (ITransitionableFrom) this.parent;
	}

	public void setFromState(ITransitionableFrom fromState) {
		Node parent = this.node.getParentNode();
		if (parent != null) {
			parent.removeChild(this.node);
		}
		WebflowModelXmlUtils.insertNode(getNode(), fromState.getNode());
		this.parent = fromState;
	}

	@Override
	public IWebflowModelElement getElementParent() {
		return this.parent;
	}

	public void createNew(IWebflowModelElement parent, IWebflowState webflowState) {
		this.webflowState = webflowState;
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("transition");
		init(node, parent);
	}

	public IStateTransition cloneModelElement() {
		StateTransition state = new StateTransition(this.webflowState);
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	public void applyCloneValues(IStateTransition element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setOn(element.getOn());
			init(element.getNode(), parent);
			WebflowModelXmlUtils.removeTextChildren(getNode());
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
		}
	}

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
				else if ("render".equals(child.getLocalName())) {
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
		WebflowModelXmlUtils.removeTextChildren(newNode);
		node = newNode;
	}

	public void removeAll() {
		for (IActionElement action : this.actions) {
			getNode().removeChild(action.getNode());
		}
		this.actions = new ArrayList<IActionElement>();
	}

	public String getOnException() {
		return getAttribute("on-exception");
	}

	public void setOnException(String exception) {
		setAttribute("on-exception", exception);
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			
			for (IActionElement action : getActions()) {
				if (monitor.isCanceled()) {
					return;
				}
				action.accept(visitor, monitor);
			}
		}
	}

	
	public void setToStateId(String id) {
		setAttribute("to", id);
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getAttributes());
		children.addAll(getActions());
		return children.toArray(new IModelElement[children.size()]);
	}

	public String getBind() {
		return getAttribute("bind");
	}

	public void setBind(String bind) {
		setAttribute("bind", bind);
	}

	public String getHistory() {
		return getAttribute("history");
	}

	public void setHistory(String history) {
		setAttribute("history", history);
	}
}
