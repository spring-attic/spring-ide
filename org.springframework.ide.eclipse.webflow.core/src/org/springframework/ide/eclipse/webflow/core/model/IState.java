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
public interface IState extends IAttributeEnabled {

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * Sets the id.
	 * 
	 * @param id the id
	 */
	void setId(String id);

	/**
	 * Gets the entry actions.
	 * 
	 * @return the entry actions
	 */
	IEntryActions getEntryActions();

	/**
	 * Sets the entry actions.
	 * 
	 * @param entryActions the entry actions
	 */
	void setEntryActions(IEntryActions entryActions);

	/**
	 * Sets the exit actions.
	 * 
	 * @param exitActions the exit actions
	 */
	void setExitActions(IExitActions exitActions);

	/**
	 * Gets the exit actions.
	 * 
	 * @return the exit actions
	 */
	IExitActions getExitActions();

	/**
	 * Gets the exception handlers.
	 * 
	 * @return the exception handlers
	 */
	List<IExceptionHandler> getExceptionHandlers();

	/**
	 * Adds the exception handler.
	 * 
	 * @param action the action
	 */
	void addExceptionHandler(IExceptionHandler action);

	/**
	 * Adds the exception handler.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addExceptionHandler(IExceptionHandler action, int i);

	/**
	 * Removes the exception handler.
	 * 
	 * @param action the action
	 */
	void removeExceptionHandler(IExceptionHandler action);

	/**
	 * Removes the all exception handler.
	 */
	void removeAllExceptionHandler();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowState parent);
	
	void setParent(String parent);
	
	String getParent();
}
