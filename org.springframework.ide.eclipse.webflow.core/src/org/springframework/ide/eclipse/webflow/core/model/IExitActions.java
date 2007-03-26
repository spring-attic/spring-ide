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
public interface IExitActions extends IWebflowModelElement {

	/**
	 * Gets the exit actions.
	 * 
	 * @return the exit actions
	 */
	List<IActionElement> getExitActions();

	/**
	 * Adds the exit action.
	 * 
	 * @param action the action
	 */
	void addExitAction(IActionElement action);

	/**
	 * Adds the exit action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addExitAction(IActionElement action, int i);

	/**
	 * Removes the exit action.
	 * 
	 * @param action the action
	 */
	void removeExitAction(IActionElement action);

	/**
	 * Removes the all.
	 */
	void removeAll();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);
}
