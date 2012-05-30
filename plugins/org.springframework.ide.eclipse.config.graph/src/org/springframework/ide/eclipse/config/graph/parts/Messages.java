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
package org.springframework.ide.eclipse.config.graph.parts;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.graph.parts.messages"; //$NON-NLS-1$

	public static String AbstractConfigEditPartFactory_ERROR_CREATING_GRAPH;

	public static String AbstractConfigPaletteFactory_CONTROL_GROUP_TITLE;

	public static String AbstractConfigPaletteFactory_ERROR_CREATING_PALETTE;

	public static String StructuredActivityPart_ERROR_OPENING_VIEW;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
