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
 * Web Flow schema derived from:
 * <code>http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.3.4
 * @version Spring Web Flow 2.0
 */
public class WebFlowSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/webflow"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ACTION_STATE = "action-state"; //$NON-NLS-1$

	public static String ELEM_ATTRIBUTE = "attribute"; //$NON-NLS-1$

	public static String ELEM_BEAN = "bean"; //$NON-NLS-1$

	public static String ELEM_BEAN_IMPORT = "bean-import"; //$NON-NLS-1$

	public static String ELEM_BINDER = "binder"; //$NON-NLS-1$

	public static String ELEM_BINDING = "binding"; //$NON-NLS-1$

	public static String ELEM_DECISION_STATE = "decision-state"; //$NON-NLS-1$

	public static String ELEM_END_STATE = "end-state"; //$NON-NLS-1$

	public static String ELEM_EVALUATE = "evaluate"; //$NON-NLS-1$

	public static String ELEM_EXCEPTION_HANDLER = "exception-handler"; //$NON-NLS-1$

	public static String ELEM_FLOW = "flow"; //$NON-NLS-1$

	public static String ELEM_GLOBAL_TRANSITIONS = "global-transitions"; //$NON-NLS-1$

	public static String ELEM_IF = "if"; //$NON-NLS-1$

	public static String ELEM_INPUT = "input"; //$NON-NLS-1$

	public static String ELEM_ON_END = "on-end"; //$NON-NLS-1$

	public static String ELEM_ON_ENTRY = "on-entry"; //$NON-NLS-1$

	public static String ELEM_ON_EXIT = "on-exit"; //$NON-NLS-1$

	public static String ELEM_ON_RENDER = "on-render"; //$NON-NLS-1$

	public static String ELEM_ON_START = "on-start"; //$NON-NLS-1$

	public static String ELEM_OUTPUT = "output"; //$NON-NLS-1$

	public static String ELEM_PERSISTENCE_CONTEXT = "persistence-context"; //$NON-NLS-1$

	public static String ELEM_RENDER = "render"; //$NON-NLS-1$

	public static String ELEM_SECURED = "secured"; //$NON-NLS-1$

	public static String ELEM_SET = "set"; //$NON-NLS-1$

	public static String ELEM_SUBFLOW_STATE = "subflow-state"; //$NON-NLS-1$

	public static String ELEM_TRANSITION = "transition"; //$NON-NLS-1$

	public static String ELEM_VALUE = "value"; //$NON-NLS-1$

	public static String ELEM_VAR = "var"; //$NON-NLS-1$

	public static String ELEM_VIEW_STATE = "view-state"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ABSTRACT = "abstract"; //$NON-NLS-1$

	public static String ATTR_ATTRIBUTES = "attributes"; //$NON-NLS-1$

	public static String ATTR_BEAN = "bean"; //$NON-NLS-1$

	public static String ATTR_BIND = "bind"; //$NON-NLS-1$

	public static String ATTR_CLASS = "class"; //$NON-NLS-1$

	public static String ATTR_COMMIT = "commit"; //$NON-NLS-1$

	public static String ATTR_CONVERTER = "converter"; //$NON-NLS-1$

	public static String ATTR_ELSE = "else"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_FRAGMENTS = "fragments"; //$NON-NLS-1$

	public static String ATTR_HISTORY = "history"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_MATCH = "match"; //$NON-NLS-1$

	public static String ATTR_MODEL = "model"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ON = "on"; //$NON-NLS-1$

	public static String ATTR_ON_EXCEPTION = "on-exception"; //$NON-NLS-1$

	public static String ATTR_PARENT = "parent"; //$NON-NLS-1$

	public static String ATTR_POPUP = "popup"; //$NON-NLS-1$

	public static String ATTR_PROPERTY = "property"; //$NON-NLS-1$

	public static String ATTR_REDIRECT = "redirect"; //$NON-NLS-1$

	public static String ATTR_REQUIRED = "required"; //$NON-NLS-1$

	public static String ATTR_RESOURCE = "resource"; //$NON-NLS-1$

	public static String ATTR_RESULT = "result"; //$NON-NLS-1$

	public static String ATTR_RESULT_TYPE = "result-type"; //$NON-NLS-1$

	public static String ATTR_START_STATE = "start-state"; //$NON-NLS-1$

	public static String ATTR_SUBFLOW = "subflow"; //$NON-NLS-1$

	public static String ATTR_SUBFLOW_ATTRIBUTE_MAPPER = "subflow-attribute-mapper"; //$NON-NLS-1$

	public static String ATTR_TEST = "test"; //$NON-NLS-1$

	public static String ATTR_THEN = "then"; //$NON-NLS-1$

	public static String ATTR_TO = "to"; //$NON-NLS-1$

	public static String ATTR_TYPE = "type"; //$NON-NLS-1$

	public static String ATTR_VALIDATE = "validate"; //$NON-NLS-1$

	public static String ATTR_VALUE = "value"; //$NON-NLS-1$

	public static String ATTR_VIEW = "view"; //$NON-NLS-1$

	// Web Flow 1.x tags (incomplete)

	public static String ELEM_ACTION = "action"; //$NON-NLS-1$

	public static String ELEM_BEAN_ACTION = "bean-action"; //$NON-NLS-1$

	public static String ELEM_ARGUMENT = "argument"; //$NON-NLS-1$

	public static String ELEM_MAPPING = "mapping"; //$NON-NLS-1$

	public static String ELEM_START_STATE = "start-state"; //$NON-NLS-1$

	public static String ATTR_FLOW = "flow"; //$NON-NLS-1$

	public static String ATTR_FROM = "from"; //$NON-NLS-1$

	public static String ATTR_IDREF = "idref"; //$NON-NLS-1$

	public static String ATTR_METHOD = "method"; //$NON-NLS-1$

	public static String ATTR_PARAMETER_TYPE = "parameter-type"; //$NON-NLS-1$

}
