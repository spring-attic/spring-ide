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
package org.springframework.ide.eclipse.beans.osgibridge;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * Provides template style working with OSGi services.
 * <p>
 * Handles querying, resolving and ungetting of the {@link ServiceReference}
 * instances.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiUtils {

	/**
	 * Callback interface that can be passed into
	 * {@link OsgiUtils#executeCallback()}.
	 * <p>
	 * The {@link #doWithService(Object)} method will be called for with every
	 * located matching service.
	 */
	public static interface OsgiServiceCallback {
		void doWithService(Object service) throws Exception;
	}

	/**
	 * Calls {@link OsgiServiceCallback#doWithService(Object)} for every
	 * {@link ServiceReference} that matches the given <code>serviceClass</code>
	 * within the <code>bundleContext</code>.
	 * @param callback the {@link OsgiServiceCallback} to execute
	 * @param bundleContext the {@link BundleContext}
	 * @param serviceClass the required service class
	 * @throws Exception any {@link Exception}
	 */
	public static void executeCallback(OsgiServiceCallback callback,
			BundleContext bundleContext, Class<?> serviceClass)
			throws Exception {
		ServiceReference[] services = OsgiServiceUtils.getServices(
				bundleContext, serviceClass, null);
		for (ServiceReference service : services) {
			Object obj = Activator.getDefault().getBundleContext().getService(
					service);
			try {
				callback.doWithService(obj);
			}
			finally {
				bundleContext.ungetService(service);
			}
		}
	}
}
