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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.DefaultOsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.ServiceProxyDestroyedException;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitTimedOutEvent;
import org.eclipse.gemini.blueprint.service.importer.support.internal.dependency.ImporterStateListener;
import org.eclipse.gemini.blueprint.service.importer.support.internal.exception.BlueprintExceptionFactory;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.DefaultRetryCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryTemplate;
import org.eclipse.gemini.blueprint.service.importer.support.internal.util.OsgiServiceBindingUtils;
import org.eclipse.gemini.blueprint.util.OsgiListenerUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Interceptor adding dynamic behaviour for unary service (..1 cardinality). It will look for a service using the given
 * filter, retrying if the service is down or unavailable. Will dynamically rebound a new service, if one is available
 * with a higher service ranking and the interceptor is non sticky.
 * 
 * <p/> In case no service is available, it will throw an exception.
 * 
 * <p/> <strong>Note</strong>: this is a stateful interceptor and should not be shared.
 * 
 * @author Costin Leau
 */
public class ServiceDynamicInterceptor extends ServiceInvoker implements InitializingBean,
		ApplicationEventPublisherAware {

	/**
	 * Override the default implementation to plug in event notification.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class EventSenderRetryTemplate extends RetryTemplate {

		public EventSenderRetryTemplate(long waitTime) {
			super(waitTime, lock);
		}

		public EventSenderRetryTemplate() {
			super(lock);
		}

		protected void callbackFailed(long stop) {
			publishEvent(new OsgiServiceDependencyWaitTimedOutEvent(eventSource, dependency, stop));
		}

		protected void callbackSucceeded(long stop) {
			publishEvent(new OsgiServiceDependencyWaitEndedEvent(eventSource, dependency, stop));
		}

		protected void onMissingTarget() {
			// send event
			publishEvent(new OsgiServiceDependencyWaitStartingEvent(eventSource, dependency, this.getWaitTime()));
		}
	}

	private class ServiceLookUpCallback extends DefaultRetryCallback<Object> {

		public Object doWithRetry() {
			// before checking for a service, check whether the proxy is still valid
			if (destroyed && !isDuringDestruction) {
				throw new ServiceProxyDestroyedException();
			}

			return (holder != null ? holder.getService() : null);
		}
	}

	private class ServiceReferenceLookUpCallback extends DefaultRetryCallback<ServiceReference> {

		public ServiceReference doWithRetry() {
			// before checking for a service, check whether the proxy is still valid
			if (destroyed && !isDuringDestruction) {
				throw new ServiceProxyDestroyedException();
			}

			return (holder != null ? holder.getReference() : null);
		}
	}

	/**
	 * Listener tracking the OSGi services which form the dynamic reference.
	 */
	// NOTE: while the listener here seems to share the same functionality as
	// the one in ServiceCollection in reality there are a big number of
	// differences in them - for example this one supports rebind
	// while the collection does not.
	//
	// the only common part is the TCCL handling before calling the listeners.
	private class Listener implements ServiceListener {

		public void serviceChanged(ServiceEvent event) {
			boolean hasSecurity = (System.getSecurityManager() != null);
			ClassLoader tccl = null;
			if (hasSecurity) {
				tccl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
					public ClassLoader run() {
						ClassLoader cl = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(classLoader);
						return cl;
					}
				});
			} else {
				tccl = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(classLoader);
			}
			try {
				ServiceReference ref = event.getServiceReference();

				boolean debug = log.isDebugEnabled();
				boolean publicDebug = PUBLIC_LOGGER.isDebugEnabled();

				switch (event.getType()) {

				case (ServiceEvent.REGISTERED):
					// same as ServiceEvent.REGISTERED
				case (ServiceEvent.MODIFIED): {
					// flag indicating if the service is bound or rebound
					boolean servicePresent = (holder != null);

					if (updateWrapperIfNecessary(ref)) {
						// inform listeners
						OsgiServiceBindingUtils.callListenersBind(proxy, ref, listeners);
						// we have a bind
						if (!servicePresent) {
							notifySatisfiedStateListeners();
						}
					}

					break;
				}
				case (ServiceEvent.UNREGISTERING): {

					boolean serviceRemoved = false;
					//
					// used if the service goes down and there is no replacement
					//
					// since the listeners will require a valid proxy, the invalidation has to happen *after* calling
					// the listeners
					//
					ReferenceHolder oldHolder = holder;

					// remove service
					if (holder != null && holder.equals(ref)) {
						serviceRemoved = true;
						holder = null;
					}

					ServiceReference newReference = null;

					boolean isDestroyed = destroyed;

					// discover a new reference only if we are still running
					if (!isDestroyed) {
						newReference =
								OsgiServiceReferenceUtils.getServiceReference(bundleContext, filterClassName,
										(filter == null ? null : filter.toString()));

						// we have a rebind (a new service was bound)
						// so another candidate has to be searched from the existing candidates
						// - as they are alive already, we have to send an event for them ourselves
						// MODIFIED will be used for clarity
						if (newReference != null) {
							// update the listeners (through a MODIFIED event
							serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED, newReference));
						}
					}

					// if no new reference was found and the service was indeed removed (it was bound to the
					// interceptor) then do an unbind
					if (newReference == null && serviceRemoved) {
						// reuse the old service until the listeners are notified
						holder = oldHolder;

						// inform listeners
						OsgiServiceBindingUtils.callListenersUnbind(proxy, ref, listeners);

						holder = null;

						if (debug || publicDebug) {
							String message = "Service reference [" + ref + "] was unregistered";
							if (serviceRemoved) {
								message += " and unbound from the service proxy";
							} else {
								message += " but did not affect the service proxy";
							}
							if (debug)
								log.debug(message);
							if (publicDebug)
								PUBLIC_LOGGER.debug(message);
						}

						// update internal state listeners (unsatisfied event)
						notifyUnsatisfiedStateListeners();
					}

					break;
				}
				default:
					throw new IllegalArgumentException("unsupported event type");
				}
			} catch (Throwable e) {
				// The framework will swallow these exceptions without logging,
				// so log them here
				log.fatal("Exception during service event handling", e);
			} finally {
				final ClassLoader finalTccl = tccl;
				if (hasSecurity) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						public Object run() {
							Thread.currentThread().setContextClassLoader(finalTccl);
							return null;
						}
					});
				}
				else {
					Thread.currentThread().setContextClassLoader(finalTccl);
				}

			}
		}

		private void notifySatisfiedStateListeners() {
			synchronized (stateListeners) {
				for (ImporterStateListener stateListener : stateListeners) {
					stateListener.importerSatisfied(eventSource, dependency);
				}
			}
		}

		private void notifyUnsatisfiedStateListeners() {
			synchronized (stateListeners) {
				for (ImporterStateListener stateListener : stateListeners) {
					stateListener.importerUnsatisfied(eventSource, dependency);
				}
			}
		}

		private boolean updateWrapperIfNecessary(ServiceReference ref) {
			boolean updated = false;
			try {
				if (holder == null || (!sticky && holder.isWorseThen(ref))) {
					updated = true;
					updateReferenceHolders(ref);
				}
				synchronized (lock) {
					lock.notifyAll();
				}
				return updated;
			} finally {
				boolean debug = log.isDebugEnabled();
				boolean publicDebug = PUBLIC_LOGGER.isDebugEnabled();

				if (debug || publicDebug) {
					String message = "Service reference [" + ref + "]";
					if (updated)
						message += " bound to proxy";
					else
						message += " not bound to proxy";
					if (debug)
						log.debug(message);
					if (publicDebug)
						PUBLIC_LOGGER.debug(message);
				}
			}
		}

		/**
		 * Update internal holders for the backing ServiceReference.
		 * 
		 * @param ref
		 */
		private void updateReferenceHolders(ServiceReference ref) {
			holder = new ReferenceHolder(ref, bundleContext);
			referenceDelegate.swapDelegates(ref);
		}
	}

	private static final int hashCode = ServiceDynamicInterceptor.class.hashCode() * 13;

	/** public logger */
	private static final Log PUBLIC_LOGGER =
			LogFactory.getLog("org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean");

	private final BundleContext bundleContext;

	private final String filterClassName;

	private final Filter filter;

	/** TCCL to set when calling listeners */
	private final ClassLoader classLoader;

	private final SwappingServiceReferenceProxy referenceDelegate;

	/** event listener */
	private final ServiceListener listener;

	/** mandatory flag */
	private boolean mandatoryService = true;

	/** flag indicating whether the destruction has started or not */
	private boolean isDuringDestruction = false;

	/** flag indicating whether the proxy is already destroyed or not */
	private volatile boolean destroyed = false;

	/** private lock */
	/**
	 * used for reading/setting property and sending notifications between the event listener and any threads waiting
	 * for an OSGi service to appear
	 */
	private final Object lock = new Object();

	/** service reference/service holder */
	private volatile ReferenceHolder holder;

	/** retry template */
	private final RetryTemplate retryTemplate = new EventSenderRetryTemplate();

	/** retry callback */
	private final RetryCallback<Object> retryCallback = new ServiceLookUpCallback();

	/** dependable service importer */
	private Object eventSource;

	/** event source (importer) name */
	private String sourceName;

	/** listener that need to be informed of bind/rebind/unbind */
	private OsgiServiceLifecycleListener[] listeners = new OsgiServiceLifecycleListener[0];

	/** reference to the created proxy passed to the listeners */
	private Object proxy;

	/** event publisher */
	private ApplicationEventPublisher applicationEventPublisher;

	/** dependency object */
	private OsgiServiceDependency dependency;

	/** internal state listeners */
	private List<ImporterStateListener> stateListeners = Collections.emptyList();
	/** standard exception flag */
	private boolean useBlueprintExceptions = false;

	private boolean sticky = false;

	public ServiceDynamicInterceptor(BundleContext context, String filterClassName, Filter filter,
			ClassLoader classLoader) {
		this.bundleContext = context;
		this.filterClassName = filterClassName;
		this.filter = filter;
		this.classLoader = classLoader;

		referenceDelegate = new SwappingServiceReferenceProxy();
		listener = new Listener();
	}

	public Object getTarget() {
		Object target = lookupService();

		// nothing found
		if (target == null) {
			throw (useBlueprintExceptions ? BlueprintExceptionFactory.createServiceUnavailableException(filter)
					: new ServiceUnavailableException(filter));
		}
		return target;
	}

	public ServiceReference getTargetReference() {
		ServiceReference reference = lookupServiceReference();

		// nothing found
		if (reference == null) {
			throw (useBlueprintExceptions ? BlueprintExceptionFactory.createServiceUnavailableException(filter)
					: new ServiceUnavailableException(filter));
		}
		return reference;
	}

	/**
	 * Looks the service by waiting the service to appear. Note this method should use the same lock as the listener
	 * handling the service reference.
	 */
	private Object lookupService() {
		synchronized (lock) {
			return retryTemplate.execute(retryCallback);
		}
	}

	/**
	 * Looks for the service reference to appear.
	 * 
	 * @return
	 */
	private ServiceReference lookupServiceReference() {
		synchronized (lock) {
			return retryTemplate.execute(new ServiceReferenceLookUpCallback());
		}
	}

	private void publishEvent(ApplicationEvent event) {
		if (applicationEventPublisher != null) {
			if (log.isTraceEnabled())
				log.trace("Publishing event through publisher " + applicationEventPublisher);
			try {
				applicationEventPublisher.publishEvent(event);
			} catch (IllegalStateException ise) {
				log.debug("Event " + event + " not published as the publisher is not initialized - "
						+ "usually this is caused by eager initialization of the importers by post processing", ise);
			}

		} else if (log.isTraceEnabled())
			log.trace("No application event publisher set; no events will be published");
	}

	public void afterPropertiesSet() {
		Assert.notNull(proxy);
		Assert.notNull(eventSource);

		boolean debug = log.isDebugEnabled();

		dependency = new DefaultOsgiServiceDependency(sourceName, filter, mandatoryService);

		if (debug)
			log.debug("Adding OSGi mandatoryListeners for services matching [" + filter + "]");
		OsgiListenerUtils.addSingleServiceListener(bundleContext, listener, filter);

		// inform listeners (in case no service is available)
		synchronized (lock) {
			if (referenceDelegate.getTargetServiceReference() == null) {
				OsgiServiceBindingUtils.callListenersUnbind(null, null, listeners);
			}
		}
	}

	public void destroy() {
		OsgiListenerUtils.removeServiceListener(bundleContext, listener);
		ServiceReference ref = null;
		synchronized (lock) {
			// set this flag first to make sure no rebind is done
			destroyed = true;
			isDuringDestruction = true;
			if (holder != null) {
				ref = holder.getReference();
				// send unregistration event to the listener
				listener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, ref));
			}
			/** destruction process has ended */
			isDuringDestruction = false;
			// notify also any proxies that still wait on the service
			lock.notifyAll();
		}

		// unget the service (help sorting out the bundles during shutdown)
		if (ref != null) {
			try {
				bundleContext.ungetService(ref);
			} catch (IllegalStateException ex) {
				// it's okay if the context is invalid
			}
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * This particular interceptor returns a delegated service reference so that callers can keep the reference even if
	 * the underlying target service reference changes in time.
	 */
	public ServiceReference getServiceReference() {
		return referenceDelegate;
	}

	public void setRetryTimeout(long timeout) {
		retryTemplate.reset(timeout);
	}

	public RetryTemplate getRetryTemplate() {
		return retryTemplate;
	}

	public OsgiServiceLifecycleListener[] getListeners() {
		return listeners;
	}

	public void setListeners(OsgiServiceLifecycleListener[] listeners) {
		this.listeners = listeners;
	}

	public void setServiceImporter(Object importer) {
		this.eventSource = importer;
	}

	public void setServiceImporterName(String name) {
		this.sourceName = name;
	}

	public void setMandatoryService(boolean mandatoryService) {
		this.mandatoryService = mandatoryService;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/** Internal state listeners */
	public void setStateListeners(List stateListeners) {
		this.stateListeners = stateListeners;
	}

	public void setUseBlueprintExceptions(boolean useBlueprintExceptions) {
		this.useBlueprintExceptions = useBlueprintExceptions;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceDynamicInterceptor) {
			ServiceDynamicInterceptor oth = (ServiceDynamicInterceptor) other;
			return (mandatoryService == oth.mandatoryService && ObjectUtils.nullSafeEquals(holder, oth.holder)
					&& ObjectUtils.nullSafeEquals(filter, oth.filter) && ObjectUtils.nullSafeEquals(retryTemplate,
					oth.retryTemplate));
		} else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}