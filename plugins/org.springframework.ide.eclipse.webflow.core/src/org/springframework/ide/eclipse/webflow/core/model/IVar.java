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
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IVar extends IWebflowModelElement {

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	void setName(String name);

	/**
	 * Sets the class.
	 * 
	 * @param clazz the class
	 */
	void setClazz(String clazz);

	/**
	 * Sets the bean.
	 * 
	 * @param bean the bean
	 */
	void setBean(String bean);

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	void setScope(String scope);

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Gets the clazz.
	 * 
	 * @return the clazz
	 */
	String getClazz();

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	String getBean();

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	String getScope();
	
	void createNew(IWebflowModelElement parent); 

}
