/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
