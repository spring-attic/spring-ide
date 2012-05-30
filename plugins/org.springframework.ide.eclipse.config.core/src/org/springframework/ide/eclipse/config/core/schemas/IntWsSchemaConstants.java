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
 * Integration WS adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/ws/spring-integration-ws-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntWsSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/ws"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_HEADER_ENRICHER = "header-enricher"; //$NON-NLS-1$

	public static String ELEM_INBOUND_GATEWAY = "inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_SOAP_ACTION = "soap-action"; //$NON-NLS-1$

	public static String ELEM_URI_VARIABLE = "uri-variable"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_DESTINATION_PROVIDER = "destination-provider"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD = "extract-payload"; //$NON-NLS-1$

	public static String ATTR_FAULT_MESSAGE_RESOLVER = "fault-message-resolver"; //$NON-NLS-1$

	public static String ATTR_HEADER_MAPPER = "header-mapper"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IGNORE_EMPTY_RESPONSES = "ignore-empty-responses"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_INTERCEPTOR = "interceptor"; //$NON-NLS-1$

	public static String ATTR_INTERCEPTORS = "interceptors"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REPLY_HEADERS = "mapped-reply-headers"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REQUEST_HEADERS = "mapped-request-headers"; //$NON-NLS-1$

	public static String ATTR_MARSHALLER = "marshaller"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_FACTORY = "message-factory"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_SENDER = "message-sender"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_SENDERS = "message-senders"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CALLBACK = "request-callback"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_SOURCE_EXTRACTOR = "source-extractor"; //$NON-NLS-1$

	public static String ATTR_UNMARSHALLER = "unmarshaller"; //$NON-NLS-1$

	public static String ATTR_URI = "uri"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

}
