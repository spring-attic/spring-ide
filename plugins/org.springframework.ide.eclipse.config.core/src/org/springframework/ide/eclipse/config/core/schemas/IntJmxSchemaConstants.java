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
 * Integration JMX adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/jmx/spring-integration-jmx-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntJmxSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/jmx"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ATTRIBUTE_POLLING_CHANNEL_ADAPTER = "attribute-polling-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_CONTROL_BUS = "control-bus"; //$NON-NLS-1$

	public static String ELEM_MBEAN_EXPORT = "mbean-export"; //$NON-NLS-1$

	public static String ELEM_NOTIFICATION_LISTENING_CHANNEL_ADAPTER = "notification-listening-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_NOTIFICATION_PUBLISHING_CHANNEL_ADAPTER = "notification-publishing-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OPERATION_INVOKING_CHANNEL_ADAPTER = "operation-invoking-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OPERATION_INVOKING_OUTBOUND_GATEWAY = "operation-invoking-outbound-gateway"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ATTRIBUTE_NAME = "attribute-name"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_DOMAIN = "default-domain"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_NOTIFICATION_TYPE = "default-notification-type"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OBJECT_NAME = "default-object-name"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OPERATION_NAME = "default-operation-name"; //$NON-NLS-1$

	public static String ATTR_DOMAIN = "domain"; //$NON-NLS-1$

	public static String ATTR_HANDBACK = "handback"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_MANAGED_COMPONENTS = "managed-components"; //$NON-NLS-1$

	public static String ATTR_MBEAN_SERVER = "mbean-server"; //$NON-NLS-1$

	public static String ATTR_NOTIFICATION_FILTER = "notification-filter"; //$NON-NLS-1$

	public static String ATTR_OBJECT_NAME = "object-name"; //$NON-NLS-1$

	public static String ATTR_OBJECT_NAME_STATIC_PROPERTIES = "object-name-static-properties"; //$NON-NLS-1$

	public static String ATTR_OPERATION_CHANNEL = "operation-channel"; //$NON-NLS-1$

	public static String ATTR_OPERATION_NAME = "operation-name"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_SEND_TIMEOUT = "send-timeout"; //$NON-NLS-1$

	public static String ATTR_SERVER = "server"; //$NON-NLS-1$

}
