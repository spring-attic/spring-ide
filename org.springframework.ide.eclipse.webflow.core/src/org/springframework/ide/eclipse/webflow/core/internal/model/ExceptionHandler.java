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
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ExceptionHandler extends AbstractModelElement implements
		IExceptionHandler, ICloneableModelElement<IExceptionHandler> {

	public String getBean() {
		return getAttribute("bean");
	}

	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("exception-handler");
		init(node, parent);
	}

	public IExceptionHandler cloneModelElement() {
		ExceptionHandler state = new ExceptionHandler();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	public void applyCloneValues(IExceptionHandler element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			init(element.getNode(), parent);
			super.firePropertyChange(PROPS);
		}
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
