/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal logging - if a logger hasn't been set, dump to sdout
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class AopLog {

	public static final int DEFAULT = 0;

	public static final int BUILDER = 1;

	public static final int BUILDER_CLASSPATH = 2;

	public static final int BUILDER_PROGRESS = 3;

	public static final int BUILDER_MESSAGES = 4;

	private static IAopLogger logger;

	// support for logging the start and end of activies
	private static Map<String, Long> timers = new HashMap<String, Long>();

	public static void log(String msg) {
		log(DEFAULT, msg);
	}

	public static void log(int category, String msg) {
		if (logger != null) {
			logger.log(category, msg);
		}
		else {
			System.out.println(msg);
		}
	}

	public static void logStart(String event) {
		Long now = new Long(System.currentTimeMillis());
		timers.put(event, now);
	}

	public static void logEnd(int category, String event) {
		logEnd(category, event, null);
	}

	public static void logEnd(int category, String event, String optional_msg) {
		Long then = timers.get(event);
		if (then != null) {
			long now = System.currentTimeMillis();
			long elapsed = now - then.longValue();
			if ((optional_msg != null) && (optional_msg.length() > 0)) {
				log(
						category,
						"Timer event: " + elapsed + "ms: " + event + " (" + optional_msg + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else {
				log(category, "Timer event: " + elapsed + "ms: " + event); //$NON-NLS-1$ //$NON-NLS-2$
			}
			timers.remove(event);
		}
	}

	public static void setLogger(IAopLogger l) {
		logger = l;
	}
}
