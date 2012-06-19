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
 * JEE schema derived from
 * <code>http://www.springframework.org/schema/jee/spring-jee-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring JEE 2.5
 */
public class JeeSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/jee"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ENVIRONMENT = "environment"; //$NON-NLS-1$

	public static String ELEM_JNDI_LOOKUP = "jndi-lookup"; //$NON-NLS-1$

	public static String ELEM_LOCAL_SLSB = "local-slsb"; //$NON-NLS-1$

	public static String ELEM_REMOTE_SLSB = "remote-slsb"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_BUSINESS_INTERFACE = "business-interface"; //$NON-NLS-1$

	public static String ATTR_CACHE = "cache"; //$NON-NLS-1$

	public static String ATTR_CACHE_HOME = "cache-home"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REF = "default-ref"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_VALUE = "default-value"; //$NON-NLS-1$

	public static String ATTR_ENVIRONMENT_REF = "environment-ref"; //$NON-NLS-1$

	public static String ATTR_EXPECTED_TYPE = "expected-type"; //$NON-NLS-1$

	public static String ATTR_HOME_INTERFACE = "home-interface"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_JNDI_NAME = "jndi-name"; //$NON-NLS-1$

	public static String ATTR_LOOKUP_HOME_ON_STARTUP = "lookup-home-on-startup"; //$NON-NLS-1$

	public static String ATTR_LOOKUP_ON_STARTUP = "lookup-on-startup"; //$NON-NLS-1$

	public static String ATTR_PROXY_INTERFACE = "proxy-interface"; //$NON-NLS-1$

	public static String ATTR_REFRESH_HOME_ON_CONNECT_FAILURE = "refresh-home-on-connect-failure"; //$NON-NLS-1$

	public static String ATTR_RESOURCE_REF = "resource-ref"; //$NON-NLS-1$

}
