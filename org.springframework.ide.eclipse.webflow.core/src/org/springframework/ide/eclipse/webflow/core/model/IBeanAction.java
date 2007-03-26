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
public interface IBeanAction extends IAction {

	/**
	 * Gets the method arguments.
	 * 
	 * @return the method arguments
	 */
	IMethodArguments getMethodArguments();

	/**
	 * Gets the method result.
	 * 
	 * @return the method result
	 */
	IMethodResult getMethodResult();

	/**
	 * Sets the method arguments.
	 * 
	 * @param methodArguments the method arguments
	 */
	void setMethodArguments(IMethodArguments methodArguments);

	/**
	 * Sets the method result.
	 * 
	 * @param methodResult the method result
	 */
	void setMethodResult(IMethodResult methodResult);

}
