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
