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
 * OSGi schema derived from
 * <code>http://www.springframework.org/schema/osgi/spring-osgi-1.1.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring OSGi 1.1
 */
public class OsgiSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/osgi"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_BUNDLE = "bundle"; //$NON-NLS-1$

	public static String ELEM_COMPARATOR = "comparator"; //$NON-NLS-1$

	public static String ELEM_INTERFACES = "interfaces"; //$NON-NLS-1$

	public static String ELEM_LIST = "list"; //$NON-NLS-1$

	public static String ELEM_LISTENER = "listener"; //$NON-NLS-1$

	public static String ELEM_NATURAL = "natural"; //$NON-NLS-1$

	public static String ELEM_REFERENCE = "reference"; //$NON-NLS-1$

	public static String ELEM_REGISTRATION_LISTENER = "registration-listener"; //$NON-NLS-1$

	public static String ELEM_SERVICE = "service"; //$NON-NLS-1$

	public static String ELEM_SERVICE_PROPERTIES = "service-properties"; //$NON-NLS-1$

	public static String ELEM_SET = "set"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACTION = "action"; //$NON-NLS-1$

	public static String ATTR_AUTO_EXPORT = "auto-export"; //$NON-NLS-1$

	public static String ATTR_BASIS = "basis"; //$NON-NLS-1$

	public static String ATTR_BEAN_NAME = "bean-name"; //$NON-NLS-1$

	public static String ATTR_BIND_METHOD = "bind-method"; //$NON-NLS-1$

	public static String ATTR_CARDINALITY = "cardinality"; //$NON-NLS-1$

	public static String ATTR_COMPARATOR_REF = "comparator-ref"; //$NON-NLS-1$

	public static String ATTR_CONTEXT_CLASS_LOADER = "context-class-loader"; //$NON-NLS-1$

	public static String ATTR_DEPENDS_ON = "depends-on"; //$NON-NLS-1$

	public static String ATTR_DESTROY_ACTION = "destroy-action"; //$NON-NLS-1$

	public static String ATTR_FILTER = "filter"; //$NON-NLS-1$

	public static String ATTR_GREEDY_PROXYING = "greedy-proxying"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INTERFACE = "interface"; //$NON-NLS-1$

	public static String ATTR_LOCATION = "location"; //$NON-NLS-1$

	public static String ATTR_MERGE = "merge"; //$NON-NLS-1$

	public static String ATTR_RANKING = "ranking"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REGISTRATION_METHOD = "registration-method"; //$NON-NLS-1$

	public static String ATTR_START_LEVEL = "start-level"; //$NON-NLS-1$

	public static String ATTR_SYMBOLIC_NAME = "symbolic-name"; //$NON-NLS-1$

	public static String ATTR_TIMEOUT = "timeout"; //$NON-NLS-1$

	public static String ATTR_UNBIND_ELEMENT = "unbind-element"; //$NON-NLS-1$

	public static String ATTR_UNREGISTRATION_METHOD = "unregistration-method"; //$NON-NLS-1$

}
