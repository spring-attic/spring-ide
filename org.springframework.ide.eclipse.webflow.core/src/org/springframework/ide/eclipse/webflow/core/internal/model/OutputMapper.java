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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class OutputMapper extends AbstractModelElement implements IOutputMapper {

	/**
	 * The output attributes.
	 */
	private List<IOutputAttribute> outputAttributes;

	/**
	 * The mappings.
	 */
	private List<IMapping> mappings;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.outputAttributes = new ArrayList<IOutputAttribute>();
		this.mappings = new ArrayList<IMapping>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("output-attribute".equals(child.getLocalName())) {
					OutputAttribute attr = new OutputAttribute();
					attr.init(child, this);
					this.outputAttributes.add(attr);
				}
				else if ("mapping".equals(child.getLocalName())) {
					Mapping map = new Mapping();
					map.init(child, this);
					this.mappings.add(map);
				}
			}
		}
	}

	/**
	 * Gets the output attributes.
	 * 
	 * @return the output attributes
	 */
	public List<IOutputAttribute> getOutputAttributes() {
		return this.outputAttributes;
	}

	/**
	 * Gets the mapping.
	 * 
	 * @return the mapping
	 */
	public List<IMapping> getMapping() {
		return this.mappings;
	}

	/**
	 * Adds the output attribute.
	 * 
	 * @param action the action
	 */
	public void addOutputAttribute(IOutputAttribute action) {
		if (!this.outputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.outputAttributes.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.outputAttributes.indexOf(action)), action);
		}
	}

	/**
	 * Adds the output attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addOutputAttribute(IOutputAttribute action, int i) {
		if (!this.outputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.outputAttributes.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.outputAttributes.indexOf(action)), action);
		}
	}

	/**
	 * Removes the all output attribute.
	 */
	public void removeAllOutputAttribute() {
		for (IInputAttribute action : this.outputAttributes) {
			getNode().removeChild(action.getNode());
		}
		this.outputAttributes = new ArrayList<IOutputAttribute>();
	}

	/**
	 * Removes the output attribute.
	 * 
	 * @param action the action
	 */
	public void removeOutputAttribute(IOutputAttribute action) {
		if (this.outputAttributes.contains(action)) {
			this.outputAttributes.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("output-mapper");
		init(node, parent);
	}

	/**
	 * Adds the mapping.
	 * 
	 * @param action the action
	 */
	public void addMapping(IMapping action) {
		if (!this.mappings.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.mappings.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.mappings
					.indexOf(action)), action);
		}
	}

	/**
	 * Adds the mapping.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addMapping(IMapping action, int i) {
		if (!this.mappings.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.mappings.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.mappings
					.indexOf(action)), action);
		}
	}

	/**
	 * Removes the all mapping.
	 */
	public void removeAllMapping() {
		for (IMapping action : this.mappings) {
			getNode().removeChild(action.getNode());
		}
		this.mappings = new ArrayList<IMapping>();
	}

	/**
	 * Removes the mapping.
	 * 
	 * @param action the action
	 */
	public void removeMapping(IMapping action) {
		if (this.mappings.contains(action)) {
			this.mappings.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
	}

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			for (IMapping state : getMapping()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			for (IOutputAttribute state : getOutputAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
}
