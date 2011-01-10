/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ViewState extends AbstractTransitionableFrom implements
		IViewState, ICloneableModelElement<IViewState> {

	/**
	 * The render actions.
	 */
	private IRenderActions renderActions = null;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		this.renderActions = null;

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("render-actions".equals(child.getLocalName())) {
					this.renderActions = new RenderActions();
					this.renderActions.init(child, this);
				}
				else if ("on-render".equals(child.getLocalName())) {
					this.renderActions = new RenderActions();
					this.renderActions.init(child, this);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.IViewState#getView()
	 */
	/**
	 * Gets the view.
	 * 
	 * @return the view
	 */
	public String getView() {
		return getAttribute("view");
	}

	/**
	 * Sets the view.
	 * 
	 * @param view the view
	 */
	public void setView(String view) {
		setAttribute("view", view);
	}

	/**
	 * Gets the render actions.
	 * 
	 * @return the render actions
	 */
	public IRenderActions getRenderActions() {
		return this.renderActions;
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("view-state");
		init(node, parent);
	}

	/**
	 * Clone model element.
	 * 
	 * @return the i view state
	 */
	public IViewState cloneModelElement() {
		ViewState state = new ViewState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(IViewState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setId(element.getId());
			setView(element.getView());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
	}

	/**
	 * Sets the render actions.
	 * 
	 * @param renderActions the render actions
	 */
	public void setRenderActions(IRenderActions renderActions) {
		if (this.renderActions != null) {
			getNode().removeChild(this.renderActions.getNode());
		}
		this.renderActions = renderActions;
		if (renderActions != null) {
			WebflowModelXmlUtils.insertNode(renderActions.getNode(), getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
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
			if (getRenderActions() != null) {
				getRenderActions().accept(visitor, monitor);
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
		children.add(getExitActions());
		children.add(getRenderActions());
		children.addAll(getExceptionHandlers());
		children.addAll(getOutputTransitions());
		return children.toArray(new IModelElement[children.size()]);
	}

	public String getModel() {
		return getAttribute("model");
	}

	public String getPopup() {
		return getAttribute("popup");
	}

	public String getRedirect() {
		return getAttribute("redirect");
	}

	public void setModel(String model) {
		setAttribute("model", model);
	}

	public void setPopup(String popup) {
		setAttribute("popup", popup);
	}

	public void setRedirect(String redirect) {
		setAttribute("redirect", redirect);
	}
}
