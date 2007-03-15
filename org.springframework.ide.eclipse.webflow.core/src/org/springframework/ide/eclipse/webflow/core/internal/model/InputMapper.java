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
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class InputMapper extends AbstractModelElement implements IInputMapper {

	/**
	 * The input attributes.
	 */
	private List<IInputAttribute> inputAttributes = null;

	/**
	 * The mappings.
	 */
	private List<IMapping> mappings = null;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
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
				else if ("mapping".equals(child.getLocalName())) {
					Mapping map = new Mapping();
					map.init(child, this);
					this.mappings.add(map);
				}
			}
		}
	}

	/**
	 * Gets the input attributes.
	 * 
	 * @return the input attributes
	 */
	public List<IInputAttribute> getInputAttributes() {
		return this.inputAttributes;
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
	 * Adds the input attribute.
	 * 
	 * @param action the action
	 */
	public void addInputAttribute(IInputAttribute action) {
		if (!this.inputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.inputAttributes.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.inputAttributes.indexOf(action)), action);
		}
	}

	/**
	 * Adds the input attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	public void addInputAttribute(IInputAttribute action, int i) {
		if (!this.inputAttributes.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.inputAttributes.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.inputAttributes.indexOf(action)), action);
		}
	}

	/**
	 * Removes the all input attribute.
	 */
	public void removeAllInputAttribute() {
		for (IInputAttribute action : this.inputAttributes) {
			getNode().removeChild(action.getNode());
		}
		this.inputAttributes = new ArrayList<IInputAttribute>();
	}

	/**
	 * Removes the input attribute.
	 * 
	 * @param action the action
	 */
	public void removeInputAttribute(IInputAttribute action) {
		if (this.inputAttributes.contains(action)) {
			this.inputAttributes.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
		}
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

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("input-mapper");
		init(node, parent);
	}

}
