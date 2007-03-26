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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.ISet;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Set extends AbstractAction implements ISet,
		ICloneableModelElement<Set> {

	/**
	 * Gets the attribute.
	 * 
	 * @return the attribute
	 */
	public String getAttribute() {
		return getAttribute("attribute");
	}

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	public String getScope() {
		return getAttribute("scope");
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return getAttribute("value");
	}

	/**
	 * Sets the attribute.
	 * 
	 * @param attribute the attribute
	 */
	public void setAttribute(String attribute) {
		setAttribute("attribute", attribute);
	}

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	public void setScope(String scope) {
		setAttribute("scope", scope);
	}

	/**
	 * Sets the value.
	 * 
	 * @param value the value
	 */
	public void setValue(String value) {
		setAttribute("value", value);
	}

	/**
	 * Clone model element.
	 * 
	 * @return the set
	 */
	public Set cloneModelElement() {
		Set state = new Set();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		state.setType(getType());
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(Set element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setType(element.getType());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
			super.firePropertyChange(PROPS);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("set");
		init(node, parent);
	}

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}
}
