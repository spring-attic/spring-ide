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
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

/**
 * TODO: push these functions into BootDashElements so CF implements differently.
 *
 * @author Kris De Volder
 */
public class BootDashElementUtil {

	public static String getUrl(BootDashElement el, RequestMapping rm) {
		String path = rm.getPath();
		return getUrl(el, path);
	}

	public static String getUrl(BootDashElement el, String path) {
		if (path==null) {
			path = "";
		}
		String host = el.getLiveHost();
		if (host!=null) {
			int port = el.getLivePort();
			if (port>0) {
				if (!path.startsWith("/")) {
					path = "/" +path;
				}
				return "http://"+host+":"+port+path;
			}
		}
		return null;
	}

	public static String getUrl(BootDashElement el) {
		if (el!=null) {
			return getUrl(el, el.getDefaultRequestMappingPath());
		}
		return null;
	}
}
