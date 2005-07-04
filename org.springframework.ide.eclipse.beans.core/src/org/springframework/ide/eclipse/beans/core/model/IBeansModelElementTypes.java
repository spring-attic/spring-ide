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

/**
 * Constants for element types defined by the Beans model.
 */
public interface IBeansModelElementTypes {

	/**
	 * Constant representing a Beans project.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansProject</code>.
	 */
	int PROJECT_TYPE = 2; // starts with 2 because 1 is reserved for the model

	/**
	 * Constant representing a Beans project's config.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansConfig</code>.
	 */
	int CONFIG_TYPE = 3;

	/**
	 * Constant representing a Beans project's config set.
	 * A Beans element with this type can be safely cast to
	 * <code>BeansConfigSet</code>.
	 */
	int CONFIG_SET_TYPE = 4;

	/**
	 * Constant representing a Beans project's bean.
	 * A Beans element with this type can be safely cast to
	 * <code>Bean</code>.
	 */
	int BEAN_TYPE = 5;

	/**
	 * Constant representing a Beans project bean's property.
	 * A Beans element with this type can be safely cast to
	 * <code>BeanProperty</code>.
	 */
	int PROPERTY_TYPE = 6;

	/**
	 * Constant representing a Beans project bean's constructor argument.
	 * A Beans element with this type can be safely cast to
	 * <code>BeanConstructorArgument</code>.
	 */
	int CONSTRUCTOR_ARGUMENT_TYPE = 7;
}
