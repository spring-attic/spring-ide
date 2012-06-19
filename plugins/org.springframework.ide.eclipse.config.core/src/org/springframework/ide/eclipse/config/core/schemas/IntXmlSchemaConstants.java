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
 * Integration XML adapter schema derived from
 * <code>http://www.springframework.org/schema/integration/xml/spring-integration-xml-2.0.xsd</code>
 * @author Leo Dos Santos
 * @since STS 2.5.0
 * @version Spring Integration 2.0
 */
public class IntXmlSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/integration/xml"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_HEADER = "header"; //$NON-NLS-1$

	public static String ELEM_MARSHALLING_TRANSFORMER = "marshalling-transformer"; //$NON-NLS-1$

	public static String ELEM_UNMARSHALLING_TRANSFORMER = "unmarshalling-transformer"; //$NON-NLS-1$

	public static String ELEM_VALIDATING_FILTER = "validating-filter"; //$NON-NLS-1$

	public static String ELEM_XPATH_EXPRESSION = "xpath-expression"; //$NON-NLS-1$

	public static String ELEM_XPATH_FILTER = "xpath-filter"; //$NON-NLS-1$

	public static String ELEM_XPATH_HEADER_ENRICHER = "xpath-header-enricher"; //$NON-NLS-1$

	public static String ELEM_XPATH_ROUTER = "xpath-router"; //$NON-NLS-1$

	public static String ELEM_XPATH_SELECTOR = "xpath-selector"; //$NON-NLS-1$

	public static String ELEM_XPATH_SPLITTER = "xpath-splitter"; //$NON-NLS-1$

	public static String ELEM_XPATH_TRANSFORMER = "xpath-transformer"; //$NON-NLS-1$

	public static String ELEM_XSLT_TRANSFORMER = "xslt-transformer"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_CHANNEL_RESOLVER = "channel-resolver"; //$NON-NLS-1$

	public static String ATTR_CONVERTER = "converter"; //$NON-NLS-1$

	public static String ATTR_CREATE_DOCUMENTS = "create-documents"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OUTPUT_CHANNEL = "default-output-channel"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_OVERWRITE = "default-overwrite"; //$NON-NLS-1$

	public static String ATTR_DISCARD_CHANNEL = "discard-channel"; //$NON-NLS-1$

	public static String ATTR_DOC_BUILDER_FACTORY = "doc-builder-factory"; //$NON-NLS-1$

	public static String ATTR_EVALUATION_RESULT_TYPE = "evaluation-result-type"; //$NON-NLS-1$

	public static String ATTR_EVALUATION_TYPE = "evaluation-type"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_EXTRACT_PAYLOAD = "extract-payload"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_IGNORE_CHANNEL_NAME_RESOLUTION_FAILURES = "ignore-channel-name-resolution-failures"; //$NON-NLS-1$

	public static String ATTR_INPUT_CHANNEL = "input-channel"; //$NON-NLS-1$

	public static String ATTR_INVALID_CHANNEL = "invalid-channel"; //$NON-NLS-1$

	public static String ATTR_MARSHALLER = "marshaller"; //$NON-NLS-1$

	public static String ATTR_MATCH_VALUE = "match-value"; //$NON-NLS-1$

	public static String ATTR_MATCH_TYPE = "match-type"; //$NON-NLS-1$

	public static String ATTR_MULTI_CHANNEL = "multi-channel"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_NAMESPACE_MAP = "namespace-map"; //$NON-NLS-1$

	public static String ATTR_NODE_MAPPER = "node-mapper"; //$NON-NLS-1$

	public static String ATTR_NS_PREFIX = "ns-prefix"; //$NON-NLS-1$

	public static String ATTR_NS_URI = "ns-uri"; //$NON-NLS-1$

	public static String ATTR_OUTPUT_CHANNEL = "output-channel"; //$NON-NLS-1$

	public static String ATTR_OVERWRITE = "overwrite"; //$NON-NLS-1$

	public static String ATTR_RESOLUTION_REQUIRED = "resolution-required"; //$NON-NLS-1$

	public static String ATTR_RESULT_FACTORY = "result-factory"; //$NON-NLS-1$

	public static String ATTR_RESULT_TRANSFORMER = "result-transformer"; //$NON-NLS-1$

	public static String ATTR_RESULT_TYPE = "result-type"; //$NON-NLS-1$

	public static String ATTR_SCHEMA_LOCATION = "schema-location"; //$NON-NLS-1$

	public static String ATTR_SCHEMA_TYPE = "schema-type"; //$NON-NLS-1$

	public static String ATTR_SHOULD_SKIP_NULLS = "should-skip-nulls"; //$NON-NLS-1$

	public static String ATTR_STRING_TEST_VALUE = "string-test-value"; //$NON-NLS-1$

	public static String ATTR_SOURCE_FACTORY = "source-factory"; //$NON-NLS-1$

	public static String ATTR_THROW_EXCEPTION_ON_REJECTION = "throw-exception-on-rejection"; //$NON-NLS-1$

	public static String ATTR_UNMARSHALLER = "unmarshaller"; //$NON-NLS-1$

	public static String ATTR_VALID_CHANNEL = "valid-channel"; //$NON-NLS-1$

	public static String ATTR_XML_VALIDATOR = "xml-validator"; //$NON-NLS-1$

	public static String ATTR_XPATH_EXPRESSION = "xpath-expression"; //$NON-NLS-1$

	public static String ATTR_XPATH_EXPRESSION_REF = "xpath-expression-ref"; //$NON-NLS-1$

	public static String ATTR_XSL_RESOURCE = "xsl-resource"; //$NON-NLS-1$

	public static String ATTR_XSL_TEMPLATES = "xsl-templates"; //$NON-NLS-1$

	public static String ATTR_XSLT_PARAM_HEADERS = "xslt-param-headers"; //$NON-NLS-1$

}
