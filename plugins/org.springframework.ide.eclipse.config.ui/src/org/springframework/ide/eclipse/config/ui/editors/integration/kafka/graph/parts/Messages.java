/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.parts;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.ui.editors.integration.kafka.graph.parts.messages"; //$NON-NLS-1$

	public static String IntKafkaPaletteFactory_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntKafkaPaletteFactory_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntKafkaPaletteFactory_MESSAGE_DRIVEN_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}