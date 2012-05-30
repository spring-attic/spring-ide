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
 * Integration IP adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/ip/spring-integration-ip-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntIpSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/ip"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_TCP_CONNECTION_FACTORY = "tcp-connection-factory"; //$NON-NLS-1$

	public static String ELEM_TCP_INBOUND_CHANNEL_ADAPTER = "tcp-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_TCP_INBOUND_GATEWAY = "tcp-inbound-gateway"; //$NON-NLS-1$

	public static String ELEM_TCP_OUTBOUND_CHANNEL_ADAPTER = "tcp-outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_TCP_OUTBOUND_GATEWAY = "tcp-outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_UDP_INBOUND_CHANNEL_ADAPTER = "udp-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_UDP_OUTBOUND_CHANNEL_ADAPTER = "udp-outbound-channel-adapter"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACK_HOST = "ack-host"; //$NON-NLS-1$

	public static String ATTR_ACK_PORT = "ack-port"; //$NON-NLS-1$

	public static String ATTR_ACK_TIMEOUT = "ack-timemout"; //$NON-NLS-1$

	public static String ATTR_ACKNOWLEDGE = "acknowledge"; //$NON-NLS-1$

	public static String ATTR_APPLY_SEQUENCE = "apply-sequence"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHECK_LENGTH = "check-length"; //$NON-NLS-1$

	public static String ATTR_CLIENT_MODE = "client-mode"; //$NON-NLS-1$

	public static String ATTR_CLOSE = "close"; //$NON-NLS-1$

	public static String ATTR_CONNECTION_FACTORY = "connection-factory"; //$NON-NLS-1$

	public static String ATTR_CUSTOM_SOCKET_READER_CLASS_NAME = "custom-socket-reader-class-name"; //$NON-NLS-1$

	public static String ATTR_CUSTOM_SOCKET_WRITER_CLASS_NAME = "custom-socket-writer-class-name"; //$NON-NLS-1$

	public static String ATTR_DESERIALIZER = "deserializer"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_HOST = "host"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INPUT_CONVERTER = "input-converter"; //$NON-NLS-1$

	public static String ATTR_INTERCEPTER_FACTORY_CHAIN = "interceptor-factory-chain"; //$NON-NLS-1$

	public static String ATTR_LOCAL_ADDRESS = "local-address"; //$NON-NLS-1$

	public static String ATTR_LOOKUP_HOST = "lookup-host"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_FORMAT = "message-format"; //$NON-NLS-1$

	public static String ATTR_MIN_ACKS_FOR_SUCCESS = "min-acks-for-success"; //$NON-NLS-1$

	public static String ATTR_MULTICAST = "multicast"; //$NON-NLS-1$

	public static String ATTR_MULTICAST_ADDRESS = "multicast-address"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CONVERTER = "output-converter"; //$NON-NLS-1$

	public static String ATTR_PHASE = "phase"; //$NON-NLS-1$

	public static String ATTR_POOL_SIZE = "pool-size"; //$NON-NLS-1$

	public static String ATTR_PORT = "port"; //$NON-NLS-1$

	public static String ATTR_PROTOCOL = "protocal"; //$NON-NLS-1$

	public static String ATTR_RECEIVE_BUFFER_SIZE = "receive-buffer-size"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_TIMEOUT = "request-timeout"; //$NON-NLS-1$

	public static String ATTR_RETRY_INTERVAL = "retry-interval"; //$NON-NLS-1$

	public static String ATTR_SCHEDULER = "scheduler"; //$NON-NLS-1$

	public static String ATTR_SERIALIZER = "serializer"; //$NON-NLS-1$

	public static String ATTR_SINGLE_USE = "single-use"; //$NON-NLS-1$

	public static String ATTR_SO_KEEP_ALIVE = "so-keep-alive"; //$NON-NLS-1$

	public static String ATTR_SO_LINGER = "so-linger"; //$NON-NLS-1$

	public static String ATTR_SO_RECEIVE_BUFFER_SIZE = "so-receive-buffer-size"; //$NON-NLS-1$

	public static String ATTR_SO_SEND_BUFFER_SIZE = "so-send-buffer-size"; //$NON-NLS-1$

	public static String ATTR_SO_TCP_NO_DELAY = "so-tcp-no-delay"; //$NON-NLS-1$

	public static String ATTR_SO_TIMEOUT = "so-timeout"; //$NON-NLS-1$

	public static String ATTR_SO_TRAFFIC_CLASS = "so-traffic-class"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TIME_TO_LIVE = "time-to-live"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_USING_DIRECT_BUFFERS = "using-direct-buffers"; //$NON-NLS-1$

	public static String ATTR_USING_NIO = "using-nio"; //$NON-NLS-1$

}
