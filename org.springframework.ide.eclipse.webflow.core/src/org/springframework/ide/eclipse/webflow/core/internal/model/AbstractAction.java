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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.springframework.ide.eclipse.webflow.core.model.IAction;
import org.springframework.ide.eclipse.webflow.core.model.IBeanReference;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractAction extends AbstractActionElement implements
		IAction, IBeanReference {

	/**
	 * 
	 */
	private ACTION_TYPE type;

	/**
	 * 
	 * 
	 * @param type
	 */
	public void setType(ACTION_TYPE type) {
		this.type = type;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public ACTION_TYPE getType() {
		return this.type;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return getAttribute("name");
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	public String getBean() {
		return getAttribute("bean");
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public String getMethod() {
		return getAttribute("method");
	}

	/**
	 * Checks for bean reference.
	 * 
	 * @return true, if has bean reference
	 */
	public boolean hasBeanReference() {
		return getBean() != null;
	}

	/**
	 * Sets the bean.
	 * 
	 * @param bean the bean
	 */
	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	/**
	 * Sets the method.
	 * 
	 * @param method the method
	 */
	public void setMethod(String method) {
		setAttribute("method", method);
	}
}
