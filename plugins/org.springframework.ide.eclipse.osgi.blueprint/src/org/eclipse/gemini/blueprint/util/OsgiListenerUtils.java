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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Utility class for dealing with various OSGi listeners, mainly
 * {@link ServiceListener}s. This class contains common functionality such as
 * broadcasting events or safely registering an OSGi listener.
 * 
 * @author Costin Leau
 */
public abstract class OsgiListenerUtils {

	private static final Log log = LogFactory.getLog(OsgiListenerUtils.class);


	/**
	 * Adds a service listener to the given bundle context under the specified
	 * filter. This method will deliver <em>synthetic</em> events of type
	 * <code>REGISTERED</code> for <em>all</em> existing services (that
	 * match the given filter) as if the services were registered after the
	 * listener registration.
	 * 
	 * <p/> This might cause problems if a service is registered between the
	 * listener registration and the retrieval of existing services since the
	 * listener will receive two events for the same service. For most listeners
	 * implementations however, this should not be a problem
	 * 
	 * @param context bundle context to register the listener with
	 * @param listener service listener to be registered
	 * @param filter OSGi filter (given as a Filter) for registering the
	 * listener (can be <code>null</code>)
	 * @see #addServiceListener(BundleContext, ServiceListener, String)
	 */
	public static void addServiceListener(BundleContext context, ServiceListener listener, Filter filter) {
		String toStringFilter = (filter == null ? null : filter.toString());
		addServiceListener(context, listener, toStringFilter);
	}

	/**
	 * Adds a service listener to the given bundle context under the specified
	 * filter given as a String. The method will also retrieve <em>all</em>
	 * the services registered before the listener registration (that match the
	 * given filter) and will inform the listener through service events of type
	 * <code>REGISTERED</code>.
	 * 
	 * <p/> This might cause problems if a service is registered between the
	 * listener registration and the retrieval of existing services since the
	 * listener will receive two events for the same service. For most listeners
	 * implementations however, this should not be a problem
	 * 
	 * 
	 * @param context bundle context to register the listener with
	 * @param listener service listener to be registered
	 * @param filter OSGi filter (given as a String) for registering the
	 * listener (can be <code>null</code>)
	 * @see BundleContext#getServiceReference(String)
	 * @see BundleContext#getServiceReferences(String, String)
	 */
	public static void addServiceListener(BundleContext context, ServiceListener listener, String filter) {
		registerListener(context, listener, filter);

		// now get the already registered services and call the listener
		// (the listener should be able to handle duplicates)
		dispatchServiceRegistrationEvents(OsgiServiceReferenceUtils.getServiceReferences(context, filter), listener);
	}

	private static void registerListener(BundleContext context, ServiceListener listener, String filter) {
		Assert.notNull(context);
		Assert.notNull(listener);

		try {
			// add listener
			context.addServiceListener(listener, filter);
		}
		catch (InvalidSyntaxException isex) {
			throw (RuntimeException) new IllegalArgumentException("Invalid filter").initCause(isex);
		}
	}

	private static void dispatchServiceRegistrationEvents(ServiceReference[] alreadyRegistered, ServiceListener listener) {
		if (log.isTraceEnabled())
			log.trace("Calling listener for already registered services: "
					+ ObjectUtils.nullSafeToString(alreadyRegistered));

		if (alreadyRegistered != null) {
			for (int i = 0; i < alreadyRegistered.length; i++) {
				listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, alreadyRegistered[i]));
			}
		}
	}

	/**
	 * Adds a service listener to the given bundle context, under the specified
	 * filter. This method will deliver at most one <em>synthetic</em> event
	 * of type <code>REGISTERED</code> for the <em>best matching</em>
	 * existing service as if the services were registered after the listener
	 * registration.
	 * 
	 * 
	 * <p/> This might cause problems if a service is registered between the
	 * listener registration and the retrieval of existing services since the
	 * listener will receive two events for the same service. For most listeners
	 * implementations however, this should not be a problem
	 * 
	 * 
	 * @param context bundle context to register the listener with
	 * @param listener service listener to be registered
	 * @param filter OSGi filter (given as a Filter) for registering the
	 * listener (can be <code>null</code>)
	 * @see #addSingleServiceListener(BundleContext, ServiceListener, String)
	 */
	public static void addSingleServiceListener(BundleContext context, ServiceListener listener, Filter filter) {
		String toStringFilter = (filter == null ? null : filter.toString());
		addSingleServiceListener(context, listener, toStringFilter);
	}

	/**
	 * Adds a service listener to the given application context, under the
	 * specified filter given as a String. The method will also retrieve the
	 * <em>best matching</em> service registered before the listener
	 * registration and will inform the listener through a service event of type
	 * <code>REGISTERED</code>. This is the only difference from
	 * {@link #addServiceListener(BundleContext, ServiceListener, Filter)} which
	 * considers all services, not just the best match.
	 * 
	 * <p/> This might cause problems if a service is registered between the
	 * listener registration and the retrieval of existing services since the
	 * listener will receive two events for the same service. For most listeners
	 * implementations however, this should not be a problem
	 * 
	 * 
	 * @param context bundle context to register the listener with
	 * @param listener service listener to be registered
	 * @param filter OSGi filter (given as a String) for registering the
	 * listener (can be <code>null</code>)
	 * @see BundleContext#getServiceReference(String)
	 * @see BundleContext#getServiceReferences(String, String)
	 */
	public static void addSingleServiceListener(BundleContext context, ServiceListener listener, String filter) {
		registerListener(context, listener, filter);

		// now get the already registered services and call the listener
		// (the listener should be able to handle duplicates)
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(context, filter);
		ServiceReference[] refs = (ref == null ? null : new ServiceReference[] { ref });
		dispatchServiceRegistrationEvents(refs, listener);
	}

	/**
	 * Removes a service listener from the given bundle context. This method
	 * simply takes care of any exceptions that might be thrown (in case the
	 * context is invalid).
	 * 
	 * @param context bundle context to unregister the listener from
	 * @param listener service listener to unregister
	 * @return true if the listener unregistration has succeeded, false
	 * otherwise (for example if the bundle context is invalid)
	 */
	public static boolean removeServiceListener(BundleContext context, ServiceListener listener) {
		if (context == null || listener == null)
			return false;

		try {
			context.removeServiceListener(listener);
			return true;
		}
		catch (IllegalStateException e) {
			// Bundle context is no longer valid
		}

		return false;
	}

}