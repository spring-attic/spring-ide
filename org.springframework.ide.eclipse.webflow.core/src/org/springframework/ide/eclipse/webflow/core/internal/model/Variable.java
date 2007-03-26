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
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Variable extends AbstractModelElement implements IVar {

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	public String getBean() {
		return getAttribute("bean");
	}

	/**
	 * Gets the clazz.
	 * 
	 * @return the clazz
	 */
	public String getClazz() {
		return getAttribute("class");
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return getAttribute("name");
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
	 * Sets the bean.
	 * 
	 * @param bean the bean
	 */
	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	/**
	 * Sets the class.
	 * 
	 * @param clazz the class
	 */
	public void setClazz(String clazz) {
		setAttribute("class", clazz);
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	public void setScope(String scope) {
		setAttribute("scope", scope);
	} 

	public void accept(IWebflowModelElementVisitor visitor,
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
				.createElement("var");
		init(node, parent);
	}
}
