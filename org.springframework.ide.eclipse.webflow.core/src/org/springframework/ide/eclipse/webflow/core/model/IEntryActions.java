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
