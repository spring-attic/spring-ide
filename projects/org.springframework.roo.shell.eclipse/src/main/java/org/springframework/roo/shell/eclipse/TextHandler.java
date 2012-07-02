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
package org.springframework.roo.shell.eclipse;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.support.util.ReflectionUtils;

/**
 * {@link Handler} implementation that funnels all log events to an instance of {@link StyledText}.
 * @author Christian Dupuis
 */
public class TextHandler extends Handler {

	private static Map<Integer, Object> outputMapping = new ConcurrentHashMap<Integer, Object>();

	public TextHandler(Object appender, int idenity) {
		outputMapping.put(idenity, appender);
		setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return record.getMessage() + System.getProperty("line.separator");
			}
		});

	}

	@Override
	public void close() throws SecurityException {
	} 

	@Override
	public void flush() {
	}

	@Override
	public void publish(final LogRecord record) {
		try {
			ProcessManager processManager = ActiveProcessManager.getActiveProcessManager();
			if (processManager != null) {
				final Object appender = outputMapping.get(System.identityHashCode(processManager));
				if (appender != null) {
					Method method = ReflectionUtils.findMethod(appender.getClass(), "append", new Class[] {
							String.class, Integer.class });
					ReflectionUtils.invokeMethod(method, appender, new Object[] { getFormatter().format(record),
							new Integer(record.getLevel().intValue()) });
				}
			}

		}
		catch (Exception e) {
			reportError("Could not publish log message", e, Level.SEVERE.intValue());
		}
	}
}
