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

import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.gemini.blueprint.context.support.internal.classloader.ClassLoaderFactory;
import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.util.internal.PrivilegedUtils;
import org.osgi.framework.Bundle;
import org.springframework.util.ObjectUtils;

/**
 * Special Thread Context ClassLoading handling interceptor dealing with "service-provided" case, in which the backing
 * service reference can be updated which requires update of the classloader used as TCCL.
 * 
 * This interceptor requires registration of a dedicated {@link OsgiServiceLifecycleListener} which updates the
 * classloader used.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceProviderTCCLInterceptor implements MethodInterceptor {

	public class ServiceProviderTCCLListener implements OsgiServiceLifecycleListener {

		public void bind(Object service, Map properties) throws Exception {

			// check cast just to be sure (useful when doing testing for
			// example)
			if (service instanceof ImportedOsgiServiceProxy) {
				// get the service reference from object
				setServiceProvidedClassLoader(((ImportedOsgiServiceProxy) service).getServiceReference().getBundle());
			}
		}

		public void unbind(Object service, Map properties) throws Exception {
			// do nothing on unbind
		}
	}

	private static final int hashCode = ServiceProviderTCCLInterceptor.class.hashCode() * 13;

	/** internal lock used for synchronized access to the serviceBundle */
	private final Object lock = new Object();

	private Bundle serviceBundle;

	private ClassLoader serviceClassLoader;

	public Object invoke(MethodInvocation invocation) throws Throwable {

		if (System.getSecurityManager() != null) {
			return invokePrivileged(invocation);
		} else {
			return invokeUnprivileged(invocation);
		}
	}

	private Object invokePrivileged(final MethodInvocation invocation) throws Throwable {
		return PrivilegedUtils.executeWithCustomTCCL(getServiceProvidedClassLoader(),
				new PrivilegedUtils.UnprivilegedThrowableExecution() {

					public Object run() throws Throwable {
						return invocation.proceed();
					}
				});
	}

	private Object invokeUnprivileged(MethodInvocation invocation) throws Throwable {
		ClassLoader current = getServiceProvidedClassLoader();

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(current);
			return invocation.proceed();
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	private ClassLoader getServiceProvidedClassLoader() {
		synchronized (lock) {
			return serviceClassLoader;
		}
	}

	private void setServiceProvidedClassLoader(Bundle serviceBundle) {
		synchronized (lock) {
			this.serviceBundle = serviceBundle;
			if (serviceBundle != null) {
				serviceClassLoader = ClassLoaderFactory.getBundleClassLoaderFor(serviceBundle);
			} else
				serviceClassLoader = null;
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceProviderTCCLInterceptor) {
			ServiceProviderTCCLInterceptor oth = (ServiceProviderTCCLInterceptor) other;
			return (ObjectUtils.nullSafeEquals(serviceBundle, oth.serviceBundle));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}