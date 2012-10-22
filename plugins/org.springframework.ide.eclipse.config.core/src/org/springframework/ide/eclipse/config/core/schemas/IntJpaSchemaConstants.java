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
 * Integration JPA adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/jpa/spring-integration-jpa-2.2.xsd</code>
 * @author Leo Dos Santos
 * @since STS 3.2.0
 * @version Spring Integration 2.2
 */
public class IntJpaSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/jpa"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_RETRIEVING_OUTBOUND_GATEWAY = "retrieving-outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_UPDATING_OUTBOUND_GATEWAY = "updating-outbound-gateway"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_DELETE_AFTER_POLL = "delete-after-poll"; //$NON-NLS-1$

	public static String ATTR_DELETE_IN_BATCH = "delete-in-batch"; //$NON-NLS-1$

	public static String ATTR_ENTITY_CLASS = "entity-class"; //$NON-NLS-1$

	public static String ATTR_ENTITY_MANAGER = "entity-manager"; //$NON-NLS-1$

	public static String ATTR_ENTITY_MANAGER_FACTORY = "entity-manager-factory"; //$NON-NLS-1$

	public static String ATTR_EXPECT_SINGLE_RESULT = "expect-single-result"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_JPA_OPERATIONS = "jpa-operations"; //$NON-NLS-1$

	public static String ATTR_JPA_QUERY = "jpa-query"; //$NON-NLS-1$

	public static String ATTR_MAX_NUMBER_OF_RESULTS = "max-number-of-results"; //$NON-NLS-1$

	public static String ATTR_NAMED_QUERY = "named-query"; //$NON-NLS-1$

	public static String ATTR_NATIVE_QUERY = "native-query"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_PARAMETER_SOURCE = "parameter-source"; //$NON-NLS-1$

	public static String ATTR_PARAMETER_SOURCE_FACTORY = "parameter-source-factory"; //$NON-NLS-1$

	public static String ATTR_PERSIST_MODE = "persist-mode"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_SEND_TIMEOUT = "send-timeout"; //$NON-NLS-1$

	public static String ATTR_USE_PAYLOAD_AS_PARAMETER_SOURCE = "use-payload-as-parameter-source"; //$NON-NLS-1$

}
