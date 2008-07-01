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
public interface IStateTransition extends ITransition {

	/**
	 * Gets the from state.
	 * 
	 * @return the from state
	 */
	ITransitionableFrom getFromState();

	/**
	 * Sets the from state.
	 * 
	 * @param formState the from state
	 */
	void setFromState(ITransitionableFrom formState);

	/**
	 * Sets the on.
	 * 
	 * @param on the on
	 */
	void setOn(String on);

	/**
	 * Gets the on.
	 * 
	 * @return the on
	 */
	String getOn();

	/**
	 * Gets the actions.
	 * 
	 * @return the actions
	 */
	List<IActionElement> getActions();

	/**
	 * Adds the action.
	 * 
	 * @param action the action
	 */
	void addAction(IActionElement action);

	/**
	 * Adds the action.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addAction(IActionElement action, int i);

	/**
	 * Removes the action.
	 * 
	 * @param action the action
	 */
	void removeAction(IActionElement action);

	/**
	 * Removes the all.
	 */
	void removeAll();

	/**
	 * Creates the new.
	 * 
	 * @param webflowState the webflow state
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent, IWebflowState webflowState);

	String getOnException();

	void setOnException(String exception);

	String getToStateId();
	
	String getBind();
	
	void setBind(String bind);

	String getHistory();
	
	void setHistory(String history);
}
