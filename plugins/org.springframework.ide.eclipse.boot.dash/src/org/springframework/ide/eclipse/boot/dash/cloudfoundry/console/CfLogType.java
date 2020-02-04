/*******************************************************************************
 * Copyright (c) 2015-2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.console;

import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.doppler.MessageType;
import org.eclipse.swt.SWT;

public class CfLogType {

	public static LogType getLogType(LogMessage message) {
		for (LogType type : CfLogType.CFLOG_TYPES) {
			if (type.getMessageType() != null && type.getMessageType().equals(message.getMessageType().toString())) {
				return type;
			}
		}
		return CfLogType.CFSTDOUT;
	}

	public static final LogType CFSTDERROR = new LogType(MessageType.ERR.toString(), SWT.COLOR_RED);
	public static final LogType CFSTDOUT = new LogType(MessageType.OUT.toString(), SWT.COLOR_DARK_GREEN);
	public static final LogType[] CFLOG_TYPES = { CFSTDOUT, CFSTDERROR };
}
