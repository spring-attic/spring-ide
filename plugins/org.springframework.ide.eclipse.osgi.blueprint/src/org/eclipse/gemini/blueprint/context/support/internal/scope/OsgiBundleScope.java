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

package org.eclipse.gemini.blueprint.context.support.internal.scope;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;

/**
 * OSGi bundle {@link org.springframework.beans.factory.config.Scope}
 * implementation.
 * 
 * Will allow per--calling-bundle object instance, thus this scope becomes
 * useful when enabled on localBeans exposed as OSGi services.
 * 
 * @author Costin Leau
 * 
 */

// This class relies heavily on the OSGi ServiceFactory (SF) behaviour.
// Since the OSGi platform automatically calls get/ungetService on a SF
// and caches the getService() object there is no need for caching inside the
// scope.
// This also means that the scope cannot interact with the cache and acts
// only as an object creator and nothing more in favor of the ServiceFactory.
// However, note that for the inner bundle, the scope has to mimic the OSGi
// behaviour.
// 
public class OsgiBundleScope implements Scope, DisposableBean {

	public static final String SCOPE_NAME = "bundle";

	private static final Log log = LogFactory.getLog(OsgiBundleScope.class);


	/**
	 * Decorating {@link org.osgi.framework.ServiceFactory} used for supporting
	 * 'bundle' scoped localBeans.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static class BundleScopeServiceFactory implements ServiceFactory {

		private ServiceFactory decoratedServiceFactory;

		/** destruction callbacks for bean instances */
		private final Map<Bundle, Runnable> callbacks = new ConcurrentHashMap<Bundle, Runnable>(4);


		public BundleScopeServiceFactory(ServiceFactory serviceFactory) {
			Assert.notNull(serviceFactory);
			this.decoratedServiceFactory = serviceFactory;
		}

		/**
		 * Called if a bundle requests a service for the first time (start the
		 * scope).
		 * 
		 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
		 *      org.osgi.framework.ServiceRegistration)
		 */
		public Object getService(Bundle bundle, ServiceRegistration registration) {
			try {
				// tell the scope, it's an outside bundle that does the call
				EXTERNAL_BUNDLE.set(Boolean.TRUE);

				// create the new object (call the container)
				Object obj = decoratedServiceFactory.getService(bundle, registration);

				// get callback (registered through the scope)
				Object passedObject = OsgiBundleScope.EXTERNAL_BUNDLE.get();

				// make sure it's not the marker object
				if (passedObject != null && passedObject instanceof Runnable) {
					Runnable callback = (Runnable) OsgiBundleScope.EXTERNAL_BUNDLE.get();
					if (callback != null)
						callbacks.put(bundle, callback);
				}
				return obj;
			}
			finally {
				// clean ThreadLocal
				OsgiBundleScope.EXTERNAL_BUNDLE.set(null);
			}
		}

		/**
		 * Called if a bundle releases the service (stop the scope).
		 * 
		 * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle,
		 *      org.osgi.framework.ServiceRegistration, java.lang.Object)
		 */
		public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
			try {
				// tell the scope, it's an outside bundle that does the call
				EXTERNAL_BUNDLE.set(Boolean.TRUE);
				// unget object first
				decoratedServiceFactory.ungetService(bundle, registration, service);

				// then apply the destruction callback (if any)
				Runnable callback = callbacks.remove(bundle);
				if (callback != null)
					callback.run();
			}
			finally {
				// clean ThreadLocal
				EXTERNAL_BUNDLE.set(null);
			}
		}
	}


	/**
	 * ThreadLocal used for passing objects around {@link OsgiBundleScope} and
	 * {@link BundleScopeServiceFactory} (there is only one scope instance but
	 * multiple BSSFs).
	 */
	public static final ThreadLocal<Object> EXTERNAL_BUNDLE = new NamedThreadLocal<Object>(
		"Current in-creation scoped bean");

	/**
	 * Map of localBeans imported by the current bundle from other bundles. This
	 * map is sychronized and is used by
	 * {@link org.eclipse.gemini.blueprint.context.support.internal.scope.OsgiBundleScope}
	 * .
	 */
	private final Map<String, Object> localBeans = new LinkedHashMap<String, Object>(4);

	/**
	 * Unsynchronized map of callbacks for the services used by the running
	 * bundle.
	 * 
	 * Uses the bean name as key and as value, a list of callbacks associated
	 * with the bean instances.
	 */
	private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<String, Runnable>(8);


	private boolean isExternalBundleCalling() {
		return (EXTERNAL_BUNDLE.get() != null);
	}

	public Object get(String name, ObjectFactory<?> objectFactory) {
		// outside bundle calling (no need to cache things)
		if (isExternalBundleCalling()) {
			Object bean = objectFactory.getObject();

			return bean;
		}
		// in-appCtx call
		else {
			// use local bean repository
			// cannot use a concurrent map since we want to postpone the call to
			// getObject
			synchronized (localBeans) {
				Object bean = localBeans.get(name);
				if (bean == null) {
					bean = objectFactory.getObject();
					localBeans.put(name, bean);
				}
				return bean;
			}
		}

	}

	public String getConversationId() {
		return null;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		// pass the destruction callback to the ServiceFactory
		if (isExternalBundleCalling())
			EXTERNAL_BUNDLE.set(callback);
		// otherwise destroy the bean from the local cache
		else {
			destructionCallbacks.put(name, callback);
		}
	}

	/*
	 * Unable to do this as we cannot invalidate the OSGi cache.
	 */
	public Object remove(String name) {
		throw new UnsupportedOperationException();
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	/*
	 * Clean up the scope (context refresh/close()).
	 */
	public void destroy() {
		boolean debug = log.isDebugEnabled();

		// handle only the local cache/localBeans
		// the ServiceFactory object will be destroyed upon service
		// unregistration
		for (Iterator<Map.Entry<String, Runnable>> iter = destructionCallbacks.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, Runnable> entry = iter.next();
			Runnable callback = (Runnable) entry.getValue();

			if (debug)
				log.debug("destroying local bundle scoped bean [" + entry.getKey() + "]");

			callback.run();
		}

		destructionCallbacks.clear();
		localBeans.clear();
	}
}