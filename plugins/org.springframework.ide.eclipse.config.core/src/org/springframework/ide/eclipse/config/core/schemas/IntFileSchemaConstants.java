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
 * Integration File adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/file/spring-integration-file-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntFileSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/file"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_FILE_TO_BYTES_TRANSFORMER = "file-to-bytes-transformer"; //$NON-NLS-1$

	public static String ELEM_FILE_TO_STRING_TRANSFORMER = "file-to-string-transformer"; //$NON-NLS-1$

	public static String ELEM_INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_LOCKER = "locker"; //$NON-NLS-1$

	public static String ELEM_NIO_LOCKER = "nio-locker"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

	public static String ELEM_OUTBOUND_GATEWAY = "outbound-gateway"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_AUTO_CREATE_DIRECTORY = "auto-create-directory"; //$NON-NLS-1$

	public static String ATTR_AUTO_STARTUP = "auto-startup"; //$NON-NLS-1$

	public static String ATTR_CHANNEL = "channel"; //$NON-NLS-1$

	public static String ATTR_CHARSET = "charset"; //$NON-NLS-1$

	public static String ATTR_COMPARATOR = "comparator"; //$NON-NLS-1$

	public static String ATTR_DELETE_FILES = "delete-files"; //$NON-NLS-1$

	public static String ATTR_DELETE_SOURCE_FILES = "delete-source-files"; //$NON-NLS-1$

	public static String ATTR_DIRECTORY = "directory"; //$NON-NLS-1$

	public static String ATTR_FILENAME_GENERATOR = "filename-generator"; //$NON-NLS-1$

	public static String ATTR_FILENAME_GENERATOR_EXPRESSION = "filename-generator-expression"; //$NON-NLS-1$

	public static String ATTR_FILENAME_PATTERN = "filename-pattern"; //$NON-NLS-1$

	public static String ATTR_FILTER = "filter"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_PREVENT_DUPLICATES = "prevent-duplicates"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_QUEUE_SIZE = "queue-size"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REPLY_CHANNEL = "reply-channel"; //$NON-NLS-1$

	public static String ATTR_REQUEST_CHANNEL = "request-channel"; //$NON-NLS-1$

	public static String ATTR_SCANNER = "scanner"; //$NON-NLS-1$

	public static String ATTR_TEMPORARY_FILE_SUFFIX = "temporary-file-suffix"; //$NON-NLS-1$

}
