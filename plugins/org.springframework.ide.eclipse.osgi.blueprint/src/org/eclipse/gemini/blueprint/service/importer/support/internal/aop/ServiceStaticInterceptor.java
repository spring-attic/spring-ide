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

package org.eclipse.gemini.blueprint.service.importer.support.internal.aop;

import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.ServiceProxyDestroyedException;
import org.eclipse.gemini.blueprint.service.importer.support.internal.exception.BlueprintExceptionFactory;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * Interceptor offering static behaviour around an OSGi service. If the OSGi becomes unavailable, no look up or retries
 * will be executed, the interceptor throwing an exception.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceStaticInterceptor extends ServiceInvoker {

	private static final int hashCode = ServiceStaticInterceptor.class.hashCode() * 13;
	private boolean destroyed = false;

	/** private lock */
	private final Object lock = new Object();
	private final ServiceReference reference;
	private final BundleContext bundleContext;
	/** standard exception flag */
	private boolean useBlueprintExceptions = false;
	private final Filter filter;
	private volatile Object target = null;

	public ServiceStaticInterceptor(BundleContext context, ServiceReference reference) {
		Assert.notNull(context);
		Assert.notNull(reference, "a not null service reference is required");
		this.bundleContext = context;
		this.reference = reference;
		this.filter = OsgiFilterUtils.createFilter(OsgiFilterUtils.getFilter(reference));
	}

	protected Object getTarget() {
		synchronized (lock) {
			if (destroyed)
				throw new ServiceProxyDestroyedException();
		}

		// check if the service is alive first
		if (reference.getBundle() != null) {
			// since requesting for a service requires additional work
			// from the OSGi platform
			if (target == null) {
				synchronized (lock) {
					if (target == null && !destroyed) {
						target = bundleContext.getService(reference);
					}
				}
			}
			return target;
		}
		// throw exception
		throw (useBlueprintExceptions ? BlueprintExceptionFactory.createServiceUnavailableException(filter)
				: new ServiceUnavailableException(filter));
	}

	public void setUseBlueprintExceptions(boolean useBlueprintExceptions) {
		this.useBlueprintExceptions = useBlueprintExceptions;
	}

	public ServiceReference getServiceReference() {
		return reference;
	}

	public void destroy() {
		synchronized (lock) {
			// set this flag first to make sure after destruction, the OSGi service is not used any more
			destroyed = true;
		}
		try {
			bundleContext.ungetService(reference);
		} catch (IllegalStateException ex) {
			// in case the context is not valid anymore
		}

		target = null;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceStaticInterceptor) {
			ServiceStaticInterceptor oth = (ServiceStaticInterceptor) other;
			return reference.equals(oth.reference) && bundleContext.equals(oth.bundleContext);
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}