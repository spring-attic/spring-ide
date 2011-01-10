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
public interface IImport extends IWebflowModelElement {

	/**
	 * Sets the resource.
	 * 
	 * @param resource the resource
	 */
	void setResource(String resource);

	/**
	 * Gets the resource.
	 * 
	 * @return the resource
	 */
	String getResource();
	
	void createNew(IWebflowModelElement parent);

}
