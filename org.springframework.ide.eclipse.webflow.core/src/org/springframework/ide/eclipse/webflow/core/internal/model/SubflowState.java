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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class SubflowState extends AbstractTransitionableFrom implements
		ISubflowState, ICloneableModelElement<ISubflowState> {

	/**
	 * The attribute mapper.
	 */
	private IAttributeMapper attributeMapper;

	/**
	 * @param node
	 * @param parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.attributeMapper = null;
		
		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("attribute-mapper".equals(child.getLocalName())) {
					this.attributeMapper = new AttributeMapper();
					this.attributeMapper.init(child, this);
				}
			}
		}
	}

	/**
	 * @return
	 */
	public IAttributeMapper getAttributeMapper() {
		return this.attributeMapper;
	}

	/**
	 * @param attributeMapper
	 */
	public void setAttributeMapper(IAttributeMapper attributeMapper) {
		IAttributeMapper oldValue = this.attributeMapper;
		if (this.attributeMapper != null) {
			getNode().removeChild(this.attributeMapper.getNode());
		}
		this.attributeMapper = attributeMapper;
		if (attributeMapper != null) {
			WebflowModelXmlUtils.insertNode(attributeMapper.getNode(),
					getNode());
		}
		super.firePropertyChange(ADD_CHILDREN, new Integer(0), oldValue);
	}

	/**
	 * 
	 */
	public void removeAttributeMapper() {
		IAttributeMapper oldValue = this.attributeMapper;
		if (this.attributeMapper != null) {
			getNode().removeChild(this.attributeMapper.getNode());
		}
		this.attributeMapper = null;
		super.firePropertyChange(REMOVE_CHILDREN, attributeMapper, oldValue);
	}

	/**
	 * @param flow
	 */
	public void setFlow(String flow) {
		setAttribute("flow", flow);
	}

	/**
	 * @return
	 */
	public String getFlow() {
		return getAttribute("flow");
	}

	/**
	 * @param parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("subflow-state");
		init(node, parent);
	}

	/**
	 * @return
	 */
	public ISubflowState cloneModelElement() {
		SubflowState state = new SubflowState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * @param element
	 */
	public void applyCloneValues(ISubflowState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setId(element.getId());
			setFlow(element.getFlow());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
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
			if (getAttributeMapper() != null) {
				getAttributeMapper().accept(visitor, monitor);
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
		children.add(getAttributeMapper());
		children.add(getExitActions());
		children.addAll(getExceptionHandlers());
		children.addAll(getOutputTransitions());
		return children.toArray(new IModelElement[children.size()]);
	}
}
