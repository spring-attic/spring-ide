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
 * Integration Security adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/security/spring-integration-security-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntSecuritySchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/security"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ACCESS_POLICY = "access-policy"; //$NON-NLS-1$

	public static String ELEM_SECURED_CHANNELS = "secured-channels"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACCESS_DECISION_MANAGER = "access-decision-manager"; //$NON-NLS-1$

	public static String ATTR_AUTHENTICATION_MANAGER = "authentication-manager"; //$NON-NLS-1$

	public static String ATTR_PATTERN = "pattern"; //$NON-NLS-1$

	public static String ATTR_RECEIVE_ACCESS = "receive-access"; //$NON-NLS-1$

	public static String ATTR_SEND_ACCESS = "send-access"; //$NON-NLS-1$

}
