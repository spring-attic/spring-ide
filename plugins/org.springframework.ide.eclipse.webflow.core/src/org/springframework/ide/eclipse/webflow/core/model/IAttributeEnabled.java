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

import java.util.List;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IAttributeEnabled extends IWebflowModelElement {

	/**
	 * Adds the attribute.
	 * 
	 * @param property the property
	 */
	void addAttribute(IAttribute property);

	/**
	 * Adds the attribute.
	 * 
	 * @param index the index
	 * @param property the property
	 */
	void addAttribute(IAttribute property, int index);

	/**
	 * Removes the attribute.
	 * 
	 * @param property the property
	 */
	void removeAttribute(IAttribute property);

	/**
	 * Gets the attributes.
	 * 
	 * @return the attributes
	 */
	List<IAttribute> getAttributes();
}
