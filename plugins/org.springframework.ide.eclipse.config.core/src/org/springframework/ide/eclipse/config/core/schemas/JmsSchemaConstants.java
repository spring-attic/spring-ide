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
 * JMS schema derived from
 * <code>http://www.springframework.org/schema/jms/spring-jms-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring JMS 2.5
 */
public class JmsSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/jms"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_JCA_LISTENER_CONTAINER = "jca-listener-container"; //$NON-NLS-1$

	public static String ELEM_LISTENER = "listener"; //$NON-NLS-1$

	public static String ELEM_LISTENER_CONTAINER = "listener-container"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACKNOWLEDGE = "acknowledge"; //$NON-NLS-1$

	public static String ATTR_ACTIVATION_SPEC_FACTORY = "activation-spec-factory"; //$NON-NLS-1$

	public static String ATTR_CLIENT_ID = "client-id"; //$NON-NLS-1$

	public static String ATTR_CONCURRENCY = "concurrency"; //$NON-NLS-1$

	public static String ATTR_CONNECTION_FACTORY = "connection-factory"; //$NON-NLS-1$

	public static String ATTR_CONTAINER_TYPE = "container-type"; //$NON-NLS-1$

	public static String ATTR_DESTINATION = "destination"; //$NON-NLS-1$

	public static String ATTR_DESTINATION_RESOLVER = "destination-resolver"; //$NON-NLS-1$

	public static String ATTR_DESTINATION_TYPE = "destination-type"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_MESSAGE_CONVERTER = "message-converter"; //$NON-NLS-1$

	public static String ATTR_METHOD = "method"; //$NON-NLS-1$

	public static String ATTR_PREFETCH = "prefetch"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_RESOURCE_ADAPTER = "resource-adapter"; //$NON-NLS-1$

	public static String ATTR_RESPONSE_DESTINATION = "response-destination"; //$NON-NLS-1$

	public static String ATTR_SELECTOR = "selector"; //$NON-NLS-1$

	public static String ATTR_SUBSCRIPTION = "subscription"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

}
