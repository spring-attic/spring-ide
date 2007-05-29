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
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Import extends AbstractModelElement implements IImport {

	/**
	 * Gets the resource.
	 * 
	 * @return the resource
	 */
	public String getResource() {
		return getAttribute("resource");
	}

	/**
	 * Sets the resource.
	 * 
	 * @param resource the resource
	 */
	public void setResource(String resource) {
		setAttribute("resource", resource);
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}
	
	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("import");
		init(node, parent);
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}
}
