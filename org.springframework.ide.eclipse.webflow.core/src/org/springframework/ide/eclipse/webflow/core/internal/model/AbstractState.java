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
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractState extends AbstractModelElement implements
		IState {

	/**
	 * The entry actions.
	 */
	protected IEntryActions entryActions = null;

	/**
	 * The exit actions.
	 */
	protected IExitActions exitActions = null;

	/**
	 * The exception handler.
	 */
	protected List<IExceptionHandler> exceptionHandler = null;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.exceptionHandler = new ArrayList<IExceptionHandler>();
		this.entryActions = null;
		this.exitActions = null;

		if (node != null) {
			NodeList children = node.getChildNodes();
			if (children != null && children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					IDOMNode child = (IDOMNode) children.item(i);
					if ("entry-actions".equals(child.getLocalName())) {
						this.entryActions = new EntryActions();
						((EntryActions) this.entryActions).init(child, this);
					}
					else if ("exit-actions".equals(child.getLocalName())) {
						this.exitActions = new ExitActions();
						((ExitActions) this.exitActions).init(child, this);
					}
					else if ("exception-handler".equals(child.getLocalName())) {
						ExceptionHandler handler = new ExceptionHandler();
						handler.init(child, this);
						this.exceptionHandler.add(handler);
					}
				}
			}
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return getAttribute("id");
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the id
	 */
	public void setId(String id) {
		setAttribute("id", id);
	}

	/**
	 * Gets the entry actions.
	 * 
	 * @return the entry actions
	 */
	public IEntryActions getEntryActions() {
		return this.entryActions;
	}

	/**
	 * Gets the exit actions.
	 * 
	 * @return the exit actions
	 */
	public IExitActions getExitActions() {
		return this.exitActions;
	}

	/**
	 * Sets the entry actions.
	 * 
	 * @param entryActions the entry actions
	 */
	public void setEntryActions(IEntryActions entryActions) {
		if (this.entryActions != null) {
			getNode().removeChild(this.entryActions.getNode());
		}
		this.entryActions = entryActions;
		if (entryActions != null) {
			WebflowModelUtils.insertNode(entryActions.getNode(), getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Sets the exit actions.
	 * 
	 * @param exitActions the exit actions
	 */
	public void setExitActions(IExitActions exitActions) {
		if (this.exitActions != null) {
			getNode().removeChild(this.exitActions.getNode());
		}
		this.exitActions = exitActions;
		if (exitActions != null) {
			WebflowModelUtils.insertNode(exitActions.getNode(), getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Gets the element parent.
	 * 
	 * @return the element parent
	 */
	@Override
	public IWebflowModelElement getElementParent() {
		return this.parent;
	}

	/**
	 * Gets the exception handlers.
	 * 
	 * @return the exception handlers
	 */
	public List<IExceptionHandler> getExceptionHandlers() {
		return this.exceptionHandler;
	}

	/**
	 * Adds the exception handler.
	 * 
	 * @param action the action
	 */
	public void addExceptionHandler(IExceptionHandler action) {
		if (!this.exceptionHandler.contains(action)) {
			this.exceptionHandler.add(action);
			WebflowModelUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.exceptionHandler.indexOf(action)), action);
		}
	}

	/**
	 * Adds the exception handler.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addExceptionHandler(IExceptionHandler action, int i) {
		if (!this.exceptionHandler.contains(action)) {
			this.exceptionHandler.add(i, action);
			WebflowModelUtils.insertNode(action.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.exceptionHandler.indexOf(action)), action);
		}
	}

	/**
	 * Removes the exception handler.
	 * 
	 * @param action the action
	 */
	public void removeExceptionHandler(IExceptionHandler action) {
		if (!this.exceptionHandler.contains(action)) {
			this.exceptionHandler.remove(action);
			getNode().removeChild(action.getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.exceptionHandler.indexOf(action)), action);
		}
	}

	/**
	 * Removes the all exception handler.
	 */
	public void removeAllExceptionHandler() {
		for (IExceptionHandler action : this.exceptionHandler) {
			getNode().removeChild(action.getNode());
		}
		this.exceptionHandler = new ArrayList<IExceptionHandler>();
	}
}