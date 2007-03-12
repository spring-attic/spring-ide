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
public interface IOutputMapper extends IWebflowModelElement {

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
	 * Gets the mapping.
	 * 
	 * @return the mapping
	 */
	List<IMapping> getMapping();

	/**
	 * Adds the mapping.
	 * 
	 * @param action the action
	 */
	void addMapping(IMapping action);

	/**
	 * Adds the mapping.
	 * 
	 * @param i the i
	 * @param action the action
	 */
	void addMapping(IMapping action, int i);

	/**
	 * Removes the mapping.
	 * 
	 * @param action the action
	 */
	void removeMapping(IMapping action);

	/**
	 * Removes the all mapping.
	 */
	void removeAllMapping();

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	void createNew(IWebflowModelElement parent);
}
