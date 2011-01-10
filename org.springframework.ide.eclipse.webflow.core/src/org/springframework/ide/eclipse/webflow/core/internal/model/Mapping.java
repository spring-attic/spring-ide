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
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Mapping extends AbstractModelElement implements IMapping {

	public String getName() {
		return getAttribute("name");
	}

	public boolean getRequired() {
		return Boolean.valueOf(getAttribute("required"));
	}

	public String getScope() {
		return getAttribute("scope");
	}

	public void setName(String name) {
		setAttribute("name", name);
	}

	public void setRequired(boolean required) {
		if (required) {
			setAttribute("required", "true");
		}
		else {
			setAttribute("required", "false");
		}
	}

	public void setScope(String scope) {
		setAttribute("scope", scope);
	}

	public String getFrom() {
		return getAttribute("from");
	}

	public String getSource() {
		return getAttribute("source");
	}

	public String getTarget() {
		return getAttribute("target");
	}

	public String getTargetCollection() {
		return getAttribute("target-collection");
	}

	public String getTo() {
		return getAttribute("to");
	}

	public void setFrom(String value) {
		setAttribute("from", value);
	}

	public void setSource(String value) {
		setAttribute("source", value);
	}

	public void setTarget(String value) {
		setAttribute("target", value);
	}

	public void setTargetCollection(String value) {
		setAttribute("target-collection", value);
	}

	public void setTo(String value) {
		setAttribute("to", value);
	}

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("mapping");
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
