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
public interface IDescriptionEnabled extends IWebflowModelElement {

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	String getDescription();

	/**
	 * Sets the description.
	 * 
	 * @param desc the description
	 */
	void setDescription(String desc);

	/**
	 * Checks for description.
	 * 
	 * @return true, if has description
	 */
	boolean hasDescription();

}
