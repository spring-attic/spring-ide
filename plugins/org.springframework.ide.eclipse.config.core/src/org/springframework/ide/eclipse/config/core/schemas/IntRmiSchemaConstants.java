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
 * Integration RMI adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/rmi/spring-integration-rmi-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntRmiSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/rmi"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_INBOUND_GATEWAY = "inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_EXPECT_REPLY = "expect-reply"; //$NON-NLS-1$

	public static String ATTR_HOST = "host"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_PORT = "port"; //$NON-NLS-1$

	public static String ATTR_REGISTRY_HOST = "registry-host"; //$NON-NLS-1$

	public static String ATTR_REGISTRY_PORT = "registry-port"; //$NON-NLS-1$

	public static String ATTR_REMOTE_CHANNEL = "remote-channel"; //$NON-NLS-1$

	public static String ATTR_REMOTE_INVOCATION_EXECUTOR = "remote-invocation-executor"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_TIMEOUT = "request-timeout"; //$NON-NLS-1$

}
