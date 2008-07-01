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
public interface ISubflowState extends ITransitionableTo {

	/**
	 * Sets the flow.
	 * 
	 * @param flow the flow
	 */
	void setFlow(String flow);

	/**
	 * Gets the flow.
	 * 
	 * @return the flow
	 */
	String getFlow();

	/**
	 * Gets the attribute mapper.
	 * 
	 * @return the attribute mapper
	 */
	IAttributeMapper getAttributeMapper();

	/**
	 * Sets the attribute mapper.
	 * 
	 * @param attributeMapper the attribute mapper
	 */
	void setAttributeMapper(IAttributeMapper attributeMapper);

	/**
	 * Removes the attribute mapper.
	 */
	void removeAttributeMapper();
	
	void setSubflowAttributeMapper(String bean);
	
	String getSubflowAttributeMapper();
	
	/**
	 * Gets the output attributes.
	 * 
	 * @return the output attributes
	 */
	List<IOutputAttribute> getOutputAttributes();

	/**
	 * Adds the output attribute.
	 * 
	 * @param action the action
	 */
	void addOutputAttribute(IOutputAttribute action);

	/**
	 * Adds the output attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addOutputAttribute(IOutputAttribute action, int i);

	/**
	 * Removes the output attribute.
	 * 
	 * @param action the action
	 */
	void removeOutputAttribute(IOutputAttribute action);

	/**
	 * Removes the all output attribute.
	 */
	void removeAllOutputAttribute();
	
	/**
	 * Gets the input attributes.
	 * 
	 * @return the input attributes
	 */
	List<IInputAttribute> getInputAttributes();

	/**
	 * Adds the input attribute.
	 * 
	 * @param action the action
	 */
	void addInputAttribute(IInputAttribute action);

	/**
	 * Adds the input attribute.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addInputAttribute(IInputAttribute action, int i);

	/**
	 * Removes the input attribute.
	 * 
	 * @param action the action
	 */
	void removeInputAttribute(IInputAttribute action);

	/**
	 * Removes the all input attribute.
	 */
	void removeAllInputAttribute();
}
