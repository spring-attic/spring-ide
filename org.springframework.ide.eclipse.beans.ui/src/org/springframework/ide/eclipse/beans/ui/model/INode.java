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

package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.springframework.ide.eclipse.core.model.IModelElement;

public interface INode extends IAdaptable {

	INode[] NO_CHILDREN = new INode[] {};

	int FLAG_IS_EXTERNAL = 1 << 0;
	int FLAG_HAS_ERRORS = 1 << 1;
	int FLAG_HAS_WARNINGS = 1 << 2;
	int FLAG_IS_PROTOTYPE = 1 << 3;
	int FLAG_IS_LAZY_INIT = 1 << 4;
	int FLAG_IS_ABSTRACT = 1 << 5;
	int FLAG_IS_ROOT_BEAN_WITHOUT_CLASS = 1 << 6;

	int NOT_PROPAGATED_FLAGS = (FLAG_IS_EXTERNAL | FLAG_IS_PROTOTYPE |
								FLAG_IS_LAZY_INIT | FLAG_IS_ABSTRACT |
								FLAG_IS_ROOT_BEAN_WITHOUT_CLASS);
	/**
	 * Returns this node's parent or <code>null</code> if none.
	 * 
	 * @return this node's parent node
	 */
	INode getParent();

	/**
	 * Returns this node's children.
	 * @return this node's children
	 */
	INode[] getChildren();

	/**
	 * Returns true if this node has children.
	 * @return true if this node has children
	 */
	boolean hasChildren();

	/**
	 * Returns the unique ID of this node.
	 *
	 * @return the node's unique ID
	 */
	String getID();

	/**
	 * Returns this node's name or <code>null</code> if none.
	 * 
	 * @return this node's name or <code>null</code> if the name attribute is
	 * 		   optional for this node
	 */
	String getName();

	/**
	 * Returns the model element associated with this node or <code>null</code>
	 * if none.
	 *
	 * @return this node's model element
	 */
	IModelElement getElement();

	/**
	 * Returns this node's flags.
	 * 
	 * @return this node's flags
	 */
	int getFlags();

	/**
	 * Returns this node's location within config file.
	 *
	 * @return this node's location within config file or -1 if location is
	 * 		   unknown 
	 */
	int getStartLine();
}
