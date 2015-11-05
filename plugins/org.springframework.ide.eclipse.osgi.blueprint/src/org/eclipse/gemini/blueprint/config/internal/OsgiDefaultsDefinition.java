/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal;

import org.eclipse.gemini.blueprint.config.internal.util.ReferenceParsingUtil;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class containing osgi defaults.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiDefaultsDefinition {

	private static final String SDM_NS = "http://www.springframework.org/schema/osgi";
	private static final String EGB_NS = "http://www.eclipse.org/gemini/blueprint/schema/blueprint";
	
	private static final String DEFAULT_TIMEOUT = "default-timeout";
	private static final String DEFAULT_AVAILABILITY = "default-availability";
	private static final String DEFAULT_CARDINALITY = "default-cardinality";
	private static final String TIMEOUT_DEFAULT = "300000";

	/** Default value */
	private String timeout = TIMEOUT_DEFAULT;
	/** Default value */
	private Availability availability = Availability.MANDATORY;

	public OsgiDefaultsDefinition(Document document, ParserContext parserContext) {
		Assert.notNull(document);
		Element root = document.getDocumentElement();

		ReferenceParsingUtil.checkAvailabilityAndCardinalityDuplication(root, DEFAULT_AVAILABILITY,
				DEFAULT_CARDINALITY, parserContext);

		parseDefaults(root, EGB_NS);
		parseDefaults(root, SDM_NS);
	}

	private void parseDefaults(Element root, String namespace) {
		String timeout = getAttribute(root, namespace, DEFAULT_TIMEOUT);

		if (StringUtils.hasText(timeout)) {
			setTimeout(timeout);
		}

		String availability = getAttribute(root, namespace, DEFAULT_AVAILABILITY);

		if (StringUtils.hasText(availability)) {
			setAvailability(ReferenceParsingUtil.determineAvailability(availability));
		}

		String cardinality = getAttribute(root, namespace, DEFAULT_CARDINALITY);

		if (StringUtils.hasText(cardinality)) {
			setAvailability(ReferenceParsingUtil.determineAvailabilityFromCardinality(cardinality));
		}
	}

	public String getTimeout() {
		return timeout;
	}

	protected void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public Availability getAvailability() {
		return availability;
	}

	protected void setAvailability(Availability availability) {
		this.availability = availability;
	}

	protected String getAttribute(Element root, String ns, String attributeName) {
		String value = root.getAttributeNS(ns, attributeName);
		return (!StringUtils.hasText(value) ? root.getAttribute(attributeName) : value);
	}
}