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

import java.util.EventObject;

import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * An element changed event describes a change to an element of the model.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the model.
 * </p>
 * @see IModelChangeListener
 * @author Torsten Juergeleit
 */
public class ModelChangeEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum Type { ADDED, REMOVED, CHANGED };

	private Type type;

	/**
	 * Creates an new element change event.
	 *
	 * @param element  the changed model element
	 * @param type  the type of modification (ADDED, REMOVED, CHANGED) this
	 * 				event contains
	 */
	public ModelChangeEvent(IModelElement element, Type type) {
		super(element);
		this.type = type;
	}

	/**
	 * Returns the modified element.
	 */
	public IModelElement getElement() {
		return (IModelElement) getSource();
	}

	/**
	 * Returns the type of modification.
	 */
	public Type getType() {
		return type;
	}
}
