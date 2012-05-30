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
 * Transaction schema derived from
 * <code>http://www.springframework.org/schema/tx/spring-tx-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Transaction 2.5
 */
public class TxSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/tx"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ADVICE = "advice"; //$NON-NLS-1$

	public static String ELEM_ANNOTATION_DRIVEN = "annotation-driven"; //$NON-NLS-1$

	public static String ELEM_ATTRIBUTES = "attributes"; //$NON-NLS-1$

	public static String ELEM_JTA_TRANSACTION_MANAGER = "jta-transaction-manager"; //$NON-NLS-1$

	public static String ELEM_METHOD = "method"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_ISOLATION = "isolation"; //$NON-NLS-1$

	public static String ATTR_MODE = "mode"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_NO_ROLLBACK_FOR = "no-rollback-for"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_PROPAGATION = "propagation"; //$NON-NLS-1$

	public static String ATTR_PROXY_TARGET_CLASS = "proxy-target-class"; //$NON-NLS-1$

	public static String ATTR_READ_ONLY = "read-only"; //$NON-NLS-1$

	public static String ATTR_ROLLBACK_FOR = "rollback-for"; //$NON-NLS-1$

	public static String ATTR_TIMEOUT = "timeout"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

}
