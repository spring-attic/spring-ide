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
package org.springframework.ide.eclipse.boot.dash.util;

/**
 * Utility methods
 *
 * @author Kris De Volder
 */
public class Utils {

	/**
	 * Creates http URL string based on host, port and path
	 * @param host
	 * @param port
	 * @param path
	 * @return the resultant URL
	 */
	public static String createUrl(String host, int port, String path) {
		if (path==null) {
			path = "";
		}
		if (host!=null) {
			if (port>0) {
				if (!path.startsWith("/")) {
					path = "/" +path;
				}
				return "http://"+host+":"+port+path;
			}
		}
		return null;
	}

}
