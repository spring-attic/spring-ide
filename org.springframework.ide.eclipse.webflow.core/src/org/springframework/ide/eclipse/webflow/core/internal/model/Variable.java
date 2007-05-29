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
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Variable extends AbstractModelElement implements IVar {

	public String getBean() {
		return getAttribute("bean");
	}

	public String getClazz() {
		return getAttribute("class");
	}

	public String getName() {
		return getAttribute("name");
	}

	public String getScope() {
		return getAttribute("scope");
	}

	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	public void setClazz(String clazz) {
		setAttribute("class", clazz);
	}

	public void setName(String name) {
		setAttribute("name", name);
	}

	public void setScope(String scope) {
		setAttribute("scope", scope);
	} 

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}
	
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("var");
		init(node, parent);
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}
}
