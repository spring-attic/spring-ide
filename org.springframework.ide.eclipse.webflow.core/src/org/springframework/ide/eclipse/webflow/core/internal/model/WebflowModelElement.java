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
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySource;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class WebflowModelElement implements IWebflowModelElement,
		IAdaptable {

	protected IDOMNode node = null;

	protected IWebflowModelElement parent;

	protected WebflowModelElement() {
	}

	transient protected PropertyChangeSupport listeners = new PropertyChangeSupport(
			this);

	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
		listeners.firePropertyChange(prop, old, newValue);
	}

	public void firePropertyChange(String prop) {
		listeners.firePropertyChange(prop, "old", "newValue");
	}

	public void fireStructureChange(String prop, Object child) {
		listeners.firePropertyChange(prop, null, child);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.removePropertyChangeListener(l);
	}

	protected String getAttribute(String attributeName) {
		return getAttribute(this.node, attributeName);
	}

	public String getAttribute(IDOMNode node, String attributeName) {
		return BeansEditorUtils.getAttribute(node, attributeName);
	}

	protected void setAttribute(String attributeName, String value) {
		setAttribute(this.node, attributeName, value);
	}

	protected void setAttribute(IDOMNode node, String attributeName,
			String value) {

		if (!StringUtils.hasText(value)) {
			value = null;
		}

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

	protected List<IDOMNode> getChildrenNodeByTagName(String tagName) {
		List<IDOMNode> nodes = new ArrayList<IDOMNode>();
		if (this.node != null) {
			NodeList children = this.node.getChildNodes();
			if (children != null && children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					if (children.item(i).getLocalName() != null
							&& children.item(i).getLocalName().equals(tagName)) {
						nodes.add((IDOMNode) children.item(i));
					}
				}
			}
		}
		return nodes;
	}

	public IDOMNode getNode() {
		return this.node;
	}

	public IModelElement getElementParent() {
		return this.parent;
	}

	public void setElementParent(IWebflowModelElement parent) {
		this.parent = parent;
	}

	public void init(IDOMNode node, IWebflowModelElement parent) {
		this.node = node;
		this.parent = parent;
	}

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
		if (node != null) {
			IStructuredDocument doc = node.getStructuredDocument();
			if (doc != null) {
				return doc.getLineOfOffset(node.getStartOffset()) + 1;
			}
		}
		return -1;
	}

	public int getElementEndLine() {
		return getElementStartLine();
	}

	public IResourceModelElement getElementSourceElement() {
		return null;
	}

	public IModelSourceLocation getElementSourceLocation() {
		return null;
	}

}
