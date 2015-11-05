/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class used for creating 'degradable' loggers for critical parts of the applications. In the future, this
 * class might be used across the entire product.
 * 
 * @author Costin Leau
 * 
 */
class LogUtils {

	/**
	 * Set the TCCL of the bundle before creating the logger. This helps if commons-logging is used since it looks at
	 * the existing TCCL before associating a LogFactory with it and since the TCCL can be the
	 * BundleDelegatingClassLoader, loading a LogFactory using the BundleDelegatingClassLoader will result in an
	 * infinite cycle or chained failures that would be swallowed.
	 * 
	 * <p/> Create the logger using LogFactory but use a simple implementation if something goes wrong.
	 * 
	 * @param logName log name
	 * @return logger implementation
	 */
	public static Log createLogger(final Class<?> logName) {
		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged(new PrivilegedAction<Log>() {
				public Log run() {
					return doCreateLogger(logName);
				}
			});
		}
		return doCreateLogger(logName);
	}

	private static Log doCreateLogger(Class<?> logName) {
		Log logger;

		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		// push the logger class classloader (useful when dealing with commons-logging 1.0.x
		Thread.currentThread().setContextClassLoader(logName.getClassLoader());
		try {
			logger = LogFactory.getLog(logName);
		} catch (Throwable th) {
			logger = new SimpleLogger();
			logger
					.fatal(
							"logger infrastructure not properly set up. If commons-logging jar is used try switching to slf4j (see the FAQ for more info).",
							th);
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
		return logger;
	}
}