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
import org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class EvaluationResult extends AbstractModelElement implements
		IEvaluationResult {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult#getName()
	 */
	/**
	 * 
	 * 
	 * @return
	 */
	public String getName() {
		return getAttribute("name");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult#getScope()
	 */
	/**
	 * 
	 * 
	 * @return
	 */
	public String getScope() {
		return getAttribute("scope");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult#setName(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult#setScope(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param scope
	 */
	public void setScope(String scope) {
		setAttribute("scope", scope);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult#createNew(org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("evaluation-result");
		init(node, parent);
	}

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}
}
