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
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
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
public class EntryActions extends AbstractModelElement implements IEntryActions {

	/**
	 * The entry actions.
	 */
	private List<IActionElement> entryActions = null;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		this.entryActions = new ArrayList<IActionElement>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("action".equals(child.getLocalName())) {
					Action action = new Action();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ENTRY_ACTION);
					this.entryActions.add(action);
				}
				else if ("bean-action".equals(child.getLocalName())) {
					BeanAction action = new BeanAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ENTRY_ACTION);
					this.entryActions.add(action);
				}
				else if ("evaluate-action".equals(child.getLocalName())) {
					EvaluateAction action = new EvaluateAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ENTRY_ACTION);
					this.entryActions.add(action);
				}
				else if ("set".equals(child.getLocalName())) {
					Set action = new Set();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.ENTRY_ACTION);
					this.entryActions.add(action);
				}
			}
		}
	}

	/**
	 * Adds the entry action.
	 * 
	 * @param action the action
	 */
	public void addEntryAction(IActionElement action) {
		if (!this.entryActions.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.entryActions.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.entryActions.indexOf(action)), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/**
	 * Removes the entry action.
	 * 
	 * @param action the action
	 */
	public void removeEntryAction(IActionElement action) {
		if (this.entryActions.contains(action)) {
			this.entryActions.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/**
	 * Adds the entry action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addEntryAction(IActionElement action, int i) {
		if (!this.entryActions.contains(action)) {
			if (this.entryActions.size() > i) {
				IActionElement ref = this.entryActions.get(i);
				WebflowModelXmlUtils.insertBefore(action.getNode(), ref
						.getNode());
			}
			else {
				WebflowModelXmlUtils.insertNode(action.getNode(), node);
			}
			this.entryActions.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/**
	 * Gets the entry actions.
	 * 
	 * @return the entry actions
	 */
	public List<IActionElement> getEntryActions() {
		return this.entryActions;
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		if (parent instanceof IWebflowState) {
			IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
					.createElement("start-actions");
			init(node, parent);
		}
		else {
			IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
					.createElement("entry-actions");
			init(node, parent);
		}
	}

	/**
	 * Removes the all.
	 */
	public void removeAll() {
		for (IActionElement action : this.entryActions) {
			getNode().removeChild(action.getNode());
		}
		this.entryActions = new ArrayList<IActionElement>();
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IActionElement state : getEntryActions()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getEntryActions());
		return children.toArray(new IModelElement[children.size()]);
	}
}
