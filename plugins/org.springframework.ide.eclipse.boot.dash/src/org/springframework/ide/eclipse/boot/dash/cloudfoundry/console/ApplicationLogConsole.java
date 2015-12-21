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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

@SuppressWarnings("restriction")
public class ApplicationLogConsole extends MessageConsole implements ApplicationLogListener, IPropertyChangeListener {

	private Map<LogType, IOConsoleOutputStream> activeStreams = new HashMap<LogType, IOConsoleOutputStream>();

	private StreamingLogToken loggregatorToken;

	public ApplicationLogConsole(String name, String type) {
		super(name, type, BootDashActivator.getImageDescriptor("icons/cloud_obj.png"), true);
	}

	public synchronized void writeLoggregatorLogs(List<ApplicationLog> logs) {
		if (logs != null) {
			for (ApplicationLog log : logs) {
				writeLoggregatorLog(log);
			}
		}
	}

	public synchronized void setLoggregatorToken(StreamingLogToken loggregatorToken) {
		if (this.loggregatorToken != null) {
			this.loggregatorToken.cancel();
		}
		this.loggregatorToken = loggregatorToken;
	}

	public synchronized StreamingLogToken getLoggregatorToken() {
		return this.loggregatorToken;
	}

	public synchronized void writeLoggregatorLog(ApplicationLog log) {
		if (log == null) {
			return;
		}
		final LogType logType = LogType.getLoggregatorType(log.getMessageType());
		writeApplicationLog(log.getMessage(), logType);
	}

	/**
	 *
	 * @param message
	 * @param type
	 * @return true if successfully wrote to stream. False otherwise
	 */
	public synchronized boolean writeApplicationLog(String message, LogType type) {
		if (message != null) {
			IOConsoleOutputStream stream = getStream(type);

			try {
				if (stream != null && !stream.isClosed()) {
					message = format(message);
					stream.write(message);
					return true;
				}
			} catch (IOException e) {
				BootDashActivator.log(e);
			}
		}
		return false;
	}

	protected static String format(String message) {
		if (message.contains("\n") || message.contains("\r")) {
			return message;
		}
		return message + '\n';
	}

	public synchronized void close() {
		setLoggregatorToken(null);

		for (IOConsoleOutputStream outputStream : activeStreams.values()) {
			if (!outputStream.isClosed()) {
				try {
					outputStream.close();
				} catch (IOException e) {
					BootDashActivator.log(e);
				}
			}
		}
		activeStreams.clear();
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		manager.removeConsoles(new IConsole[] { this });
	}

	public synchronized void clearConnection() {
		setLoggregatorToken(null);
	}

	protected synchronized IOConsoleOutputStream getStream(final LogType logType) {

		IOConsoleOutputStream stream = activeStreams.get(logType);
		// If the console is no longer managed by the Eclipse console manager,
		// do NOT
		// write to the stream to avoid exceptions
		if (!isStillManaged() || (stream != null && stream.isClosed())) {
			return null;
		}
		if (stream == null) {
			stream = newOutputStream();

			final IOConsoleOutputStream toConfig = stream;

			// Setting colour must be done in UI thread
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					toConfig.setColor(Display.getDefault().getSystemColor(logType.getDisplayColour()));
				}
			});

			activeStreams.put(logType, stream);
		}
		return stream;
	}

	protected synchronized boolean isStillManaged() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

		IConsole[] activeConsoles = manager.getConsoles();
		if (activeConsoles != null) {
			for (IConsole console : activeConsoles) {
				if (console.getName().equals(this.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Cloud Foundry Loggregator handler API
	 *
	 */
	@Override
	public void onMessage(ApplicationLog log) {
		writeLoggregatorLog(log);
	}

	@Override
	public void onComplete() {
		// Leave open for tail
	}

	@Override
	public void onError(Throwable exception) {
		writeApplicationLog(exception.getMessage(), LogType.CFSTDERROR);
	}

	@Override
	protected void init() {
		super.init();
		JFaceResources.getFontRegistry().addListener(this);
	}

	@Override
	protected void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		super.dispose();
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getProperty();
		if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
		}
	}

}
