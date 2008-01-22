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
public interface IIfTransition extends ITransition {

	/**
	 * Gets the from if.
	 * 
	 * @return the from if
	 */
	IIf getFromIf();

	/**
	 * Sets the from if.
	 * 
	 * @param fromIf the from if
	 */
	void setFromIf(IIf fromIf);

	/**
	 * Checks if is then.
	 * 
	 * @return true, if is then
	 */
	boolean isThen();

	/**
	 * Sets the then.
	 * 
	 * @param isThen then
	 */
	public void setThen(boolean isThen);
}
