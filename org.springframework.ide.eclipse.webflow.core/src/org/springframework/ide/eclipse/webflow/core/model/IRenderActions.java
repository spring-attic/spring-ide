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
public interface IRenderActions extends IWebflowModelElement {

	/**
	 * Gets the render actions.
	 * 
	 * @return the render actions
	 */
	List<IActionElement> getRenderActions();

	/**
	 * Adds the render action.
	 * 
	 * @param action the action
	 */
	void addRenderAction(IActionElement action);

	/**
	 * Adds the render action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addRenderAction(IActionElement action, int i);

	/**
	 * Removes the render action.
	 * 
	 * @param action the action
	 */
	void removeRenderAction(IActionElement action);

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
