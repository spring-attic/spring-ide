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
 * Integration JMS adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/jms/spring-integration-jms-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntJmsSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/jms"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ELEM_CORRELATION_ID = "correlation-id"; //$NON-NLS-1$

	public static String ELEM_HEADER_ENRICHER = "header-enricher"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INBOUND_GATEWAY = "inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_MESSAGE_DRIVEN_CHANNEL_ADAPTER = "message-driven-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_PUBLISH_SUBSCRIBE_CHANNEL = "publish-subscribe-channel"; //$NON-NLS-1$

	public static String ELEM_REPLY_TO = "reply-to"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACKNOWLEDGE = "acknowledge"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CACHE = "cache"; //$NON-NLS-1$

	public static String ATTR_CACHE_LEVEL = "cache-level"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CLIENT_ID = "client-id"; //$NON-NLS-1$

	public static String ATTR_CONCURRENCY = "concurrency"; //$NON-NLS-1$

	public static String ATTR_CONCURRENT_CONSUMERS = "concurrent-consumers"; //$NON-NLS-1$

	public static String ATTR_CONNECTION_FACTORY = "connection-factory"; //$NON-NLS-1$

	public static String ATTR_CONTAINER = "container"; //$NON-NLS-1$

	public static String ATTR_CONTAINER_CLASS = "container-class"; //$NON-NLS-1$

	public static String ATTR_CONTAINER_TYPE = "container-type"; //$NON-NLS-1$

	public static String ATTR_CORRELATION_KEY = "correlation-key"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REPLY_DESTINATION = "default-reply-destination"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REPLY_QUEUE_NAME = "default-reply-queue-name"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REPLY_TOPIC_NAME = "default-reply-topic-name"; //$NON-NLS-1$

	public static String ATTR_DELIVERY_MODE = "delivery-mode"; //$NON-NLS-1$

	public static String ATTR_DELIVERY_PERSISTENT = "delivery-persistent"; //$NON-NLS-1$

	public static String ATTR_DESTINATION = "destination"; //$NON-NLS-1$

	public static String ATTR_DESTINATION_NAME = "destination-name"; //$NON-NLS-1$

	public static String ATTR_DESTINATION_RESOLVER = "destination-resolver"; //$NON-NLS-1$

	public static String ATTR_DURABLE = "durable"; //$NON-NLS-1$

	public static String ATTR_DURABLE_SUBSCRIPTION_NAME = "durable-subcription-name"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_ERROR_HANDLER = "error-handler"; //$NON-NLS-1$

	public static String ATTR_EXPLICIT_QOS_ENABLED = "explicit-qos-enabled"; //$NON-NLS-1$

	public static String ATTR_EXPLICIT_QOS_ENABLED_FOR_REPLIES = "explicit-qos-enabled-for-replies"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD = "extract-payload"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_REPLY_PAYLOAD = "extract-reply-payload"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_REQUEST_PAYLOAD = "extract-request-payload"; //$NON-NLS-1$

	public static String ATTR_HEADER_MAPPER = "header-mapper"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IDLE_CONSUMER_LIMIT = "idle-consumer-limit"; //$NON-NLS-1$

	public static String ATTR_IDLE_TASK_EXECUTION_LIMIT = "idle-task-execution-limit"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_JMS_TEMPLATE = "jms-template"; //$NON-NLS-1$

	public static String ATTR_MAX_CONCURRENT_CONSUMERS = "max-concurrent-consumers"; //$NON-NLS-1$

	public static String ATTR_MAX_MESSAGES_PER_TASK = "max-messages-per-task"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_CONVERTER = "message-converter"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_PHASE = "phase"; //$NON-NLS-1$

	public static String ATTR_PREFETCH = "prefetch"; //$NON-NLS-1$

	public static String ATTR_PRIORITY = "priority"; //$NON-NLS-1$

	public static String ATTR_PUB_SUB_DOMAIN = "pub-sub-domain"; //$NON-NLS-1$

	public static String ATTR_QUEUE = "queue"; //$NON-NLS-1$

	public static String ATTR_QUEUE_TYPE = "queue-type"; //$NON-NLS-1$

	public static String ATTR_RECEIVE_TIMEOUT = "receive-timeout"; //$NON-NLS-1$

	public static String ATTR_RECOVERY_INTERVAL = "recovery-interval"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_DELIVERY_PERSISTENT = "reply-delivery-persistent"; //$NON-NLS-1$

	public static String ATTR_REPLY_DESTINATION = "reply-destination"; //$NON-NLS-1$

	public static String ATTR_REPLY_DESTINATION_NAME = "reply-destination-name"; //$NON-NLS-1$

	public static String ATTR_REPLY_PRIORITY = "reply-priority"; //$NON-NLS-1$

	public static String ATTR_REPLY_PUB_SUB_DOMAIN = "reply-pub-sub-domain"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIME_TO_LIVE = "reply-time-to-live"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_DESTINATION = "request-destination"; //$NON-NLS-1$

	public static String ATTR_REQUEST_DESTINATION_EXPRESSION = "request-destination-expression"; //$NON-NLS-1$

	public static String ATTR_REQUEST_DESTINATION_NAME = "request-destination-name"; //$NON-NLS-1$

	public static String ATTR_REQUEST_PUB_SUB_DOMAIN = "request-pub-sub-domain"; //$NON-NLS-1$

	public static String ATTR_REQUEST_TIMEOUT = "request-timeout"; //$NON-NLS-1$

	public static String ATTR_SELECTOR = "selector"; //$NON-NLS-1$

	public static String ATTR_SEND_TIMEOUT = "send-timeout"; //$NON-NLS-1$

	public static String ATTR_SUBSCRIPTION = "subscription"; //$NON-NLS-1$

	public static String ATTR_SUBSCRIPTION_DURABLE = "subscription-durable"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TIME_TO_LIVE = "time-to-live"; //$NON-NLS-1$

	public static String ATTR_TOPIC = "topic"; //$NON-NLS-1$

	public static String ATTR_TOPIC_NAME = "topic-name"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

}
