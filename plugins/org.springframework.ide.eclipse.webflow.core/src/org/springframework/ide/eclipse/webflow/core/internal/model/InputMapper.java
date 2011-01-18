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
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class InputMapper extends AbstractModelElement implements IInputMapper {

	private List<IInputAttribute> inputAttributes = null;

	private List<IMapping> mappings = null;

	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.inputAttributes = new ArrayList<IInputAttribute>();
		this.mappings = new ArrayList<IMapping>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("input-attribute".equals(child.getLocalName())) {
					InputAttribute attr = new InputAttribute();
					attr.init(child, this);
					this.inputAttributes.add(attr);
				}
				else if ("input".equals(child.getLocalName())) {
					InputAttribute attr = new InputAttribute();
					attr.init(child, this);
					this.inputAttributes.add(attr);
				}
				else if ("mapping".equals(child.getLocalName())) {
					Mapping map = new Mapping();
					map.init(child, this);
					this.mappings.add(map);
				}
			}
		}
	}

	public List<IInputAttribute> getInputAttributes() {
		return this.inputAttributes;
	}

	public List<IMapping> getMapping() {
		return this.mappings;
	}

	public void addInputAttribute(IInputAttribute action) {
		if (!this.inputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.inputAttributes.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.inputAttributes.indexOf(action)), action);
		}
	}

	public void addInputAttribute(IInputAttribute action, int i) {
		if (!this.inputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.inputAttributes.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.inputAttributes.indexOf(action)), action);
		}
	}

	public void removeAllInputAttribute() {
		for (IInputAttribute action : this.inputAttributes) {
			getNode().removeChild(action.getNode());
		}
		this.inputAttributes = new ArrayList<IInputAttribute>();
	}

	public void removeInputAttribute(IInputAttribute action) {
		if (this.inputAttributes.contains(action)) {
			this.inputAttributes.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	public void addMapping(IMapping action) {
		if (!this.mappings.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.mappings.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.mappings
					.indexOf(action)), action);
		}
	}

	public void addMapping(IMapping action, int i) {
		if (!this.mappings.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.mappings.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.mappings
					.indexOf(action)), action);
		}
	}

	public void removeAllMapping() {
		for (IMapping action : this.mappings) {
			getNode().removeChild(action.getNode());
		}
		this.mappings = new ArrayList<IMapping>();
	}

	public void removeMapping(IMapping action) {
		if (this.mappings.contains(action)) {
			this.mappings.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("input-mapper");
		init(node, parent);
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
			for (IInputAttribute state : getInputAttributes()) {
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
		children.addAll(getInputAttributes());
		return children.toArray(new IModelElement[children.size()]);
	}
}
