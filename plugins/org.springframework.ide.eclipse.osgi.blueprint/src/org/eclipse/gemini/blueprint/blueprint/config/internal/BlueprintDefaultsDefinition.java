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

package org.eclipse.gemini.blueprint.blueprint.config.internal;

import org.eclipse.gemini.blueprint.config.internal.OsgiDefaultsDefinition;
import org.eclipse.gemini.blueprint.config.internal.util.ReferenceParsingUtil;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Blueprint namespace defaults for a given element/document.
 * 
 * @author Costin Leau
 * 
 */
public class BlueprintDefaultsDefinition extends OsgiDefaultsDefinition {

	private static final String BLUEPRINT_NS = "http://www.osgi.org/xmlns/blueprint/v1.0.0";
	private static final String DEFAULT_TIMEOUT = "default-timeout";
	private static final String DEFAULT_AVAILABILITY = "default-availability";
	private static final String TIMEOUT_DEFAULT = "300000";
	private static final String DEFAULT_INITIALIZATION = "default-activation";
	private static final String LAZY_INITIALIZATION = "lazy";
	private static final boolean INITIALIZATION_DEFAULT = false;

	/** Lazy flag */
	private boolean defaultInitialization;

	/**
	 * Constructs a new <code>BlueprintDefaultsDefinition</code> instance.
	 * @param parserContext
	 * 
	 * @param root
	 */
	public BlueprintDefaultsDefinition(Document doc, ParserContext parserContext) {
		super(doc, parserContext);
		Element root = doc.getDocumentElement();
		String timeout = getAttribute(root, BLUEPRINT_NS, DEFAULT_TIMEOUT);
		setTimeout(StringUtils.hasText(timeout) ? timeout.trim() : TIMEOUT_DEFAULT);

		String availability = getAttribute(root, BLUEPRINT_NS, DEFAULT_AVAILABILITY);
		if (StringUtils.hasText(availability)) {
			Availability avail = ReferenceParsingUtil.determineAvailability(availability);
			setAvailability(avail);
		}

		// default initialization
		String initialization = getAttribute(root, BLUEPRINT_NS, DEFAULT_INITIALIZATION);
		defaultInitialization =
				(StringUtils.hasText(initialization) ? initialization.trim().equalsIgnoreCase(LAZY_INITIALIZATION)
						: INITIALIZATION_DEFAULT);
	}

	public boolean getDefaultInitialization() {
		return defaultInitialization;
	}
}