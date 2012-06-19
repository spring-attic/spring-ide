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
 * Context schema derived from
 * <code>http://www.springframework.org/schema/context/spring-context-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Context 2.5
 */
public class ContextSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/context"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ANNOTATION_CONFIG = "annotation-config"; //$NON-NLS-1$

	public static String ELEM_COMPONENT_SCAN = "component-scan"; //$NON-NLS-1$

	public static String ELEM_EXCLUDE_FILTER = "exclude-filter"; //$NON-NLS-1$

	public static String ELEM_INCLUDE_FILTER = "include-filter"; //$NON-NLS-1$

	public static String ELEM_LOAD_TIME_WEAVER = "load-time-weaver"; //$NON-NLS-1$

	public static String ELEM_MBEAN_EXPORT = "mbean-export"; //$NON-NLS-1$

	public static String ELEM_MBEAN_SERVER = "mbean-server"; //$NON-NLS-1$

	public static String ELEM_PROPERTY_OVERRIDE = "property-override"; //$NON-NLS-1$

	public static String ELEM_PROPERTY_PLACEHOLDER = "property-placeholder"; //$NON-NLS-1$

	public static String ELEM_SPRING_CONFIGURED = "spring-configured"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AGENT_ID = "agent-id"; //$NON-NLS-1$

	public static String ATTR_ANNOTATION_CONFIG = "annotation-config"; //$NON-NLS-1$

	public static String ATTR_ASPECTJ_WEAVING = "aspectj-weaving"; //$NON-NLS-1$

	public static String ATTR_BASE_PACKAGE = "base-package"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_DOMAIN = "default-domain"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_LOCATION = "location"; //$NON-NLS-1$

	public static String ATTR_NAME_GENERATOR = "name-generator"; //$NON-NLS-1$

	public static String ATTR_REGISTRATION = "registration"; //$NON-NLS-1$

	public static String ATTR_RESOURCE_PATTERN = "resource-pattern"; //$NON-NLS-1$

	public static String ATTR_SCOPE_RESOLVER = "scope-resolver"; //$NON-NLS-1$

	public static String ATTR_SCOPED_PROXY = "scoped-proxy"; //$NON-NLS-1$

	public static String ATTR_SERVER = "server"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_USE_DEFAULT_FILTERS = "use-default-filters"; //$NON-NLS-1$

	public static String ATTR_WEAVER_CLASS = "weaver-class"; //$NON-NLS-1$

}
