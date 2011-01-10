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
public interface IMapping extends IWebflowModelElement {

	/**
	 * Sets the source.
	 * 
	 * @param value the source
	 */
	void setSource(String value);

	/**
	 * Sets the target.
	 * 
	 * @param value the target
	 */
	void setTarget(String value);

	/**
	 * Sets the target collection.
	 * 
	 * @param value the target collection
	 */
	void setTargetCollection(String value);

	/**
	 * Sets the from.
	 * 
	 * @param value the from
	 */
	void setFrom(String value);

	/**
	 * Sets the to.
	 * 
	 * @param value the to
	 */
	void setTo(String value);

	/**
	 * Sets the required.
	 * 
	 * @param value the required
	 */
	void setRequired(boolean value);

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	String getSource();

	/**
	 * Gets the target.
	 * 
	 * @return the target
	 */
	String getTarget();

	/**
	 * Gets the target collection.
	 * 
	 * @return the target collection
	 */
	String getTargetCollection();

	/**
	 * Gets the from.
	 * 
	 * @return the from
	 */
	String getFrom();

	/**
	 * Gets the to.
	 * 
	 * @return the to
	 */
	String getTo();

	/**
	 * Gets the required.
	 * 
	 * @return the required
	 */
	boolean getRequired();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);

}
