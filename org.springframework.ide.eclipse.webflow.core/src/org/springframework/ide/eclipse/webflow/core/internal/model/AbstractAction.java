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

import org.springframework.ide.eclipse.webflow.core.model.IAction;
import org.springframework.ide.eclipse.webflow.core.model.IBeanReference;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractAction extends AbstractActionElement implements
		IAction, IBeanReference {

	/**
	 * 
	 */
	private ACTION_TYPE type;

	/**
	 * 
	 * 
	 * @param type
	 */
	public void setType(ACTION_TYPE type) {
		this.type = type;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public ACTION_TYPE getType() {
		return this.type;
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
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	public String getBean() {
		return getAttribute("bean");
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public String getMethod() {
		return getAttribute("method");
	}

	/**
	 * Checks for bean reference.
	 * 
	 * @return true, if has bean reference
	 */
	public boolean hasBeanReference() {
		return getBean() != null;
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
	 * Sets the method.
	 * 
	 * @param method the method
	 */
	public void setMethod(String method) {
		setAttribute("method", method);
	}
}
