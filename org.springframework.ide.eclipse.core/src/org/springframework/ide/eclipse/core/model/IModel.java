/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.model;

/**
 * The <code>IModel</code> manages instances of <code>IModelElement</code>s.
 * <code>IModelChangedListener</code>s register with the <code>IModel</code>,
 * and receive <code>ModelChangedEvent</code>s for all changes.
 * 
 * @author Torsten Juergeleit
 */
public interface IModel extends IModelElement {

	/**
	 * Constant representing a model (workspace level object).
	 * A model element with this type can be safely cast to
	 * <code>IModel</code>.
	 */
	int MODEL_TYPE = 1;

	/**
	 * Returns the element for the given element ID.
	 *
	 * @param id the element's unique ID
	 */
	IModelElement getElement(String id);

	void addChangeListener(IModelChangeListener listener);

	void removeChangeListener(IModelChangeListener listener);
}
