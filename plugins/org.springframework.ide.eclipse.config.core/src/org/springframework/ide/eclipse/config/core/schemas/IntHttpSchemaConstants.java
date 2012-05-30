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
 * Integration HTTP adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/http/spring-integration-http-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntHttpSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/http"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_HEADER = "header"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INBOUND_GATEWAY = "inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_URI_VARIABLE = "uri-variable"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHARSET = "charset"; //$NON-NLS-1$

	public static String ATTR_CONVERT_EXPRESSION = "convert-expression"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_URL = "default-url"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_ERROR_CODE = "error-code"; //$NON-NLS-1$

	public static String ATTR_ERROR_HANDLER = "error-handler"; //$NON-NLS-1$

	public static String ATTR_ERRORS_KEY = "errors-key"; //$NON-NLS-1$

	public static String ATTR_EXPECTED_RESPONSE_TYPE = "expected-response-type"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD = "extract-payload"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_REPLY_PAYLOAD = "extract-reply-payload"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_REQUEST_PAYLOAD = "extract-request-payload"; //$NON-NLS-1$

	public static String ATTR_HEADER_MAPPER = "header-mapper"; //$NON-NLS-1$

	public static String ATTR_HTTP_METHOD = "http-method"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_MAPPED_RESPONSE_HEADERS = "mapped-response-headers"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REQUEST_HEADERS = "mapped-request-headers"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_CONVERTERS = "message-converters"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_PARAMETER_EXTRACTOR = "parameter-extractor"; //$NON-NLS-1$

	public static String ATTR_PATH = "path"; //$NON-NLS-1$

	public static String ATTR_PAYLOAD_EXPRESSION = "payload-expression"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_KEY = "reply-key"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_EXECUTOR = "request-executor"; //$NON-NLS-1$

	public static String ATTR_REQUEST_FACTORY = "request-factory"; //$NON-NLS-1$

	public static String ATTR_REQUEST_KEY = "request-key"; //$NON-NLS-1$

	public static String ATTR_REQUEST_MAPPER = "request-mapper"; //$NON-NLS-1$

	public static String ATTR_REQUEST_PAYLOAD_TYPE = "request-payload-type"; //$NON-NLS-1$

	public static String ATTR_REQUEST_TIMEOUT = "request-timeout"; //$NON-NLS-1$

	public static String ATTR_REST_TEMPLATE = "rest-template"; //$NON-NLS-1$

	public static String ATTR_SEND_TIMEOUT = "send-timeout"; //$NON-NLS-1$

	public static String ATTR_SUPPORTED_METHODS = "supported-methods"; //$NON-NLS-1$

	public static String ATTR_TRANSFER_COOKIES = "transfer-cookies"; //$NON-NLS-1$

	public static String ATTR_URL = "url"; //$NON-NLS-1$

	public static String ATTR_VIEW = "view"; //$NON-NLS-1$

	public static String ATTR_VIEW_NAME = "view-name"; //$NON-NLS-1$

}
