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
 * Integration Redis adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/redis/spring-integration-redis-2.1.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.9.0
 * @version Spring Integration 2.1
 */
public class IntRedisSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/redis"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_INTERCEPTORS = "interceptors"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_PUBLISH_SUBSCRIBE_CHANNEL = "publish-subscribe-channel"; //$NON-NLS-1$

	public static String ELEM_STORE_INBOUND_CHANNEL_ADAPTER = "store-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_STORE_OUTBOUND_CHANNEL_ADAPTER = "store-outbound-channel-adapter"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_COLLECTION_TYPE = "collection-type"; //$NON-NLS-1$

	public static String ATTR_CONNECTION_FACTORY = "connection-factory"; //$NON-NLS-1$

	public static String ATTR_ERROR_CHANNEL = "error-channel"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD_ELEMENTS = "extract-payload-elements"; //$NON-NLS-1$

	public static String ATTR_HASH_KEY_SERIALIZER = "hash-key-serializer"; //$NON-NLS-1$

	public static String ATTR_HASH_VALUE_SERIALIZER = "hash-value-serializer"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_KEY = "key"; //$NON-NLS-1$

	public static String ATTR_KEY_EXPRESSION = "key-expression"; //$NON-NLS-1$

	public static String ATTR_KEY_SERIALIZER = "key-serializer"; //$NON-NLS-1$

	public static String ATTR_MAP_KEY_EXPRESSION = "map-key-expression"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_CONVERTER = "message-converter"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_REDIS_TEMPLATE = "redis-template"; //$NON-NLS-1$

	public static String ATTR_SERIALIZER = "serializer"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TOPIC_NAME = "topic-name"; //$NON-NLS-1$

	public static String ATTR_TOPICS = "topics"; //$NON-NLS-1$

	public static String ATTR_VALUE_SERIALIZER = "value-serializer"; //$NON-NLS-1$

}
