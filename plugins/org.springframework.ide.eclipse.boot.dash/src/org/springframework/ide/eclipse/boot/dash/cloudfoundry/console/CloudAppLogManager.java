/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.console;

import java.io.StringWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class CloudAppLogManager extends BootDashModelConsoleManager {

	static final String CONSOLE_TYPE = "org.springframework.ide.eclipse.boot.dash.cloudfoundry.console";
	static final String ATT_TARGET_PROPERTIES = "consoleTargetProperties";
	static final String ATT_APP_NAME = "consoleAppName";
	static final String APP_CONSOLE_ID = "consoleId";

	private IConsoleManager consoleManager;

	private final CloudFoundryRunTarget runTarget;

	public CloudAppLogManager(CloudFoundryRunTarget runTarget) {
		this.runTarget = runTarget;
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	}

	@Override
	protected void doWriteToConsole(BootDashElement element, String message, LogType type) throws Exception {
		doWriteToConsole(element.getName(), message, type);
	}

	@Override
	protected void doWriteToConsole(String appName, String message, LogType type) throws Exception {
		ApplicationLogConsole console = getExisitingConsole(runTarget.getTargetProperties(), appName);
		if (console != null) {
			console.writeApplicationLog(message, type);
			
			// To avoid console "jumping", only show console
			// if none are visible
			showIfNoOtherConsoles(console);
		}
	}

	/**
	 * Only shows the given console if no other console is visible. This avoid
	 * "jumping" between consoles when multiple consoles are streaming in
	 * parallel.
	 */
	protected void showIfNoOtherConsoles(IConsole console) {
		IConsole[] consoles = consoleManager.getConsoles();
		if (consoles == null || consoles.length == 0) {
			consoleManager.showConsoleView(console);
		}
	}

	public synchronized void resetConsole(String appName) {
		ApplicationLogConsole console = getExisitingConsole(runTarget.getTargetProperties(), appName);
		if (console != null) {
			console.clearConsole();
			console.setLogStreamingToken(null);
		}
	}

	/**
	 *
	 * @param targetProperties
	 * @param appName
	 * @return existing console, or null if it does not exist.
	 */
	protected synchronized ApplicationLogConsole getExisitingConsole(TargetProperties targetProperties,
			String appName) {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if (consoles != null) {
			for (IConsole console : consoles) {
				if (console instanceof ApplicationLogConsole) {
					String id = (String) ((MessageConsole) console).getAttribute(APP_CONSOLE_ID);
					String idToCheck = getConsoleId(targetProperties, appName);
					if (idToCheck.equals(id)) {
						ApplicationLogConsole appConsole = (ApplicationLogConsole) console;
						connectLoggregator(appConsole, appName);
						return appConsole;
					}
				}
			}
		}

		return null;
	}

	public synchronized void terminateConsole(String appName) throws Exception {
		ApplicationLogConsole console = getExisitingConsole(runTarget.getTargetProperties(), appName);
		if (console != null) {
			console.close();
			console.destroy();
		}
	}

	/**
	 *
	 * @param targetProperties
	 * @param appName
	 * @return non-null console for the given appname and target properties
	 * @throws Exception
	 *             if console was not created or found
	 */
	protected synchronized ApplicationLogConsole getApplicationConsole(TargetProperties targetProperties,
			String appName) throws Exception {
		if (appName == null) {
			throw ExceptionUtil.coreException("INTERNAL ERROR: No application name specified when writing to console.");
		}
		if (targetProperties == null) {
			throw ExceptionUtil.coreException("INTERNAL ERROR: No target properties specified for application : "
					+ appName + ". Unable to open console.");
		}
		ApplicationLogConsole appConsole = getExisitingConsole(targetProperties, appName);

		if (appConsole == null) {
			appConsole = new ApplicationLogConsole(getConsoleDisplayName(targetProperties, appName), CONSOLE_TYPE);
			appConsole.setAttribute(ATT_TARGET_PROPERTIES, targetProperties);
			appConsole.setAttribute(ATT_APP_NAME, appName);
			appConsole.setAttribute(APP_CONSOLE_ID, getConsoleId(targetProperties, appName));

			consoleManager.addConsoles(new IConsole[] { appConsole });

			connectLoggregator(appConsole, appName);
		}

		return appConsole;
	}

	protected void connectLoggregator(ApplicationLogConsole logConsole, String appName) {
		if (logConsole == null) {
			return;
		}
		if (logConsole.getLogStreamingToken() == null) {
			try {
				ClientRequests client = runTarget.getClient();
				logConsole.setLogStreamingToken(client.streamLogs(appName, logConsole));
			} catch (Exception e) {
				logConsole.writeApplicationLog("Failed to stream contents from Cloud Foundry due to: " + e.getMessage(),
						LogType.LOCALSTDERROR);
			}
		}
	}

	public static String getConsoleId(TargetProperties targetProperties, String appName) {
		return getConsoleDisplayName(targetProperties, appName) + '-' + targetProperties.getUrl();
	}

	public static String getConsoleDisplayName(TargetProperties targetProperties, String appName) {
		StringWriter writer = new StringWriter();
		writer.append(appName);

		String orgName = null;
		String spaceName = null;

		if (targetProperties instanceof CloudFoundryTargetProperties) {
			orgName = ((CloudFoundryTargetProperties) targetProperties).getOrganizationName();
			spaceName = ((CloudFoundryTargetProperties) targetProperties).getSpaceName();
		}
		if (orgName != null && spaceName != null) {
			writer.append(' ');
			writer.append('[');
			writer.append(orgName);
			writer.append(',');
			writer.append(' ');
			writer.append(spaceName);
			writer.append(']');
		}

		return writer.toString();
	}

	@Override
	public void showConsole(BootDashElement element) throws Exception {
		showConsole(element.getName());
	}

	@Override
	public void showConsole(String appName) throws Exception {
		ApplicationLogConsole console = getApplicationConsole(runTarget.getTargetProperties(), appName);
		consoleManager.showConsoleView(console);
	}

	@Override
	public void reconnect(BootDashElement element) throws Exception {
		String appName = element.getName();
		ApplicationLogConsole console = getApplicationConsole(runTarget.getTargetProperties(), appName);
		console.setLogStreamingToken(null);
		connectLoggregator(console, appName);
		consoleManager.showConsoleView(console);
	}

}
