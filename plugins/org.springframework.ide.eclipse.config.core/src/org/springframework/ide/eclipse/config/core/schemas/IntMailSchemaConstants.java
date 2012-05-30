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
 * Integration Mail adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/mail/spring-integration-mail-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntMailSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/mail"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_BCC = "bcc"; //$NON-NLS-1$

	public static String ELEM_CC = "cc"; //$NON-NLS-1$

	public static String ELEM_FROM = "from"; //$NON-NLS-1$

	public static String ELEM_HEADER_ENRICHER = "header-enricher"; //$NON-NLS-1$

	public static String ELEM_IMAP_IDLE_CHANNEL_ADAPTER = "imap-idle-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_MAIL_TO_STRING_TRANSFORMER = "mail-to-string-transformer"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_REPLY_TO = "reply-to"; //$NON-NLS-1$

	public static String ELEM_SUBJECT = "subject"; //$NON-NLS-1$

	public static String ELEM_TO = "to"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHARSET = "charset"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_HOST = "host"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_JAVA_MAIL_PROPERTIES = "java-mail-properties"; //$NON-NLS-1$

	public static String ATTR_MAIL_FILTER_EXPRESSION = "mail-filter-expression"; //$NON-NLS-1$

	public static String ATTR_MAIL_SENDER = "mail-sender"; //$NON-NLS-1$

	public static String ATTR_MAX_FETCH_SIZE = "max-fetch-size"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_PASSWORD = "password"; //$NON-NLS-1$

	public static String ATTR_PORT = "port"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_SESSION = "session"; //$NON-NLS-1$

	public static String ATTR_SHOULD_DELETE_MESSAGES = "should-delete-messages"; //$NON-NLS-1$

	public static String ATTR_SHOULD_MARK_MESSAGES_AS_READ = "should-mark-messages-as-read"; //$NON-NLS-1$

	public static String ATTR_STORE_URI = "store-uri"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_USERNAME = "username"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

}
