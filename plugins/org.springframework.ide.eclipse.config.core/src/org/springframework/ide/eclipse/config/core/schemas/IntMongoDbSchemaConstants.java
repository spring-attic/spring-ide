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
 * Integration MongoDB adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/mongodb/spring-integration-mongodb-2.2.xsd</code>
 * @author Leo Dos Santos
 * @since STS 3.2.0
 * @version Spring Integration 2.2
 */
public class IntMongoDbSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/mongodb"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_COLLECTION_NAME = "collection-name"; //$NON-NLS-1$

	public static String ATTR_COLLECTION_NAME_EXPRESSION = "collection-name-expression"; //$NON-NLS-1$

	public static String ATTR_ENTITY_CLASS = "entity-class"; //$NON-NLS-1$

	public static String ATTR_EXPECT_SINGLE_RESULT = "expect-single-result"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_MONGO_CONVERTER = "mongo-converter"; //$NON-NLS-1$

	public static String ATTR_MONGO_TEMPLATE = "mongo-template"; //$NON-NLS-1$

	public static String ATTR_MONGODB_FACTORY = "mongodb-factory"; //$NON-NLS-1$

	public static String ATTR_QUERY = "query"; //$NON-NLS-1$

	public static String ATTR_QUERY_EXPRESSION = "query-expression"; //$NON-NLS-1$

}
