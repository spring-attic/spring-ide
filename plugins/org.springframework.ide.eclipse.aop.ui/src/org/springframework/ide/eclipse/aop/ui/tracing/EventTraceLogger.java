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
package org.springframework.ide.eclipse.aop.ui.tracing;

import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.logging.IAopLogger;

/**
 * This logger simply outputs to the event trace view
 * @author Christian Dupuis
 */
public class EventTraceLogger implements IAopLogger {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ajdt.internal.core.AJLogger#log(java.lang.String)
	 */
	public void log(String msg) {
		if (DebugTracing.DEBUG) {
			EventTrace.postEvent(msg, AopLog.DEFAULT);
		}
	}

	public void log(int category, String msg) {
		if (DebugTracing.DEBUG) {
			boolean doit = true;
			if (category == AopLog.BUILDER) {
				doit = DebugTracing.DEBUG_BUILDER;
			}
			else if (category == AopLog.BUILDER_CLASSPATH) {
				doit = DebugTracing.DEBUG_BUILDER_CLASSPATH;
			}
			else if (category == AopLog.BUILDER_PROGRESS) {
				doit = DebugTracing.DEBUG_BUILDER_PROGRESS;
			}
			else if (category == AopLog.BUILDER_MESSAGES) {
				doit = DebugTracing.DEBUG_BUILDER_MESSAGES;
			}
			if (doit) {
				EventTrace.postEvent(msg, category);
			}
		}
	}

}
