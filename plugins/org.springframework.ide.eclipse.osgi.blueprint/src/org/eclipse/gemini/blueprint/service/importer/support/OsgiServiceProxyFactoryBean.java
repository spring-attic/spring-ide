/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ProxyPlusCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceInvoker;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProviderTCCLInterceptor;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterController;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterInternalActions;
import org.eclipse.gemini.blueprint.service.importer.support.internal.dependency.ImporterStateListener;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryTemplate;
import org.eclipse.gemini.blueprint.util.internal.ClassUtils;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.ObjectUtils;

/**
 * OSGi (single) service importer. This implementation creates a managed OSGi service proxy that handles the OSGi
 * service dynamics. The returned proxy will select only the best matching OSGi service for the configuration criteria.
 * If the select service goes away (at any point in time), the proxy will automatically search for a replacement without
 * the user intervention.
 * 
 * <p/> Note that the proxy instance remains the same and only the backing OSGi service changes. Due to the dynamic
 * nature of OSGi, the backing object can change during method invocations.
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * 
 */
public final class OsgiServiceProxyFactoryBean extends AbstractServiceImporterProxyFactoryBean implements
		ApplicationEventPublisherAware {

	/**
	 * Wrapper around internal commands.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class Executor implements ImporterInternalActions {

		public void addStateListener(ImporterStateListener stateListener) {
			stateListeners.add(stateListener);
		}

		public void removeStateListener(ImporterStateListener stateListener) {
			stateListeners.remove(stateListener);
		}

		public boolean isSatisfied() {
            return !mandatory || (proxy == null || proxy.getServiceReference().getBundle() != null);
		}
	}

	private static final Log log = LogFactory.getLog(OsgiServiceProxyFactoryBean.class);

	private long retryTimeout;
	private RetryTemplate retryTemplate;

	/** proxy cast to a specific interface to allow specific method calls */
	private ImportedOsgiServiceProxy proxy;

	/** proxy infrastructure hook exposed to allow clean up */
	private Runnable destructionCallback;
	private Runnable initializationCallback;

	/** application publisher */
	private ApplicationEventPublisher applicationEventPublisher;

	/** internal listeners */
	private final List<ImporterStateListener> stateListeners =
			Collections.synchronizedList(new ArrayList<ImporterStateListener>(4));

	private final ImporterInternalActions controller;
	/** convenience field * */
	private volatile boolean mandatory = true;

	private volatile boolean sticky = true;
	private final Object monitor = new Object();

	public OsgiServiceProxyFactoryBean() {
		controller = new ImporterController(new Executor());
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		mandatory = Availability.MANDATORY.equals(getAvailability());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a managed proxy to the best matching OSGi service.
	 */
	public Object getObject() {
		return super.getObject();
	}

	Object createProxy(boolean lazyProxy) {
		if (log.isDebugEnabled())
			log.debug("Creating a single service proxy ...");

		// first create the TCCL interceptor to register its listener with the
		// dynamic interceptor
		boolean serviceTccl = ImportContextClassLoaderEnum.SERVICE_PROVIDER.equals(getImportContextClassLoader());

		final ServiceProviderTCCLInterceptor tcclAdvice = (serviceTccl ? new ServiceProviderTCCLInterceptor() : null);
		final OsgiServiceLifecycleListener tcclListener =
				(serviceTccl ? tcclAdvice.new ServiceProviderTCCLListener() : null);

		Class<?> filterClass = ClassUtils.getParticularClass(getInterfaces());
		String filterClassName = (filterClass != null ? filterClass.getName() : null);
		final ServiceDynamicInterceptor lookupAdvice =
				new ServiceDynamicInterceptor(getBundleContext(), filterClassName, getUnifiedFilter(),
						getAopClassLoader());

		lookupAdvice.setMandatoryService(Availability.MANDATORY.equals(getAvailability()));
		lookupAdvice.setUseBlueprintExceptions(isUseBlueprintExceptions());
		lookupAdvice.setSticky(sticky);

		OsgiServiceLifecycleListener[] listeners = (serviceTccl ? ObjectUtils.addObjectToArray(getListeners(), tcclListener) : getListeners());

		lookupAdvice.setListeners(listeners);
		synchronized (monitor) {
			lookupAdvice.setRetryTimeout(retryTimeout);
			retryTemplate = lookupAdvice.getRetryTemplate();
		}
		lookupAdvice.setApplicationEventPublisher(applicationEventPublisher);

		// add the listeners as a list since it might be updated after the proxy
		// has been created
		lookupAdvice.setStateListeners(stateListeners);
		lookupAdvice.setServiceImporter(this);
		lookupAdvice.setServiceImporterName(getBeanName());

		// create a proxy creator using the existing context
		ServiceProxyCreator creator =
				new AbstractServiceProxyCreator(getInterfaces(), getAopClassLoader(), getBeanClassLoader(),
						getBundleContext(), getImportContextClassLoader()) {

					ServiceInvoker createDispatcherInterceptor(ServiceReference reference) {
						return lookupAdvice;
					}

					Advice createServiceProviderTCCLAdvice(ServiceReference reference) {
						return tcclAdvice;
					}
				};

		ProxyPlusCallback proxyPlusCallback = creator.createServiceProxy(lookupAdvice.getServiceReference());

		synchronized (monitor) {
			proxy = proxyPlusCallback.proxy;
			destructionCallback = new DisposableBeanRunnableAdapter(proxyPlusCallback.destructionCallback);
		}

		lookupAdvice.setProxy(proxy);
		// start the lookup only after the proxy has been assembled
		if (!lazyProxy) {
			lookupAdvice.afterPropertiesSet();
		} else {
			initializationCallback = new Runnable() {

				public void run() {
					lookupAdvice.afterPropertiesSet();
				}
			};
		}
		return proxy;
	}

	@Override
	Runnable getProxyInitializer() {
		return initializationCallback;
	}

	@Override
	Runnable getProxyDestructionCallback() {
		synchronized (monitor) {
			return destructionCallback;
		}
	}

	/**
	 * Add the given listener to the array but in the first position.
	 * 
	 * @param listeners
	 * @param listener
	 * @return
	 */
	private OsgiServiceLifecycleListener[] addListener(OsgiServiceLifecycleListener[] listeners,
			OsgiServiceLifecycleListener listener) {

		int size = (listeners == null ? 1 : listeners.length + 1);
		OsgiServiceLifecycleListener[] list = new OsgiServiceLifecycleListener[size];
		list[0] = listener;
		if (listeners != null)
			System.arraycopy(listeners, 0, list, 1, listeners.length);
		return list;
	}

	/**
	 * Sets how long (in milliseconds) should this importer wait between failed attempts at rebinding to a service that
	 * has been unregistered.
	 * 
	 * <p/> It is possible to change this value after initialization (while the proxy is in place). The new values will
	 * be used immediately by the proxy. Any in-flight waiting will be restarted using the new values. Note that if both
	 * values are the same, no restart will be applied.
	 * 
	 * @param timeoutInMillis Timeout to set, in milliseconds
	 */
	public void setTimeout(long timeoutInMillis) {
		RetryTemplate rt;

		synchronized (monitor) {
			this.retryTimeout = timeoutInMillis;
			rt = retryTemplate;
		}

		if (rt != null) {
			rt.reset(timeoutInMillis);
		}
	}

	/**
	 * Returns the timeout (in milliseconds) this importer waits while trying to find a backing service.
	 * 
	 * @return timeout in milliseconds
	 */
	public long getTimeout() {
		synchronized (monitor) {
			return retryTimeout;
		}
	}

	/**
	 * Sets the stickiness of this proxy. If 'true' (default), the proxy will rebind only if the backing service is no
	 * longer available. If 'false', the rebind will occur every time a 'better' candidate appears. A better service is
	 * defined by having either a higher ranking or the same ranking and a lower service id.
	 * 
	 * @param sticky sticky flag
	 */
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		synchronized (monitor) {
			this.applicationEventPublisher = applicationEventPublisher;
		}
	}
}