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

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IIf extends IWebflowModelElement {

	/**
	 * Gets the else transition.
	 * 
	 * @return the else transition
	 */
	IIfTransition getElseTransition();

	/**
	 * Gets the test.
	 * 
	 * @return the test
	 */
	String getTest();

	/**
	 * Gets the then transition.
	 * 
	 * @return the then transition
	 */
	IIfTransition getThenTransition();

	/**
	 * Removes the else transition.
	 */
	void removeElseTransition();

	/**
	 * Removes the then transition.
	 */
	void removeThenTransition();

	/**
	 * Sets the else transition.
	 * 
	 * @param elseTransition the else transition
	 */
	void setElseTransition(IIfTransition elseTransition);

	/**
	 * Sets the test.
	 * 
	 * @param test the test
	 */
	void setTest(String test);

	/**
	 * Sets the then transition.
	 * 
	 * @param elseTransition then transition
	 */
	void setThenTransition(IIfTransition elseTransition);

}
