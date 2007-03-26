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
public interface IAttributeMapper extends IWebflowModelElement {

	/**
	 * Sets the bean.
	 * 
	 * @param bean the bean
	 */
	void setBean(String bean);

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	String getBean();

	/**
	 * Gets the input mapper.
	 * 
	 * @return the input mapper
	 */
	IInputMapper getInputMapper();

	/**
	 * Sets the input mapper.
	 * 
	 * @param inputMapper the input mapper
	 */
	void setInputMapper(IInputMapper inputMapper);

	/**
	 * Gets the output mapper.
	 * 
	 * @return the output mapper
	 */
	IOutputMapper getOutputMapper();

	/**
	 * Sets the output mapper.
	 * 
	 * @param outputMapper the output mapper
	 */
	void setOutputMapper(IOutputMapper outputMapper);

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);

}
