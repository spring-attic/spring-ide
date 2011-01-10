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
public interface IInputMapper extends IWebflowModelElement {

	/**
	 * Gets the input attributes.
	 * 
	 * @return the input attributes
	 */
	List<IInputAttribute> getInputAttributes();

	/**
	 * Adds the input attribute.
	 * 
	 * @param action the action
	 */
	void addInputAttribute(IInputAttribute action);

	/**
	 * Adds the input attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addInputAttribute(IInputAttribute action, int i);

	/**
	 * Removes the input attribute.
	 * 
	 * @param action the action
	 */
	void removeInputAttribute(IInputAttribute action);

	/**
	 * Removes the all input attribute.
	 */
	void removeAllInputAttribute();

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
	 * Gets the mapping.
	 * 
	 * @return the mapping
	 */
	List<IMapping> getMapping();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);
}
