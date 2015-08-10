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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.sql.Date;
import java.text.DateFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;

public class AppOperationLogger {

	private final BootDashModel model;

	private final String appName;

	public AppOperationLogger(BootDashModel model, String appName) {
		this.model = model;
		this.appName = appName;
	}

	public void logAndUpdateMonitor(String message, LogType type, IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.setTaskName(message);
		}
		try {
			if (model.getElementConsoleManager() != null) {
				Date date = new Date(System.currentTimeMillis());
				String dateVal = DateFormat.getDateTimeInstance().format(date);

				message = "[" + dateVal + " - Boot Dashboard] - " + message;
				model.getElementConsoleManager().writeToConsole(appName, message, LogType.LOCALSTDOUT);
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}
}
