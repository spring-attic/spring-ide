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

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

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
	 * @param serviceClass the required service class before resolving services
	 * from Service Registry
	 * @throws Exception any {@link Exception}
	 */
	public static void executeCallback(OsgiServiceCallback callback,
			BundleContext bundleContext, Class<?> serviceClass)
			throws Exception {
		executeCallback(callback, bundleContext, serviceClass, true);
	}

	/**
	 * Calls {@link OsgiServiceCallback#doWithService(Object)} for every
	 * {@link ServiceReference} that matches the given <code>serviceClass</code>
	 * within the <code>bundleContext</code>.
	 * @param callback the {@link OsgiServiceCallback} to execute
	 * @param bundleContext the {@link BundleContext}
	 * @param serviceClass the required service class
	 * @param activateExtender true if the OSGi extender should be activated
	 * before resolving services from Service Registry
	 * @throws Exception any {@link Exception}
	 * @since 2.0.2
	 */
	public static void executeCallback(OsgiServiceCallback callback,
			BundleContext bundleContext, Class<?> serviceClass,
			boolean activateExtender) throws Exception {

		// start the OSGi extender bundle
		if (activateExtender) {
			startBundle(getBundle(Activator.OSGI_EXTENDER_SYMBOLIC_NAME));
		}

		ServiceReference[] services = getAllServices(bundleContext,
				serviceClass, null);
		for (ServiceReference service : services) {
			Object obj = bundleContext.getService(service);
			try {
				callback.doWithService(obj);
			}
			finally {
				bundleContext.ungetService(service);
			}
		}
	}

	/**
	 * Returns all OGSi deployed services matching the given
	 * <code>serviceClass</code> from the <code>context</code>.
	 * @param bundleContext the {@link BundleContext}
	 * @param serviceClass the required service class
	 * @param filter filter expression to restrict the matching
	 * @return the located {@link ServiceReference}
	 * @throws IllegalArgumentException
	 * @since 2.0.2
	 */
	public static ServiceReference[] getAllServices(
			BundleContext bundleContext, Class<?> serviceClass, String filter)
			throws IllegalArgumentException {
		try {
			ServiceReference[] serviceReferences = bundleContext
					.getAllServiceReferences(serviceClass.getName(), filter);
			return serviceReferences;
		}
		catch (InvalidSyntaxException ex) {
			throw (IllegalArgumentException) new IllegalArgumentException(ex
					.getMessage()).initCause(ex);
		}
	}

	/**
	 * Returns an OSGi {@link Bundle} identified by the given
	 * <code>symbolicName</code>.
	 * @param symbolicName the symbolic name of the desired bundle
	 * @return the {@link Bundle}
	 * @since 2.0.2
	 */
	public static Bundle getBundle(String symbolicName) {
		return Platform.getBundle(symbolicName);
	}

	/**
	 * Starts the <code>bundle</code> if not already in state
	 * <code>ACTIVE</code>.
	 * @param bundle the {@link Bundle} to activate
	 * @since 2.0.2
	 */
	public static void startBundle(Bundle bundle) {
		if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
			try {
				bundle.start();
			}
			catch (BundleException e) {
			}
		}
	}
}
