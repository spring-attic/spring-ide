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

import java.util.List;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IOutputMapper extends IWebflowModelElement {

	/**
	 * Gets the output attributes.
	 * 
	 * @return the output attributes
	 */
	List<IOutputAttribute> getOutputAttributes();

	/**
	 * Adds the output attribute.
	 * 
	 * @param action the action
	 */
	void addOutputAttribute(IOutputAttribute action);

	/**
	 * Adds the output attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addOutputAttribute(IOutputAttribute action, int i);

	/**
	 * Removes the output attribute.
	 * 
	 * @param action the action
	 */
	void removeOutputAttribute(IOutputAttribute action);

	/**
	 * Removes the all output attribute.
	 */
	void removeAllOutputAttribute();

	/**
	 * Gets the mapping.
	 * 
	 * @return the mapping
	 */
	List<IMapping> getMapping();

	/**
	 * Adds the mapping.
	 * 
	 * @param action the action
	 */
	void addMapping(IMapping action);

	/**
	 * Adds the mapping.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addMapping(IMapping action, int i);

	/**
	 * Removes the mapping.
	 * 
	 * @param action the action
	 */
	void removeMapping(IMapping action);

	/**
	 * Removes the all mapping.
	 */
	void removeAllMapping();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);
}
