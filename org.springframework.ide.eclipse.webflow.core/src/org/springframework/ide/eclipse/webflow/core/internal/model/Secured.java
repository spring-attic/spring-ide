/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.ISecured;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.1.0
 */
@SuppressWarnings("restriction")
public class Secured extends AbstractModelElement implements ISecured {

	public String getMatchType() {
		return getAttribute("match-type");
	}

	public void setMatchType(String matchType) {
		setAttribute("match-type", matchType);
	}

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("secured");
		init(node, parent);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}

	public String getRoleAttributes() {
		return getAttribute("attributes");
	}

	public void setRoleAttributes(String attributes) {
		setAttribute("attributes",  attributes);
	}

}
