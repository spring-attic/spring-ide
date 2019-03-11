/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.schemas;

/**
 * Constants for MQTT Spring Integration extension.
 *
 * @author Alex Boyko
 *
 */
public class IntMqttSchemaConstants {

	// URI

	public static final String URI = "http://www.springframework.org/schema/integration/mqtt"; //$NON-NLS-1$

	// Element tags

	public static final String ELEM_INBOUND_CHANNEL_ADAPTER = "message-driven-channel-adapter"; //$NON-NLS-1$

	public static final String ELEM_OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter"; //$NON-NLS-1$

}
