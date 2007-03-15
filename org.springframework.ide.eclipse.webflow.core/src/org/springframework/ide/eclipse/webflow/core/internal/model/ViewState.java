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

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
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
}