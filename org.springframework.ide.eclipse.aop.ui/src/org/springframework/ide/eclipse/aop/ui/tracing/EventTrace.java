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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventTrace {

	public static interface EventListener {
		public void aopEvent(String msg, int category, Date time);
	};

	private static List<EventListener> listeners = new ArrayList<EventListener>();

	public static void postEvent(String msg, int category) {
		Date time = new Date();
		if (!listeners.isEmpty()) {
			for (Object name : listeners) {
				((EventListener) name).aopEvent(msg, category, time);
			}
		}
	}

	public static void addListener(EventListener l) {
		listeners.add(l);
	}

	public static void removeListener(EventListener l) {
		listeners.remove(l);
	}
}
