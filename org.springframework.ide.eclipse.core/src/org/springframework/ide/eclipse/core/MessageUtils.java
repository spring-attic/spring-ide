/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import java.text.MessageFormat;

public final class MessageUtils {

	private MessageUtils() {
		// Not for instantiation
	}

	public static String format(String message, Object object) {
		return MessageFormat.format(message, new Object[] { object});
	}

	public static String format(String message, Object... objects) {
		return MessageFormat.format(message, objects);
	}
}
