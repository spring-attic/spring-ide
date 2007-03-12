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

import java.util.EventObject;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelChangedEvent extends EventObject {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = -8568748940583679520L;

	/**
	 * The Constant ADDED.
	 */
	public static final int ADDED = 1;

	/**
	 * The Constant REMOVED.
	 */
	public static final int REMOVED = 2;

	/**
	 * The Constant CHANGED.
	 */
	public static final int CHANGED = 3;

	/**
	 * The type.
	 */
	private int type;

	/**
	 * The Constructor.
	 * 
	 * @param element the element
	 * @param type the type
	 */
	public WebflowModelChangedEvent(IWebflowModelElement element, int type) {
		super(element);
		this.type = type;
	}

	/**
	 * Gets the element.
	 * 
	 * @return the element
	 */
	public IWebflowModelElement getElement() {
		return (IWebflowModelElement) getSource();
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}
}