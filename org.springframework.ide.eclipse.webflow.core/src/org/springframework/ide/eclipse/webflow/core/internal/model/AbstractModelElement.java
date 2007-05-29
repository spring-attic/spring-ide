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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.util.ClassUtils;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractModelElement extends WebflowModelElement
		implements IAttributeEnabled {

	private static final Map<Class, Integer> ELEMENT_TYPE_MAPPING;

	static {
		ELEMENT_TYPE_MAPPING = new HashMap<Class, Integer>();
		ELEMENT_TYPE_MAPPING.put(WebflowModel.class, 1);
		ELEMENT_TYPE_MAPPING.put(WebflowProject.class, 2);
		ELEMENT_TYPE_MAPPING.put(WebflowConfig.class, 3);
		ELEMENT_TYPE_MAPPING.put(WebflowState.class, 4);

		ELEMENT_TYPE_MAPPING.put(Action.class, 5);
		ELEMENT_TYPE_MAPPING.put(ActionState.class, 6);
		ELEMENT_TYPE_MAPPING.put(Argument.class, 7);
		ELEMENT_TYPE_MAPPING.put(Attribute.class, 8);
		ELEMENT_TYPE_MAPPING.put(AttributeMapper.class, 9);
		ELEMENT_TYPE_MAPPING.put(BeanAction.class, 10);
		ELEMENT_TYPE_MAPPING.put(DecisionState.class, 11);
		ELEMENT_TYPE_MAPPING.put(EndState.class, 12);
		ELEMENT_TYPE_MAPPING.put(EntryActions.class, 13);
		ELEMENT_TYPE_MAPPING.put(EvaluateAction.class, 14);
		ELEMENT_TYPE_MAPPING.put(EvaluationResult.class, 15);
		ELEMENT_TYPE_MAPPING.put(ExceptionHandler.class, 16);
		ELEMENT_TYPE_MAPPING.put(ExitActions.class, 17);
		ELEMENT_TYPE_MAPPING.put(GlobalTransitions.class, 18);
		ELEMENT_TYPE_MAPPING.put(If.class, 19);
		ELEMENT_TYPE_MAPPING.put(IfTransition.class, 20);
		ELEMENT_TYPE_MAPPING.put(Import.class, 21);
		ELEMENT_TYPE_MAPPING.put(InlineFlowState.class, 22);
		ELEMENT_TYPE_MAPPING.put(InputAttribute.class, 23);
		ELEMENT_TYPE_MAPPING.put(InputMapper.class, 34);
		ELEMENT_TYPE_MAPPING.put(Mapping.class, 25);
		ELEMENT_TYPE_MAPPING.put(MethodArguments.class, 26);
		ELEMENT_TYPE_MAPPING.put(MethodResult.class, 27);
		ELEMENT_TYPE_MAPPING.put(OutputAttribute.class, 28);
		ELEMENT_TYPE_MAPPING.put(RenderActions.class, 29);
		ELEMENT_TYPE_MAPPING.put(Set.class, 30);
		ELEMENT_TYPE_MAPPING.put(StateTransition.class, 31);
		ELEMENT_TYPE_MAPPING.put(SubflowState.class, 32);
		ELEMENT_TYPE_MAPPING.put(Variable.class, 33);
		ELEMENT_TYPE_MAPPING.put(ViewState.class, 34);
	}

	/**
	 * The attributes.
	 */
	protected List<IAttribute> attributes;

	/**
	 * Init.
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
	 * @return the attributes
	 */
	public List<IAttribute> getAttributes() {
		return this.attributes;
	}

	/**
	 * Removes the attribute.
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
	 * @param value the value
	 * @param name the name
	 */
	public void addProperty(String name, String value) {

	}

	/** Character used for delimiting nodes within an element's unique id */
	char ID_DELIMITER = '|';

	/**
	 * Character used separate an element's type and name within an element's
	 * unique id
	 */
	char ID_SEPARATOR = ':';

	public final String getElementID() {
		StringBuffer id = new StringBuffer();
		if (getElementParent() != null) {
			id.append(getElementParent().getElementID());
			id.append(ID_DELIMITER);
		}
		id.append(getElementType());
		id.append(ID_SEPARATOR);
		if (getElementName() != null) {
			id.append(getElementName());
		}
		else {
			id.append(super.hashCode());
		}
		return id.toString();
	}

	/**
	 * Returns the element for the given element ID.
	 * @param id the element's unique ID
	 */
	public IModelElement getElement(String id) {
		int sepPos = id.indexOf(ID_SEPARATOR);
		if (sepPos > 0) {
			try {
				int type = Integer.valueOf(id.substring(0, sepPos)).intValue();
				if (type == getElementType()) {
					int delPos = id.indexOf(ID_DELIMITER);
					if (delPos > 0) {
						String name = id.substring(sepPos + 1, delPos);
						if (name.equals(getElementName())) {

							// Ask children for remaining part of id
							id = id.substring(delPos + 1);
							for (IModelElement child : getElementChildren()) {
								if (child instanceof AbstractModelElement) {
									IModelElement element = ((AbstractModelElement) child)
											.getElement(id);
									if (element != null) {
										return element;
									}
								}
							}
						}
					}
					else {
						String name = id.substring(sepPos + 1);
						if (name.equals(getElementName())) {
							return this;
						}
					}
				}
			}
			catch (NumberFormatException e) {
				// ignore
			}
		}
		return null;
	}

	public int getElementType() {
		return ELEMENT_TYPE_MAPPING.get(getClass());
	}

	public String getElementName() {
		return ClassUtils.getShortName(getClass()) + " ("
				+ getElementStartLine() + ")";
	}
}
