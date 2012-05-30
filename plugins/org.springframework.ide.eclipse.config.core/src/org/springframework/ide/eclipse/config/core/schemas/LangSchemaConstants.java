/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.schemas;


/**
 * Lang schema derived from
 * <code>http://www.springframework.org/schema/lang/spring-lang-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Lang 2.5
 */
public class LangSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/lang"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_BSH = "bsh"; //$NON-NLS-1$

	public static String ELEM_DEFAULTS = "defaults"; //$NON-NLS-1$

	public static String ELEM_GROOVY = "groovy"; //$NON-NLS-1$

	public static String ELEM_INLINE_SCRIPT = "inline-script"; //$NON-NLS-1$

	public static String ELEM_JRUBY = "jruby"; //$NON-NLS-1$

	public static String ELEM_PROPERTY = "property"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTOWIRE = "autowire"; //$NON-NLS-1$

	public static String ATTR_CUSTOMIZER_REF = "customizer-ref"; //$NON-NLS-1$

	public static String ATTR_DEPENDENCY_CHECK = "dependency-check"; //$NON-NLS-1$

	public static String ATTR_DESTROY_METHOD = "destroy-method"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INIT_METHOD = "init-method"; //$NON-NLS-1$

	public static String ATTR_REFRESH_CHECK_DELAY = "refresh-check-delay"; //$NON-NLS-1$

	public static String ATTR_SCRIPT_INTERFACES = "script-interfaces"; //$NON-NLS-1$

	public static String ATTR_SCRIPT_SOURCE = "script-source"; //$NON-NLS-1$

	public static String ATTR_SCOPE = "scope"; //$NON-NLS-1$

}
