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
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IBeanReference extends IWebflowModelElement {

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	String getBean();

	/**
	 * Sets the bean.
	 * 
	 * @param bean the bean
	 */
	void setBean(String bean);

	/**
	 * Checks for bean reference.
	 * 
	 * @return true, if has bean reference
	 */
	boolean hasBeanReference();

	/**
	 * Sets the method.
	 * 
	 * @param method the method
	 */
	void setMethod(String method);

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	String getMethod();
}
