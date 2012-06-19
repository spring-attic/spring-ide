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
 * Integration AMQP adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.8.0
 * @version Spring Integration 2.0
 */
public class IntAmqpSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/amqp"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INBOUND_GATEWAY = "inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_PUBLISH_SUBSCRIBE_CHANNEL = "publish-subscribe-channel"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACKNOWLEDGE_MODE = "acknowledge-mode"; //$NON-NLS-1$

	public static String ATTR_ADVICE_CHAIN = "advice-chain"; //$NON-NLS-1$

	public static String ATTR_AMQP_ADMIN = "amqp-admin"; //$NON-NLS-1$

	public static String ATTR_AMQP_TEMPLATE = "amqp-template"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHANNEL_TRANSACTED = "channel-transacted"; //$NON-NLS-1$

	public static String ATTR_CONCURRENT_CONSUMERS = "concurrent-consumers"; //$NON-NLS-1$

	public static String ATTR_CONNECTION_FACTORY = "connection-factory"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_ERROR_HANDLER = "error-handler"; //$NON-NLS-1$

	public static String ATTR_ENCODING = "encoding"; //$NON-NLS-1$

	public static String ATTR_EXCHANGE = "exchange"; //$NON-NLS-1$

	public static String ATTR_EXCHANGE_NAME = "exchange-name"; //$NON-NLS-1$

	public static String ATTR_EXCHANGE_NAME_EXPRESSION = "exchange-name-expression"; //$NON-NLS-1$

	public static String ATTR_EXPOSE_LISTENER_CHANNEL = "expose-listener-channel"; //$NON-NLS-1$

	public static String ATTR_HEADER_MAPPER = "header-mapper"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_LISTENER_CONTAINER = "listener-container"; //$NON-NLS-1$

	public static String ATTR_LISTENER_CONVERTER = "listener-converter"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REPLY_HEADERS = "mapped-reply-headers"; //$NON-NLS-1$

	public static String ATTR_MAPPED_REQUEST_HEADERS = "mapped-request-headers"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_CONVERTER = "message-converter"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_DRIVEN = "message-driven"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_PROPERTIES_CONVERTER = "message-properties-converter"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_PHASE = "phase"; //$NON-NLS-1$

	public static String ATTR_PREFETCH_COUNT = "prefetch-count"; //$NON-NLS-1$

	public static String ATTR_QUEUE_NAME = "queue-name"; //$NON-NLS-1$

	public static String ATTR_QUEUE_NAMES = "queue-names"; //$NON-NLS-1$

	public static String ATTR_RECEIVE_TIMEOUT = "receive-timeout"; //$NON-NLS-1$

	public static String ATTR_RECOVERY_INTERVAL = "recovery-interval"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_ROUTING_KEY = "routing-key"; //$NON-NLS-1$

	public static String ATTR_ROUTING_KEY_EXPRESSION = "routing-key-expression"; //$NON-NLS-1$

	public static String ATTR_SHUTDOWN_TIMEOUT = "shutdown-timeout"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_ATTRIBUE = "transaction-attribute"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

	public static String ATTR_TX_SIZE = "tx-size"; //$NON-NLS-1$

}
