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
 * Integration JDBC adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntJdbcSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/jdbc"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_MESSAGE_STORE = "message-store"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	public static String ELEM_PARAMETER = "parameter"; //$NON-NLS-1$

	public static String ELEM_RETURNING_RESULTSET = "returning-resultset"; //$NON-NLS-1$

	public static String ELEM_SQL_PARAMETER_DEFINITION = "sql-definition-parameter"; //$NON-NLS-1$

	public static String ELEM_STORED_PROC_INBOUND_CHANNEL_ADAPTER = "stored-proc-inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_STORED_PROC_OUTBOUND_CHANNEL_ADAPTER = "stored-proc-outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_STORED_PROC_OUTBOUND_GATEWAY = "stored-proc-outbound-gateway"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_DATA_SOURCE = "data-source"; //$NON-NLS-1$

	public static String ATTR_DESERIALIZER = "deserializer"; //$NON-NLS-1$

	public static String ATTR_DIRECTION = "direction"; //$NON-NLS-1$

	public static String ATTR_EXPECT_SINGLE_RESULT = "expect-single-result"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IGNORE_COLUMN_META_DATA = "ignore-column-meta-data"; //$NON-NLS-1$

	public static String ATTR_IS_FUNCTION = "is-function"; //$NON-NLS-1$

	public static String ATTR_JDBC_OPERATIONS = "jdbc-operations"; //$NON-NLS-1$

	public static String ATTR_JDBC_SOURCE = "jdbc-source"; //$NON-NLS-1$

	public static String ATTR_KEYS_GENERATED = "keys-generated"; //$NON-NLS-1$

	public static String ATTR_LOB_HANDLER = "lob-handler"; //$NON-NLS-1$

	public static String ATTR_MAX_ROWS_PER_POLL = "max-rows-per-poll"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_QUERY = "query"; //$NON-NLS-1$

	public static String ATTR_REGION = "region"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REPLY_SQL_PARAMETER_SOURCE_FACTORY = "reply-sql-parameter-source-factory"; //$NON-NLS-1$

	public static String ATTR_REPLY_TIMEOUT = "reply-timeout"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_SQL_PARAMETER_SOURCE_FACTORY = "request-sql-parameter-source-factory"; //$NON-NLS-1$

	public static String ATTR_RETURN_VALUE_REQUIRED = "return-value-required"; //$NON-NLS-1$

	public static String ATTR_ROW_MAPPER = "row-mapper"; //$NON-NLS-1$

	public static String ATTR_SCALE = "scale"; //$NON-NLS-1$

	public static String ATTR_SERIALIZER = "serializer"; //$NON-NLS-1$

	public static String ATTR_SIMPLE_JDBC_OPERATIONS = "simple-jdbc-operations"; //$NON-NLS-1$

	public static String ATTR_SKIP_UNDECLARED_RESULTS = "skip-undeclared-results"; //$NON-NLS-1$

	public static String ATTR_SQL_PARAMETER_SOURCE_FACTORY = "sql-parameter-source-factory"; //$NON-NLS-1$

	public static String ATTR_SQL_QUERY_PARAMETER_SOURCE = "sql-query-parameter-source"; //$NON-NLS-1$

	public static String ATTR_STORED_PROCEDURE_NAME = "stored-procedure-name"; //$NON-NLS-1$

	public static String ATTR_TABLE_PREFIX = "table-prefix"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_UPDATE = "update"; //$NON-NLS-1$

	public static String ATTR_UPDATE_PER_ROW = "update-per-row"; //$NON-NLS-1$

	public static String ATTR_USE_PAYLOAD_AS_PARAMETER_SOURCE = "use-payload-as-parameter-source"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

}
