/*
 * Copyright 2002-2005 the original author or authors.
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
 * Common protocol for all elements provided by the model.
 */
public interface IModelElement {

	/** Character used for delimiting nodes within an element's unique id */
	char ID_DELIMITER = '|';

	/** Character used separate an element's type and name within an element's
	 *  unique id */
	char ID_SEPARATOR = ':';

	IModelElement[] NO_CHILDREN = new IModelElement[0];

	/**
	 * Returns the element directly containing this element,
	 * or <code>null</code> if this element has no parent.
	 *
	 * @return the parent element, or <code>null</code> if this element has no
	 *			 parent
	 */
	IModelElement getElementParent();

	/**
	 * Returns an array with all children of this element.
	 *
	 * @return an array with the children elements
	 */
	IModelElement[] getElementChildren();

	/**
	 * Returns the name of this element.
	 *
	 * @return the element's name
	 */
	String getElementName();

	/**
	 * Returns this element's kind encoded as an integer.
	 * This is a handle-only method.
	 *
	 * @return the kind of element; e.g. one of the constants declared in
	 *   <code>IBeansModelElement</code>
	 * @see IBeansModelElement
	 */
	int getElementType();

	/**
	 * Returns the unique ID of this element.
	 *
	 * @return the element's unique ID
	 */
	String getElementID();

	/**
	 * Accepts the given visitor.
	 * The visitor's <code>visit</code> method is called with this model
	 * element. If the visitor returns <code>true</code>, this method
	 * visits this element's members.
	 *
	 * @param visitor the visitor
	 * @see IModelElementVisitor#visit(IModelElement)
	 * @see #accept(IModelElementVisitor)
	 */
	void accept(IModelElementVisitor visitor);
}
