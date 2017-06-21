/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes;

public class DomainUtils {

	public static boolean isInDomain(String hostAndDomain, String domain) {
		if (!hostAndDomain.endsWith(domain)) {
			return false;
		}
		if (domain.length()==hostAndDomain.length()) {
			//The uri matches domain precisely
			return true;
		} else if (hostAndDomain.charAt(hostAndDomain.length()-domain.length()-1)=='.') {
			//The uri matches as ${host}.${domain}
			return true;
		}
		return false;
	}

	public static String splitHost(String domain, String hostAndDomain) {
		if (hostAndDomain.endsWith("."+domain)) {
			return hostAndDomain.substring(0, hostAndDomain.length()-domain.length()-1);
		}
		return null;
	}

}
