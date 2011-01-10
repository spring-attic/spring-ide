/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Attribute extends AbstractModelElement implements IAttribute {

	/**
	 * 
	 * 
	 * @return
	 */
	public String getName() {
		return getAttribute("name");
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getType() {
		return getAttribute("type");
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getValue() {
		return getAttribute("value");
	}

	/**
	 * 
	 * 
	 * @param name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/**
	 * 
	 * 
	 * @param type
	 */
	public void setType(String type) {
		setAttribute("type", type);
	}

	/**
	 * 
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		setAttribute("value", value);
	}

	/**
	 * 
	 * 
	 * @param parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("attribute");
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
}
