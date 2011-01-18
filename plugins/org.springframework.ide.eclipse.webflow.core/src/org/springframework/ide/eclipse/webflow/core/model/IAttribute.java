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
public interface IAttribute extends IWebflowModelElement {

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	void setName(String name);

	/**
	 * Sets the value.
	 * 
	 * @param value the value
	 */
	void setValue(String value);

	/**
	 * Sets the type.
	 * 
	 * @param type the type
	 */
	void setType(String type);

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	String getType();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);

}
