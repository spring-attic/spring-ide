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
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.ISet;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Set extends AbstractAction implements ISet,
		ICloneableModelElement<Set> {

	public String getAttribute() {
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			return getAttribute("attribute");
		}
		else {
			return getAttribute("name");
		}
	}

	public String getScope() {
		return getAttribute("scope");
	}

	public String getValue() {
		return getAttribute("value");
	}

	public void setAttribute(String attribute) {
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			setAttribute("attribute", attribute);
		}
		else {
			setAttribute("name", attribute);
		}
	}

	public void setScope(String scope) {
		setAttribute("scope", scope);
	}

	public void setValue(String value) {
		setAttribute("value", value);
	}

	public Set cloneModelElement() {
		Set state = new Set();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		state.setType(getType());
		return state;
	}

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

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("set");
		init(node, parent);
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}

	public String getSetType() {
		return getAttribute("type");
	}

	public void setSetType(String type) {
		setAttribute("type", type);
	}
}
