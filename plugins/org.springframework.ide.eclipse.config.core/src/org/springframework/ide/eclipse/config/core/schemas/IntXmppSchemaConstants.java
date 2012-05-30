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
 * Integration XMPP adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/xmpp/spring-integration-xmpp-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntXmppSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/xmpp"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_CHAT_THREAD_ID = "chat-thread-id"; //$NON-NLS-1$

	public static String ELEM_CHAT_TO = "chat-to"; //$NON-NLS-1$

	public static String ELEM_HEADER_ENRICHER = "header-enricher"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_PRESENCE_INBOUND_CHANNEL_ADAPTER = "presence-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_PRESENCE_OUTBOUND_CHANNEL_ADAPTER = "presence-outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_XMPP_CONNECTION = "xmpp-connection"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OVERWRITE = "default-overwrite"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD = "extract-payload"; //$NON-NLS-1$

	public static String ATTR_HEADER_MAPPER = "header-mapper"; //$NON-NLS-1$

	public static String ATTR_HOST = "host"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REQUEST_HEADERS = "mapped-request-headers"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_OVERWRITE = "overwrite"; //$NON-NLS-1$

	public static String ATTR_PASSWORD = "password"; //$NON-NLS-1$

	public static String ATTR_PORT = "port"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_RESOURCE = "resource"; //$NON-NLS-1$

	public static String ATTR_SERVICE_NAME = "service-name"; //$NON-NLS-1$

	public static String ATTR_SUBSCRIPTION_MODE = "subscription-mode"; //$NON-NLS-1$

	public static String ATTR_USER = "user"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

	public static String ATTR_XMPP_CONNECTION = "xmpp-connection"; //$NON-NLS-1$

}
