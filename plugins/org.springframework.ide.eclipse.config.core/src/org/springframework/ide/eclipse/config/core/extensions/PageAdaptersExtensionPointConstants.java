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
 * This extension point is used to contribute support for additional namespaces
 * into existing {@link AbstractConfigEditor} form pages. Each contribution is
 * intended to represent a single namespace in a Spring configuration XML file.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class PageAdaptersExtensionPointConstants {

	public static String POINT_ID = "com.springsource.sts.config.ui.pageAdapters"; //$NON-NLS-1$

	public static String ELEM_ADAPTER = "adapter"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_NAMESPACE_URI = "namespaceUri"; //$NON-NLS-1$

	public static String ATTR_PARENT_URI = "parentUri"; //$NON-NLS-1$

	public static String ATTR_DETAILS_FACTORY = "detailsFactory"; //$NON-NLS-1$

	public static String ATTR_MODEL_FACTORY = "modelFactory"; //$NON-NLS-1$

	public static String ATTR_EDITPART_FACTORY = "editpartFactory"; //$NON-NLS-1$

	public static String ATTR_PALETTE_FACTORY = "paletteFactory"; //$NON-NLS-1$

	public static String ATTR_LABEL = "label"; //$NON-NLS-1$

}
