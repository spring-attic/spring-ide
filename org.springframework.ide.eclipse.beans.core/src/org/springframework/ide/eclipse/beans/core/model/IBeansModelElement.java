/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.model;

import org.eclipse.core.resources.IResource;

/**
 * Common protocol for all elements provided by the Beans model.
 */
public interface IBeansModelElement {

	/**
	 * Constant representing the Beans model (workspace level object).
	 * A Beans element with this type can be safely cast to
	 * <code>BeansModel</code>.
	 */
	int MODEL = 1;

	/**
	 * Constant representing a Beans project.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansProject</code>.
	 */
	int PROJECT = 2;

	/**
	 * Constant representing a Beans project's config.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansConfig</code>.
	 */
	int CONFIG = 3;

	/**
	 * Constant representing a Beans project's config set.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansConfigSet</code>.
	 */
	int CONFIG_SET = 4;

	/**
	 * Constant representing a Beans project's bean.
	 * A Beans element with this type can be safely cast to
	 * <code>Bean</code>.
	 */
	int BEAN = 5;

	/**
	 * Constant representing a Beans project bean's property.
	 * A Beans element with this type can be safely cast to
	 * <code>Property</code>.
	 */
	int PROPERTY = 6;

	/**
	 * Constant representing a Beans project bean's constructor argument.
	 * A Beans element with this type can be safely cast to
	 * <code>ConstructorArgument</code>.
	 */
	int CONSTRUCTOR_ARGUMENT = 7;

	/**
	 * Returns the element directly containing this element,
	 * or <code>null</code> if this element has no parent.
	 *
	 * @return the parent element, or <code>null</code> if this element has no
	 *			 parent
	 */
	IBeansModelElement getElementParent();

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
	 * @return the kind of element; one of the constants declared in
	 *   <code>IBeansElement</code>
	 * @see IBeansModelElement
	 */
	int getElementType();

	/**
	 * Returns the resource of the innermost resource enclosing this element. 
	 * 
	 * @return the resource of the innermost resource enclosing this element
	 */
	IResource getElementResource();

	/**
	 * Returns the line number with the start of the element's source code.
	 *
	 * @return line number with start of element's source code
	 */
	int getElementStartLine();
}
