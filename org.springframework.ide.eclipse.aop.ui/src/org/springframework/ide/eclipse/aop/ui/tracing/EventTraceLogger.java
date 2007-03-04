/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.aop.ui.tracing;

import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.logging.IAopLogger;

/**
 * This logger simply outputs to the event trace view
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
