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
 * Beans schema derived from
 * <code>http://www.springframework.org/schema/beans/spring-beans-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Beans 2.5
 */
public class BeansSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/beans"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ALIAS = "alias"; //$NON-NLS-1$

	public static String ELEM_ARG_TYPE = "arg-type"; //$NON-NLS-1$

	public static String ELEM_ATTRIBUTE = "attribute"; //$NON-NLS-1$

	public static String ELEM_BEAN = "bean"; //$NON-NLS-1$

	public static String ELEM_BEANS = "beans"; //$NON-NLS-1$

	public static String ELEM_CONSTRUCTOR_ARG = "constructor-arg"; //$NON-NLS-1$

	public static String ELEM_DESCRIPTION = "description"; //$NON-NLS-1$

	public static String ELEM_ENTRY = "entry"; //$NON-NLS-1$

	public static String ELEM_IDREF = "idref"; //$NON-NLS-1$

	public static String ELEM_IMPORT = "import"; //$NON-NLS-1$

	public static String ELEM_KEY = "key"; //$NON-NLS-1$

	public static String ELEM_LIST = "list"; //$NON-NLS-1$

	public static String ELEM_LOOKUP_METHOD = "lookup-method"; //$NON-NLS-1$

	public static String ELEM_MAP = "map"; //$NON-NLS-1$

	public static String ELEM_META = "meta"; //$NON-NLS-1$

	public static String ELEM_NULL = "null"; //$NON-NLS-1$

	public static String ELEM_PROP = "prop"; //$NON-NLS-1$

	public static String ELEM_PROPERTY = "property"; //$NON-NLS-1$

	public static String ELEM_PROPS = "props"; //$NON-NLS-1$

	public static String ELEM_QUALIFIER = "qualifier"; //$NON-NLS-1$

	public static String ELEM_REF = "ref"; //$NON-NLS-1$

	public static String ELEM_REPLACED_METHOD = "replaced-method"; //$NON-NLS-1$

	public static String ELEM_SET = "set"; //$NON-NLS-1$

	public static String ELEM_VALUE = "value"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ABSTRACT = "abstract"; //$NON-NLS-1$

	public static String ATTR_ALIAS = "alias"; //$NON-NLS-1$

	public static String ATTR_AUTOWIRE = "autowire"; //$NON-NLS-1$

	public static String ATTR_AUTOWIRE_CANDIDATE = "autowire-candidate"; //$NON-NLS-1$

	public static String ATTR_BEAN = "bean"; //$NON-NLS-1$

	public static String ATTR_CLASS = "class"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_AUTOWIRE = "default-autowire"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_AUTOWIRE_CANDIDATES = "default-autowire-candidates"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_DEPENDENCY_CHECK = "default-dependency-check"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_DESTROY_METHOD = "default-destroy-method"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_INIT_METHOD = "default-init-method"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_LAZY_INIT = "default-lazy-init"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_MERGE = "default-merge"; //$NON-NLS-1$

	public static String ATTR_DEPENDENCY_CHECK = "dependency-check"; //$NON-NLS-1$

	public static String ATTR_DEPENDS_ON = "depends-on"; //$NON-NLS-1$

	public static String ATTR_DESTROY_METHOD = "destroy-method"; //$NON-NLS-1$

	public static String ATTR_FACTORY_BEAN = "factory-bean"; //$NON-NLS-1$

	public static String ATTR_FACTORY_METHOD = "factory-method"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INDEX = "index"; //$NON-NLS-1$

	public static String ATTR_INIT_METHOD = "init-method"; //$NON-NLS-1$

	public static String ATTR_KEY = "key"; //$NON-NLS-1$

	public static String ATTR_KEY_REF = "key-ref"; //$NON-NLS-1$

	public static String ATTR_KEY_TYPE = "key-type"; //$NON-NLS-1$

	public static String ATTR_LAZY_INIT = "lazy-init"; //$NON-NLS-1$

	public static String ATTR_LOCAL = "local"; //$NON-NLS-1$

	public static String ATTR_MATCH = "match"; //$NON-NLS-1$

	public static String ATTR_MERGE = "merge"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_PARENT = "parent"; //$NON-NLS-1$

	public static String ATTR_PRIMARY = "primary"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REPLACER = "replacer"; //$NON-NLS-1$

	public static String ATTR_RESOURCE = "resource"; //$NON-NLS-1$

	public static String ATTR_SCOPE = "scope"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

	public static String ATTR_VALUE_REF = "value-ref"; //$NON-NLS-1$

	public static String ATTR_VALUE_TYPE = "value-type"; //$NON-NLS-1$

}
