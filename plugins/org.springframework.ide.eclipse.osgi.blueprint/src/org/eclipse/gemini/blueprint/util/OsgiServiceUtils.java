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

import org.osgi.framework.ServiceRegistration;

/**
 * Utility class offering easy access to OSGi services.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public abstract class OsgiServiceUtils {

	/**
	 * Unregisters the given service registration from the given bundle. Returns
	 * true if the unregistration process succeeded, false otherwise.
	 * 
	 * @param registration service registration (can be null)
	 * @return true if the unregistration succeeded, false otherwise
	 */
	public static boolean unregisterService(ServiceRegistration registration) {
		try {
			if (registration != null) {
				registration.unregister();
				return true;
			}
		} catch (IllegalStateException alreadyUnregisteredException) {
            // do nothing
		}
		return false;
	}
}
