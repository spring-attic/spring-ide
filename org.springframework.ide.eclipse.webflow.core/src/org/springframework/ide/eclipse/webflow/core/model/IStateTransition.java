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
	void createNew(IState parent, IWebflowState webflowState);

	String getOnException();

	void setOnException(String exception);

	String getToStateId();
}