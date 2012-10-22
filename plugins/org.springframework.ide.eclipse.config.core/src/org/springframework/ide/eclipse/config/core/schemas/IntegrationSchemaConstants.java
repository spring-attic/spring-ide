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
 * Integration schema derived from
 * <code>http://www.springframework.org/schema/integration/spring-integration-2.0.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.3.0
 * @version Spring Integration 2.0
 */
public class IntegrationSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ADVICE_CHAIN = "advice-chain"; //$NON-NLS-1$

	public static String ELEM_AGGREGATOR = "aggregator"; //$NON-NLS-1$

	public static String ELEM_ANNOTATION_CONFIG = "annotation-config"; //$NON-NLS-1$

	public static String ELEM_APPLICATION_EVENT_MULTICASTER = "application-event-multicaster"; //$NON-NLS-1$

	public static String ELEM_BRIDGE = "bridge"; //$NON-NLS-1$

	public static String ELEM_CHAIN = "chain"; //$NON-NLS-1$

	public static String ELEM_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ELEM_CHANNEL_INTERCEPTOR = "channel-interceptor"; //$NON-NLS-1$

	public static String ELEM_CLAIM_CHECK_IN = "claim-check-in"; //$NON-NLS-1$

	public static String ELEM_CLAIM_CHECK_OUT = "claim-check-out"; //$NON-NLS-1$

	public static String ELEM_COMPLETION_STRATEGY = "completion-strategy"; //$NON-NLS-1$

	public static String ELEM_CONTROL_BUS = "control-bus"; //$NON-NLS-1$

	public static String ELEM_CONVERTER = "converter"; //$NON-NLS-1$

	public static String ELEM_CORRELATON_ID = "correlation-id"; //$NON-NLS-1$

	public static String ELEM_CRON_TRIGGER = "cron-trigger"; //$NON-NLS-1$

	public static String ELEM_DELAYER = "delayer"; //$NON-NLS-1$

	public static String ELEM_DISPATCHER = "dispatcher"; //$NON-NLS-1$

	public static String ELEM_ENRICHER = "enricher"; //$NON-NLS-1$

	public static String ELEM_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ELEM_EXCEPTION_TYPE_ROUTER = "exception-type-router"; //$NON-NLS-1$

	public static String ELEM_EXPIRATION_DATE = "expiration-date"; //$NON-NLS-1$

	public static String ELEM_FILTER = "filter"; //$NON-NLS-1$

	public static String ELEM_GATEWAY = "gateway"; //$NON-NLS-1$

	public static String ELEM_HEADER = "header"; //$NON-NLS-1$

	public static String ELEM_HEADER_ENRICHER = "header-enricher"; //$NON-NLS-1$

	public static String ELEM_HEADER_FILTER = "header-filter"; //$NON-NLS-1$

	public static String ELEM_HEADER_VALUE_ROUTER = "header-value-router"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INTERCEPTORS = "interceptors"; //$NON-NLS-1$

	public static String ELEM_INTERVAL_TRIGGER = "interval-trigger"; //$NON-NLS-1$

	public static String ELEM_JSON_TO_OBJECT_TRANSFORMER = "json-to-object-transformer"; //$NON-NLS-1$

	public static String ELEM_LOGGING_CHANNEL_ADAPTER = "logging-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_MAP_TO_OBJECT_TRANSFORMER = "map-to-object-transformer"; //$NON-NLS-1$

	public static String ELEM_MAPPING = "mapping"; //$NON-NLS-1$

	public static String ELEM_MESSAGE_HISTORY = "message-history"; //$NON-NLS-1$

	public static String ELEM_METHOD = "method"; //$NON-NLS-1$

	public static String ELEM_OBJECT_TO_JSON_TRANSFORMER = "object-to-json-transformer"; //$NON-NLS-1$

	public static String ELEM_OBJECT_TO_MAP_TRANSFORMER = "object-to-map-transformer"; //$NON-NLS-1$

	public static String ELEM_OBJECT_TO_STRING_TRANSFORMER = "object-to-string-transformer"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_PAYLOAD_DESERIALIZING_TRANSFORMER = "payload-deserializing-transformer"; //$NON-NLS-1$

	public static String ELEM_PAYLOAD_SERIALIZING_TRANSFORMER = "payload-serializing-transformer"; //$NON-NLS-1$

	public static String ELEM_PAYLOAD_TYPE_ROUTER = "payload-type-router"; //$NON-NLS-1$

	public static String ELEM_POLLER = "poller"; //$NON-NLS-1$

	public static String ELEM_PRIORITY = "priority"; //$NON-NLS-1$

	public static String ELEM_PRIORITY_QUEUE = "priority-queue"; //$NON-NLS-1$

	public static String ELEM_PROPERY = "property"; //$NON-NLS-1$

	public static String ELEM_PUBLISH_SUBSCRIBE_CHANNEL = "publish-subscribe-channel"; //$NON-NLS-1$

	public static String ELEM_PUBLISHER = "publisher"; //$NON-NLS-1$

	public static String ELEM_PUBLISHING_INTERCEPTOR = "publishing-interceptor"; //$NON-NLS-1$

	public static String ELEM_QUEUE = "queue"; //$NON-NLS-1$

	public static String ELEM_RECIPIENT = "recipient"; //$NON-NLS-1$

	public static String ELEM_RECIPIENT_LIST_ROUTER = "recipient-list-router"; //$NON-NLS-1$

	public static String ELEM_REF = "ref"; //$NON-NLS-1$

	public static String ELEM_RENDEZVOUS_QUEUE = "rendezvous-queue"; //$NON-NLS-1$

	public static String ELEM_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ELEM_RESEQUENCER = "resequencer"; //$NON-NLS-1$

	public static String ELEM_RESOURCE_INBOUND_CHANNEL_ADAPTER = "resource-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_ROUTER = "router"; //$NON-NLS-1$

	public static String ELEM_SCHEDULED_PRODUCER = "scheduled-producer"; //$NON-NLS-1$

	public static String ELEM_SELECTOR = "selector"; //$NON-NLS-1$

	public static String ELEM_SELECTOR_CHAIN = "selector-chain"; //$NON-NLS-1$

	public static String ELEM_SERVICE_ACTIVATOR = "service-activator"; //$NON-NLS-1$

	public static String ELEM_SPLITTER = "splitter"; //$NON-NLS-1$

	public static String ELEM_SYSLOG_TO_MAP_TRANSFORMER = "syslog-to-map-transformer"; //$NON-NLS-1$

	public static String ELEM_THREAD_LOCAL_CHANNEL = "thread-local-channel"; //$NON-NLS-1$

	public static String ELEM_THREAD_POOL_TASK_EXECUTOR = "thread-pool-task-executor"; //$NON-NLS-1$

	public static String ELEM_TRANSACTIONAL = "transactional"; //$NON-NLS-1$

	public static String ELEM_TRANSFORMER = "transformer"; //$NON-NLS-1$

	public static String ELEM_WIRE_TAP = "wire-tap"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_APPLY_SEQUENCE = "apply-sequence"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_BEAN = "bean"; //$NON-NLS-1$

	public static String ATTR_CAPACITY = "capacity"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHANNEL_RESOLVER = "channel-resolver"; //$NON-NLS-1$

	public static String ATTR_COMPARATOR = "comparator"; //$NON-NLS-1$

	public static String ATTR_COMPLETION_STRATEGY = "completion-strategy"; //$NON-NLS-1$

	public static String ATTR_COMPLETION_STRATEGY_METHOD = "completion-strategy-method"; //$NON-NLS-1$

	public static String ATTR_CORE_SIZE = "core-size"; //$NON-NLS-1$

	public static String ATTR_CORRELATION_STRATEGY = "correlation-strategy"; //$NON-NLS-1$

	public static String ATTR_CORRELATION_STRATEGY_METHOD = "correlation-strategy-method"; //$NON-NLS-1$

	public static String ATTR_CRON = "cron"; //$NON-NLS-1$

	public static String ATTR_DATATYPE = "datatype"; //$NON-NLS-1$

	public static String ATTR_DEFAULT = "default"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_CHANNEL = "default-channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_DELAY = "default-delay"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OUTPUT_CHANNEL = "default-output-channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REPLY_CHANNEL = "default-reply-channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REPLY_TIMEOUT = "default-reply-timeout"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REQUEST_CHANNEL = "default-request-channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_REQUEST_TIMEOUT = "default-request-timeout"; //$NON-NLS-1$

	public static String ATTR_DELAY_HEADER_NAME = "delay-header-name"; //$NON-NLS-1$

	public static String ATTR_DISCARD_CHANNEL = "discard-channel"; //$NON-NLS-1$

	public static String ATTR_DISPATCHER = "dispatcher"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_ERROR_HANDLER = "error-handler"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_FAILOVER = "failover"; //$NON-NLS-1$

	public static String ATTR_FILTER = "filter"; //$NON-NLS-1$

	public static String ATTR_FIXED_DELAY = "fixed-delay"; //$NON-NLS-1$

	public static String ATTR_FIXED_RATE = "fixed-rate"; //$NON-NLS-1$

	public static String ATTR_HEADER_NAME = "header-name"; //$NON-NLS-1$

	public static String ATTR_HEADERS = "headers"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IGNORE_CHANNEL_NAME_RESOLUTION_FAILURES = "ignore-channel-name-resolution-failures"; //$NON-NLS-1$

	public static String ATTR_IGNORE_FAILURES = "ignore-failures"; //$NON-NLS-1$

	public static String ATTR_IGNORE_SEND_FAILURES = "ignore-send-failures"; //$NON-NLS-1$

	public static String ATTR_INITIAL_DELAY = "initial-delay"; //$NON-NLS-1$

	public static String ATTR_INTERVAL = "interval"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_ISOLATION = "isolation"; //$NON-NLS-1$

	public static String ATTR_KEEP_ALIVE_SECONDS = "keep-alive-seconds"; //$NON-NLS-1$

	public static String ATTR_LEVEL = "level"; //$NON-NLS-1$

	public static String ATTR_LOAD_BALANCER = "load-balancer"; //$NON-NLS-1$

	public static String ATTR_LOG_FULL_MESSAGE = "log-full-message"; //$NON-NLS-1$

	public static String ATTR_MAX_MESSAGES_PER_POLL = "max-messages-per-poll"; //$NON-NLS-1$

	public static String ATTR_MAX_SIZE = "max-size"; //$NON-NLS-1$

	public static String ATTR_METHOD = "method"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_OVERWRITE = "overwrite"; //$NON-NLS-1$

	public static String ATTR_PATTERN = "pattern"; //$NON-NLS-1$

	public static String ATTR_PATTERN_RESOLVER = "pattern-resolver"; //$NON-NLS-1$

	public static String ATTR_PAYLOAD = "payload";//$NON-NLS-1$

	public static String ATTR_PROPAGATION = "propagation"; //$NON-NLS-1$

	public static String ATTR_QUEUE_CAPACITY = "queue-capacity"; //$NON-NLS-1$

	public static String ATTR_READ_ONLY = "read-only"; //$NON-NLS-1$

	public static String ATTR_REAPER_INTERVAL = "reaper-interval"; //$NON-NLS-1$

	public static String ATTR_RECEIVE_TIMEOUT = "receive-timeout"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REJECTION_POLICY = "rejection-policy"; //$NON-NLS-1$

	public static String ATTR_RELEASE_PARTIAL_SEQUENCES = "release-partial-sequences"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_PAYLOAD_EXPRESSION = "request-payload-expression"; //$NON-NLS-1$

	public static String ATTR_REQUEST_TIMEOUT = "request-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUIRES_REPLY = "requires-reply"; //$NON-NLS-1$

	public static String ATTR_RESOLUTION_REQUIRED = "resolution-required"; //$NON-NLS-1$

	public static String ATTR_SCHEDULER = "scheduler"; //$NON-NLS-1$

	public static String ATTR_SELECTOR = "selector"; //$NON-NLS-1$

	public static String ATTR_SEND_PARTIAL_RESULT_ON_TIMEOUT = "send-partial-result-on-timeout"; //$NON-NLS-1$

	public static String ATTR_SEND_TIMEOUT = "send-timeout"; //$NON-NLS-1$

	public static String ATTR_SERVICE_INTERFACE = "service-interface"; //$NON-NLS-1$

	public static String ATTR_SHOULD_CLONE_PAYLOAD = "should-clone-payload"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_THROW_EXCEPTION_ON_REJECTION = "throw-exception-on-rejection"; //$NON-NLS-1$

	public static String ATTR_TIME_UNIT = "time-unit"; //$NON-NLS-1$

	public static String ATTR_TIMEOUT = "timeout"; //$NON-NLS-1$

	public static String ATTR_TRACKED_CORRELATION_ID_CAPACITY = "tracked-correlation-id-capacity"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

	public static String ATTR_TRIGGER = "trigger"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

	public static String ATTR_VOTING_STRATEGY = "voting-strategy"; //$NON-NLS-1$

	public static String ATTR_WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = "wait-for-tasks-to-complete-on-shutdown"; //$NON-NLS-1$

}
