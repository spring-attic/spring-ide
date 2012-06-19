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
 * Faces schema derived from
 * <code>http://www.springframework.org/schema/faces/spring-faces-2.0.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Faces 2.0
 */
public class FacesSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/faces"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_FLOW_BUILDER_SERVICES = "flow-builder-services"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_CONVERSION_SERVICE = "conversion-service"; //$NON-NLS-1$

	public static String ATTR_ENABLE_MANAGED_BEANS = "enable-managed-beans"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION_PARSER = "expression-parser"; //$NON-NLS-1$

	public static String ATTR_FORMATTER_REGISTRY = "formatter-registry"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_VIEW_FACTORY_CREATOR = "view-factory-creator"; //$NON-NLS-1$

}
