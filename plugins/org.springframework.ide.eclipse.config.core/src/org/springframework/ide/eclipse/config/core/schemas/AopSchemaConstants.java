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
 * AOP schema derived from
 * <code>http://www.springframework.org/schema/aop/spring-aop-2.5.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring AOP 2.5
 */
public class AopSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/aop"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ADVISOR = "advisor"; //$NON-NLS-1$

	public static String ELEM_AFTER = "after"; //$NON-NLS-1$

	public static String ELEM_AFTER_RETURNING = "after-returning"; //$NON-NLS-1$

	public static String ELEM_AFTER_THROWING = "after-throwing"; //$NON-NLS-1$

	public static String ELEM_AROUND = "around"; //$NON-NLS-1$

	public static String ELEM_ASPECT = "aspect"; //$NON-NLS-1$

	public static String ELEM_ASPECTJ_AUTOPROXY = "aspectj-autoproxy"; //$NON-NLS-1$

	public static String ELEM_BEFORE = "before"; //$NON-NLS-1$

	public static String ELEM_CONFIG = "config"; //$NON-NLS-1$

	public static String ELEM_DECLARE_PARENTS = "declare-parents"; //$NON-NLS-1$

	public static String ELEM_INCLUDE = "include"; //$NON-NLS-1$

	public static String ELEM_POINTCUT = "pointcut"; //$NON-NLS-1$

	public static String ELEM_SCOPED_PROXY = "scoped-proxy"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ADVICE_REF = "advice-ref"; //$NON-NLS-1$

	public static String ATTR_ARG_NAMES = "arg-names"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_IMPL = "default-impl"; //$NON-NLS-1$

	public static String ATTR_DELEGATE_REF = "delegate-ref"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IMPLEMENT_INTERFACE = "implement-interface"; //$NON-NLS-1$

	public static String ATTR_METHOD = "method"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ORDER = "order"; //$NON-NLS-1$

	public static String ATTR_POINTCUT = "pointcut"; //$NON-NLS-1$

	public static String ATTR_POINTCUT_REF = "pointcut-ref"; //$NON-NLS-1$

	public static String ATTR_PROXY_TARGET_CLASS = "proxy-target-class"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_RETURNING = "returning"; //$NON-NLS-1$

	public static String ATTR_THROWING = "throwing"; //$NON-NLS-1$

	public static String ATTR_TYPES_MATCHING = "types-matching"; //$NON-NLS-1$

}
