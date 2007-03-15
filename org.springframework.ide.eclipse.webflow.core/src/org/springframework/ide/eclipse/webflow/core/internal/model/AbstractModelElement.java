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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractModelElement extends WebflowModelElement
		implements IAttributeEnabled {

	/**
	 * The attributes.
	 */
	protected List<IAttribute> attributes;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.attributes = new ArrayList<IAttribute>();

		if (node != null) {
			NodeList children = node.getChildNodes();
			if (children != null && children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					IDOMNode child = (IDOMNode) children.item(i);
					if ("attribute".equals(child.getLocalName())) {
						Attribute p = new Attribute();
						p.init(child, this);
						this.attributes.add(p);
					}
				}
			}
		}
	}

	/**
	 * Adds the attribute.
	 * 
	 * @param property the property
	 */
	public void addAttribute(IAttribute property) {
		if (!this.attributes.contains(property)) {
			WebflowModelXmlUtils.insertNode(property.getNode(), getNode());
			this.attributes.add(property);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.attributes
					.indexOf(property)), property);
		}
	}

	/**
	 * Adds the attribute.
	 * 
	 * @param index the index
	 * @param property the property
	 */
	public void addAttribute(IAttribute property, int index) {
		if (!this.attributes.contains(property)) {
			this.attributes.add(index, property);
			WebflowModelXmlUtils.determineNodeToInsert(property.getNode(),
					getNode());
			super
					.firePropertyChange(ADD_CHILDREN, new Integer(index),
							property);
		}
	}

	/**
	 * Gets the attributes.
	 * 
	 * @return the attributes
	 */
	public List<IAttribute> getAttributes() {
		return this.attributes;
	}

	/**
	 * Removes the attribute.
	 * 
	 * @param property the property
	 */
	public void removeAttribute(IAttribute property) {
		if (this.attributes.contains(property)) {
			this.attributes.remove(property);
			getNode().removeChild(property.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, property);
		}
	}

	/**
	 * Adds the property.
	 * 
	 * @param value the value
	 * @param name the name
	 */
	public void addProperty(String name, String value) {

	}
}
