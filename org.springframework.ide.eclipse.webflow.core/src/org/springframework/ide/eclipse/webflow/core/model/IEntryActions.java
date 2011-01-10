/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
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
public interface IEntryActions extends IWebflowModelElement {

	/**
	 * Gets the entry actions.
	 * 
	 * @return the entry actions
	 */
	List<IActionElement> getEntryActions();

	/**
	 * Adds the entry action.
	 * 
	 * @param action the action
	 */
	void addEntryAction(IActionElement action);

	/**
	 * Adds the entry action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addEntryAction(IActionElement action, int i);

	/**
	 * Removes the entry action.
	 * 
	 * @param action the action
	 */
	void removeEntryAction(IActionElement action);

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
