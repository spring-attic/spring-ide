/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.console;

import org.cloudfoundry.client.lib.domain.ApplicationLog.MessageType;
import org.eclipse.swt.SWT;

public class LogType {
	/*
	 * CF Loggregator types
	 */
	public static final LogType CFSTDERROR = new LogType(MessageType.STDERR, SWT.COLOR_RED);
	public static final LogType CFSTDOUT = new LogType(MessageType.STDOUT, SWT.COLOR_DARK_GREEN);

	/*
	 * Local messages types
	 *
	 */
	public static final LogType LOCALSTDOUT = new LogType(SWT.COLOR_DARK_BLUE);
	public static final LogType LOCALSTDEROR = new LogType(SWT.COLOR_RED);

	public static final LogType[] LOGGREGATORTYPES = { CFSTDOUT, CFSTDERROR };

	private final MessageType type;
	private final int displayColour;

	public LogType(MessageType type, int displayColour) {
		this.displayColour = displayColour;
		this.type = type;
	}

	public LogType(int displayColour) {
		this(null, displayColour);
	}

	public MessageType getMessageType() {
		return this.type;
	}

	public int getDisplayColour() {
		return this.displayColour;
	}

	public static LogType getLoggregatorType(MessageType type) {
		for (LogType config : LOGGREGATORTYPES) {
			if (config.getMessageType() != null && config.getMessageType().equals(type)) {
				return config;
			}
		}
		return CFSTDOUT;
	}

}
