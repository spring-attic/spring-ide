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

package org.eclipse.gemini.blueprint.service.importer.support;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ImportedOsgiServiceProxyAdvice;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.InfrastructureOsgiProxyAdvice;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ProxyPlusCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceInvoker;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.eclipse.gemini.blueprint.service.util.internal.aop.ProxyUtils;
import org.eclipse.gemini.blueprint.service.util.internal.aop.ServiceTCCLInterceptor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * Internal (package visible) class used for handling common aspects in creating a proxy over OSGi services.
 * 
 * Notably, this class creates common aspects such as publishing the bundleContext on a thread-local or handling of
 * thread context classloader.
 * 
 * @author Costin Leau
 */
abstract class AbstractServiceProxyCreator implements ServiceProxyCreator {

	private static final Log log = LogFactory.getLog(AbstractServiceProxyCreator.class);

	/** shared immutable interceptor for client TCCL selection (default) */
	private final Advice clientTCCLAdvice;

	/** shared immutable interceptor for publishing the client bundle context */
	private final Advice invokerBundleContextAdvice;

	/** importing bundle/client classLoader */
	protected final ClassLoader classLoader;

	/** proxy classes (for static generation) */
	protected final Class<?>[] classes;

	/** client bundle context */
	protected final BundleContext bundleContext;

	private final ImportContextClassLoaderEnum iccl;

	AbstractServiceProxyCreator(Class<?>[] classes, ClassLoader aopClassLoader, ClassLoader bundleClassLoader,
			BundleContext bundleContext, ImportContextClassLoaderEnum iccl) {
		Assert.notNull(bundleContext);
		Assert.notNull(aopClassLoader);

		this.classes = classes;
		this.bundleContext = bundleContext;
		this.classLoader = aopClassLoader;
		this.iccl = iccl;

		clientTCCLAdvice =
				(ImportContextClassLoaderEnum.CLIENT.equals(iccl) ? new ServiceTCCLInterceptor(bundleClassLoader)
						: null);
		invokerBundleContextAdvice = new LocalBundleContextAdvice(bundleContext);
	}

	public ProxyPlusCallback createServiceProxy(ServiceReference reference) {
		List advices = new ArrayList(4);

		// 1. the ServiceReference-like mixin
		Advice mixin = new ImportedOsgiServiceProxyAdvice(reference);
		advices.add(mixin);

		// 2. publication of bundleContext (if there is any)
		// TODO: make this configurable (so it can be disabled)
		advices.add(invokerBundleContextAdvice);

		// 3. TCCL handling (if there is any)
		Advice tcclAdvice = determineTCCLAdvice(reference);

		if (tcclAdvice != null)
			advices.add(tcclAdvice);

		// 4. add the infrastructure proxy
		// but first create the dispatcher since we need
		ServiceInvoker dispatcherInterceptor = createDispatcherInterceptor(reference);
		Advice infrastructureMixin = new InfrastructureOsgiProxyAdvice(dispatcherInterceptor);

		advices.add(infrastructureMixin);
		advices.add(dispatcherInterceptor);

		return new ProxyPlusCallback(ProxyUtils.createProxy(getInterfaces(reference), null, classLoader, bundleContext,
				advices), dispatcherInterceptor);
	}

	private Advice determineTCCLAdvice(ServiceReference reference) {
		try {

			switch (iccl) {
			case CLIENT:
				return clientTCCLAdvice;
			case SERVICE_PROVIDER:
				return createServiceProviderTCCLAdvice(reference);
			case UNMANAGED:
				// do nothing
				return null;
			default:
				return null;
			}

		} finally {
			if (log.isTraceEnabled()) {
				log.trace(iccl + " TCCL used for invoking " + OsgiStringUtils.nullSafeToString(reference));
			}
		}
	}

	Class<?>[] getInterfaces(ServiceReference reference) {
		return classes;
	}

	/**
	 * Create service provider TCCL advice. Subclasses should extend this based on their configuration (i.e. is a static
	 * proxy or is it dynamic).
	 * 
	 * @param reference service reference
	 * @return AOP advice
	 */
	abstract Advice createServiceProviderTCCLAdvice(ServiceReference reference);

	/**
	 * Create a dispatcher interceptor that actually execute the call on the target service.
	 * 
	 * @param reference service reference
	 * @return AOP advice
	 */
	abstract ServiceInvoker createDispatcherInterceptor(ServiceReference reference);
}
