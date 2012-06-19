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
package org.springframework.ide.eclipse.config.core.extensions;

/**
 * This extension point is used to add new form pages to an
 * {@link AbstractConfigEditor}. Each page added through this extension point is
 * intended to represent a single namespace in a Spring configuration XML file.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 */
public class FormPagesExtensionPointConstants {

	public static String POINT_ID = "com.springsource.sts.config.ui.formPages"; //$NON-NLS-1$

	public static String ELEM_FORM_PAGE = "formPage"; //$NON-NLS-1$

	public static String ATTR_CLASS = "class"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_NAMESPACE_PREFIX = "namespacePrefix"; //$NON-NLS-1$

	public static String ATTR_NAMESPACE_URI = "namespaceUri"; //$NON-NLS-1$

	public static String ATTR_GRAPH = "graph"; //$NON-NLS-1$

}
