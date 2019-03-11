/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * https://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at https://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.		   - initial API and implementation
 *   Spring IDE Developers 
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.osgi.blueprint.internal.jaxb.Tavailability;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Blueprint namespace defaults for a given element/document.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 * 
 */
public class BlueprintDefaultsDefinition {

	private static final String BLUEPRINT_NS = "http://www.osgi.org/xmlns/blueprint/v1.0.0";
	private static final String DEFAULT_TIMEOUT = "default-timeout";
	private static final String DEFAULT_AVAILABILITY = "default-availability";
	private static final String TIMEOUT_DEFAULT = "300000";
	private static final String DEFAULT_INITIALIZATION = "default-activation";
	private static final String LAZY_INITIALIZATION = "lazy";
	private static final boolean INITIALIZATION_DEFAULT = false;

	/** Lazy flag */
	private boolean defaultInitialization;

	/** Default value */
	private String timeout = TIMEOUT_DEFAULT;
	/** Default value */
	private Tavailability availability = Tavailability.MANDATORY;

	/**
	 * Constructs a new <code>BlueprintDefaultsDefinition</code> instance.
	 * 
	 * @param parserContext
	 * 
	 * @param root
	 */
	public BlueprintDefaultsDefinition(Document doc, ParserContext parserContext) {
		Element root = doc.getDocumentElement();
		String timeout = getAttribute(root, BLUEPRINT_NS, DEFAULT_TIMEOUT);
		setTimeout(StringUtils.hasText(timeout) ? timeout.trim() : TIMEOUT_DEFAULT);

		String availability = getAttribute(root, BLUEPRINT_NS, DEFAULT_AVAILABILITY);
		if (StringUtils.hasText(availability)) {
			Tavailability avail = Tavailability.fromValue(availability);
			setAvailability(avail);
		}

		// default initialization
		String initialization = getAttribute(root, BLUEPRINT_NS, DEFAULT_INITIALIZATION);
		defaultInitialization = (StringUtils.hasText(initialization)
				? initialization.trim().equalsIgnoreCase(LAZY_INITIALIZATION) : INITIALIZATION_DEFAULT);
	}

	public boolean getDefaultInitialization() {
		return defaultInitialization;
	}

	public String getTimeout() {
		return timeout;
	}

	protected void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public Tavailability getAvailability() {
		return availability;
	}

	protected void setAvailability(Tavailability availability) {
		this.availability = availability;
	}

	protected String getAttribute(Element root, String ns, String attributeName) {
		String value = root.getAttributeNS(ns, attributeName);
		return (!StringUtils.hasText(value) ? root.getAttribute(attributeName) : value);
	}
}