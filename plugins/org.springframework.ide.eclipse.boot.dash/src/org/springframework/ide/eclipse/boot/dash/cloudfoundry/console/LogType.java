/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.console;

import org.eclipse.swt.SWT;

public class LogType {
	/*
	 * Local messages types
	 */
	public static final LogType LOCALSTDOUT = new LogType(SWT.COLOR_DARK_BLUE);
	public static final LogType LOCALSTDERROR = new LogType(SWT.COLOR_RED);

	private final String type;
	private final int displayColour;

	public LogType(String type, int displayColour) {
		this.displayColour = displayColour;
		this.type = type;
	}

	public LogType(int displayColour) {
		this(null, displayColour);
	}

	public String getMessageType() {
		return this.type;
	}

	public int getDisplayColour() {
		return this.displayColour;
	}

}
