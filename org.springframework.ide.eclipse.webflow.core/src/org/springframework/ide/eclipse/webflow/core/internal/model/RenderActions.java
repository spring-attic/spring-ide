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
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class RenderActions extends WebflowModelElement implements
		IRenderActions {

	/**
	 * The render actions.
	 */
	private List<IActionElement> renderActions = null;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.renderActions = new ArrayList<IActionElement>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("action".equals(child.getLocalName())) {
					Action action = new Action();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.RENDER_ACTION);
					this.renderActions.add(action);
				}
				else if ("bean-action".equals(child.getLocalName())) {
					BeanAction action = new BeanAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.RENDER_ACTION);
					this.renderActions.add(action);
				}
				else if ("evaluate-action".equals(child.getLocalName())) {
					EvaluateAction action = new EvaluateAction();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.RENDER_ACTION);
					this.renderActions.add(action);
				}
				else if ("set".equals(child.getLocalName())) {
					Set action = new Set();
					action.init(child, this);
					action.setType(IActionElement.ACTION_TYPE.RENDER_ACTION);
					this.renderActions.add(action);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
	/**
	 * Adds the render action.
	 * 
	 * @param action the action
	 */
	public void addRenderAction(IActionElement action) {
		if (!this.renderActions.contains(action)) {
			this.renderActions.add(action);
			WebflowModelUtils.insertNode(action.getNode(), node);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.renderActions.indexOf(action)), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IActionState#addAction(org.springframework.ide.eclipse.web.flow.core.model.IAction,
	 * int)
	 */
	/**
	 * Adds the render action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addRenderAction(IActionElement action, int i) {
		if (!this.renderActions.contains(action)) {
			if (this.renderActions.size() > i) {
				IActionElement ref = this.renderActions.get(i);
				WebflowModelUtils.insertBefore(action.getNode(), ref.getNode());
			}
			else {
				WebflowModelUtils.insertNode(action.getNode(), node);
			}
			this.renderActions.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#getActions()
	 */
	/**
	 * Gets the render actions.
	 * 
	 * @return the render actions
	 */
	public List<IActionElement> getRenderActions() {
		return this.renderActions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IActionState#removeAction(org.springframework.ide.eclipse.web.core.model.IAction)
	 */
	/**
	 * Removes the render action.
	 * 
	 * @param action the action
	 */
	public void removeRenderAction(IActionElement action) {
		if (this.renderActions.contains(action)) {
			this.renderActions.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("render-actions");
		init(node, parent);
	}

	/**
	 * Removes the all.
	 */
	public void removeAll() {
		for (IActionElement action : this.renderActions) {
			getNode().removeChild(action.getNode());
		}
		this.renderActions = new ArrayList<IActionElement>();
	}
}
