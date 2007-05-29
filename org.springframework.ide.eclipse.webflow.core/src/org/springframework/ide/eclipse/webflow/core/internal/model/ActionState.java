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
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ActionState extends AbstractTransitionableFrom implements
		IActionState, ICloneableModelElement<IActionState> {

	/**
	 * The actions.
	 */
	private List<IActionElement> actions = null;

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
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public List<IActionElement> getActions() {
		return this.actions;
	}

	/**
	 * 
	 * 
	 * @param action
	 */
	public void addAction(IActionElement action) {
		if (!this.actions.contains(action)) {
			this.actions.add(action);
			WebflowModelXmlUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.actions
					.indexOf(action)), action);
		}
	}

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

	/**
	 * 
	 * 
	 * @param i
	 * @param action
	 */
	public void addAction(IActionElement action, int i) {
		if (!this.actions.contains(action)) {
			if (this.actions.size() > i) {
				IActionElement ref = this.actions.get(i);
				WebflowModelXmlUtils.insertBefore(action.getNode(), ref
						.getNode());
			}
			else {
				WebflowModelXmlUtils.insertNode(action.getNode(), node);
			}
			this.actions.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
		}
	}

	/**
	 * 
	 * 
	 * @param parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("action-state");
		init(node, parent);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IActionState cloneModelElement() {
		ActionState state = new ActionState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * 
	 * 
	 * @param element
	 */
	public void applyCloneValues(IActionState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setId(element.getId());
			init(element.getNode(), parent);
		}
	}

	/**
	 * 
	 */
	public void removeAll() {
		for (IActionElement action : this.actions) {
			getNode().removeChild(action.getNode());
		}
		this.actions = new ArrayList<IActionElement>();
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
			if (getEntryActions() != null) {
				getEntryActions().accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			for (IActionElement state : getActions()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			if (getExitActions() != null) {
				getExitActions().accept(visitor, monitor);
			}
			for (IExceptionHandler state : getExceptionHandlers()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			for (ITransition state : getOutputTransitions()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getAttributes());
		children.add(getEntryActions());
		children.addAll(getActions());
		children.add(getExitActions());
		children.addAll(getExceptionHandlers());
		children.addAll(getOutputTransitions());
		return children.toArray(new IModelElement[children.size()]);
	}

}
