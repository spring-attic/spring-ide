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

package org.springframework.ide.eclipse.beans.core;

/**
 * Markers related with Spring Beans projects.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * @author Torsten Juergeleit
 */
public interface IBeansProjectMarker {

	/**
	 * Spring Beans project problem marker type (value 
	 * <code>"org.springframework.ide.eclipse.beans.core.problemmarker"</code>).
	 * This can be used to recognize those markers in the workspace that flag
	 * problems related with Spring Beans projects.
	 */
	public static final String PROBLEM_MARKER = BeansCorePlugin.PLUGIN_ID
			+ ".problemmarker";
	/**
	 * Error code marker attribute (value <code>"errorCode"</code>).
	 */
	public static final String ERROR_CODE = "errorCode";

	/**
	 * Bean ID marker attribute (value <code>"beanID"</code>).
	 */
	public static final String BEAN_ID = "beanID";

	/**
	 * Error data marker attribute (value <code>"errorData"</code>).
	 */
	public static final String ERROR_DATA = "errorData";

	// Codes used for attribute 'ERROR_CODE'
	public enum ErrorCode {
		NONE,
		PARSING_FAILED,
		BEAN_OVERRIDE,
		BEAN_WITHOUT_CLASS_OR_PARENT,
		CLASS_NOT_FOUND,
		UNDEFINED_PARENT_BEAN,
		NO_CONSTRUCTOR,
		NO_SETTER,
		UNDEFINED_REFERENCED_BEAN,
		INVALID_REFERENCED_BEAN,
		INVALID_BEAN_DEFINITION,
		INVALID_BEAN_ALIAS,
		UNDEFINED_DEPENDS_ON_BEAN,
		INVALID_DEPENDS_ON_BEAN,
		UNDEFINED_FACTORY_BEAN,
		INVALID_FACTORY_BEAN,
		UNDEFINED_FACTORY_BEAN_METHOD,
		NO_GETTER,
		CLASS_NOT_ALLOWED,
		NO_FACTORY_METHOD,
		ALIAS_OVERRIDE,
		INVALID_PROPERTY_NAME,
		UNDEFINED_INIT_METHOD,
		UNDEFINED_DESTROY_METHOD
	}
}
