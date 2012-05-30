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
 * Integration Groovy adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntGroovySchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/groovy"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_CONTROL_BUS = "control-bus"; //$NON-NLS-1$

	public static String ELEM_SCRIPT = "script"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_CUSTOMIZER = "customizer"; //$NON-NLS-1$

	public static String ATTR_LOCATION = "location"; //$NON-NLS-1$

	public static String ATTR_REFRESH_CHECK_DELAY = "refresh-check-delay"; //$NON-NLS-1$

}
