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
package org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.parts;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.parts.messages"; //$NON-NLS-1$

	public static String IntXmlPaletteFactory_MARSHALLING_TRANSFORMER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_UNMARSHALLING_TRANSFORMER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_VALIDATING_FILTER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XPATH_FILTER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XPATH_HEADER_ENRICHER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XPATH_ROUTER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XPATH_SPLITTER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XPATH_TRANSFORMER_COMPONENT_DESCRIPTION;

	public static String IntXmlPaletteFactory_XSLT_TRANSFORMER_COMPONENT_DESCRIPTION;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
