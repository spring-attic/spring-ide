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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySource;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class WebflowModelElement implements IWebflowModelElement,
		IAdaptable {

	/**
	 * The node.
	 */
	protected IDOMNode node = null;

	/**
	 * The parent.
	 */
	protected IWebflowModelElement parent;

	/**
	 * The Constructor.
	 */
	protected WebflowModelElement() {
	}

	/**
	 * The listeners.
	 */
	transient protected PropertyChangeSupport listeners = new PropertyChangeSupport(
			this);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	/**
	 * 
	 * 
	 * @param l
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
	}

	/**
	 * Fire property change.
	 * 
	 * @param newValue the new value
	 * @param old the old
	 * @param prop the prop
	 */
	public void firePropertyChange(String prop, Object old, Object newValue) {
		listeners.firePropertyChange(prop, old, newValue);
	}

	/**
	 * Fire property change.
	 * 
	 * @param prop the prop
	 */
	public void firePropertyChange(String prop) {
		listeners.firePropertyChange(prop, "old", "newValue");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#fireStructureChange(java.lang.String,
	 * java.lang.Object)
	 */
	/**
	 * 
	 * 
	 * @param child
	 * @param prop
	 */
	public void fireStructureChange(String prop, Object child) {
		listeners.firePropertyChange(prop, null, child);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	/**
	 * 
	 * 
	 * @param l
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.removePropertyChangeListener(l);
	}

	/**
	 * Gets the attribute.
	 * 
	 * @param attributeName the attribute name
	 * 
	 * @return the attribute
	 */
	protected String getAttribute(String attributeName) {
		return getAttribute(this.node, attributeName);
	}

	/**
	 * Gets the attribute.
	 * 
	 * @param attributeName the attribute name
	 * @param node the node
	 * 
	 * @return the attribute
	 */
	protected String getAttribute(IDOMNode node, String attributeName) {
		return BeansEditorUtils.getAttribute(node, attributeName);
	}

	/**
	 * Sets the attribute.
	 * 
	 * @param attributeName the attribute name
	 * @param value the value
	 */
	protected void setAttribute(String attributeName, String value) {
		setAttribute(this.node, attributeName, value);
	}

	/**
	 * Sets the attribute.
	 * 
	 * @param attributeName the attribute name
	 * @param value the value
	 * @param node the node
	 */
	protected void setAttribute(IDOMNode node, String attributeName,
			String value) {

		if (!StringUtils.hasText(value)) {
			value = null;
		}

		// String oldValue = BeansEditorUtils.getAttribute(node, attributeName);
		node.getModel().aboutToChangeModel();
		if (value != null) {
			((Element) node).setAttribute(attributeName, value);
		}
		else {
			((Element) node).removeAttribute(attributeName);
		}
		node.getModel().changedModel();
		// we always want to fire a property change event here
		firePropertyChange(PROPS, null, value);
	}

	/**
	 * Gets the children node by tag name.
	 * 
	 * @param tagName the tag name
	 * 
	 * @return the children node by tag name
	 */
	protected List<IDOMNode> getChildrenNodeByTagName(String tagName) {
		List<IDOMNode> nodes = new ArrayList<IDOMNode>();
		NodeList children = this.node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getLocalName() != null
						&& children.item(i).getLocalName().equals(tagName)) {
					nodes.add((IDOMNode) children.item(i));
				}
			}
			return nodes;
		}
		else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#getNode()
	 */
	/**
	 * 
	 * 
	 * @return
	 */
	public IDOMNode getNode() {
		return this.node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#getElementParent()
	 */
	/**
	 * 
	 * 
	 * @return
	 */
	public IWebflowModelElement getElementParent() {
		return this.parent;
	}

	/**
	 * 
	 * 
	 * @param parent
	 */
	public void setElementParent(IWebflowModelElement parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement#init(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
	 * org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param node
	 * @param parent
	 */
	public void init(IDOMNode node, IWebflowModelElement parent) {
		this.node = node;
		this.parent = parent;
	}

	/**
	 * 
	 * 
	 * @param key
	 * 
	 * @return
	 */
	public Object getAdapter(Class key) {
		if (IPropertySource.class == key) {
			if (node instanceof IDOMNode) {
				INodeNotifier source = (INodeNotifier) node;
				IPropertySource propertySource = (IPropertySource) source
						.getAdapterFor(IPropertySource.class);
				if (propertySource == null) {
					propertySource = new XMLPropertySource(
							(INodeNotifier) source);
					return new WebflowElementPropertySource(propertySource);
				}
			}
		}
		return null;
	}

	public int getElementStartLine() {
		return this.node.getStructuredDocument().getLineOfOffset(
				this.node.getStartOffset()) + 1;
	}
}
