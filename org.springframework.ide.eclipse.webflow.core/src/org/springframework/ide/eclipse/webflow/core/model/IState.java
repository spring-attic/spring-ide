/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
}