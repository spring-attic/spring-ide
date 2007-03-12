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
public class EntryActions extends WebflowModelElement implements IEntryActions {

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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
	/**
	 * Adds the entry action.
	 * 
	 * @param action the action
	 */
	public void addEntryAction(IActionElement action) {
		if (!this.entryActions.contains(action)) {
			WebflowModelUtils.insertNode(action.getNode(), node);
			this.entryActions.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.entryActions.indexOf(action)), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#removeAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.flow.core.model.IAction,
	 * int)
	 */
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
				WebflowModelUtils.insertBefore(action.getNode(), ref.getNode());
			}
			else {
				WebflowModelUtils.insertNode(action.getNode(), node);
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

}
