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
public interface ISet extends IAction {

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	void setScope(String scope);

	/**
	 * Sets the value.
	 * 
	 * @param value the value
	 */
	void setValue(String value);

	/**
	 * Sets the attribute.
	 * 
	 * @param attribute the attribute
	 */
	void setAttribute(String attribute);

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	String getScope();

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Gets the attribute.
	 * 
	 * @return the attribute
	 */
	String getAttribute();

}
