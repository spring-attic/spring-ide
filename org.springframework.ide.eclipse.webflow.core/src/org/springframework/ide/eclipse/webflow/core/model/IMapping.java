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
public interface IMapping extends IWebflowModelElement {

	/**
	 * Sets the source.
	 * 
	 * @param value the source
	 */
	void setSource(String value);

	/**
	 * Sets the target.
	 * 
	 * @param value the target
	 */
	void setTarget(String value);

	/**
	 * Sets the target collection.
	 * 
	 * @param value the target collection
	 */
	void setTargetCollection(String value);

	/**
	 * Sets the from.
	 * 
	 * @param value the from
	 */
	void setFrom(String value);

	/**
	 * Sets the to.
	 * 
	 * @param value the to
	 */
	void setTo(String value);

	/**
	 * Sets the required.
	 * 
	 * @param value the required
	 */
	void setRequired(boolean value);

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	String getSource();

	/**
	 * Gets the target.
	 * 
	 * @return the target
	 */
	String getTarget();

	/**
	 * Gets the target collection.
	 * 
	 * @return the target collection
	 */
	String getTargetCollection();

	/**
	 * Gets the from.
	 * 
	 * @return the from
	 */
	String getFrom();

	/**
	 * Gets the to.
	 * 
	 * @return the to
	 */
	String getTo();

	/**
	 * Gets the required.
	 * 
	 * @return the required
	 */
	boolean getRequired();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);

}
