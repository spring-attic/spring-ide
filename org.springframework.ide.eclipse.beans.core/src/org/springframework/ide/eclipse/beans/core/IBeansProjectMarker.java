/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
	String PROBLEM_MARKER = BeansCorePlugin.PLUGIN_ID + ".problemmarker";

	/**
	 * Error code marker attribute (value <code>"errorCode"</code>).
	 */
	String ERROR_CODE = "errorCode";

	/**
	 * Bean ID marker attribute (value <code>"beanID"</code>).
	 */
	String BEAN_ID = "beanID";

	/**
	 * Error data marker attribute (value <code>"errorData"</code>).
	 */
	String ERROR_DATA = "errorData";

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
