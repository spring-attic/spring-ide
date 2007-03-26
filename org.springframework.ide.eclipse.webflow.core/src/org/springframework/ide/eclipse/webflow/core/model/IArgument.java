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
public interface IArgument extends IWebflowModelElement {

	/**
	 * Sets the expression.
	 * 
	 * @param expression the expression
	 */
	void setExpression(String expression);

	/**
	 * Sets the parameter type.
	 * 
	 * @param parameterType the parameter type
	 */
	void setParameterType(String parameterType);

	/**
	 * Gets the expression.
	 * 
	 * @return the expression
	 */
	String getExpression();

	/**
	 * Gets the parameter type.
	 * 
	 * @return the parameter type
	 */
	String getParameterType();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);

}
