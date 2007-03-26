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

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.aop.ui.Activator;

public class DebugTracing {

	/**
	 * General debug trace for the plug-in enabled through the master trace
	 * switch.
	 */
	public static boolean DEBUG = false;

	/**
	 * Progress information for the compiler
	 */
	public static boolean DEBUG_BUILDER_PROGRESS = true;

	/**
	 * More detailed trace for compiler task list messages
	 */
	public static boolean DEBUG_BUILDER_MESSAGES = true;

	/**
	 * More detailed trace for the project builder
	 */
	public static boolean DEBUG_BUILDER = true;

	/**
	 * More detailed trace for project classpaths
	 */
	public static boolean DEBUG_BUILDER_CLASSPATH = true;

	public static String startupInfo() {
		Bundle bundle = Activator.getDefault().getBundle();
		String version = (String) bundle.getHeaders().get(
				Constants.BUNDLE_VERSION);
		StringBuffer eventData = new StringBuffer();
		eventData.append("Spring IDE version ");
		eventData.append(version);
		return eventData.toString();
	}
}
