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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;

/**
 * Around interceptor for OSGi service invokers. Uses method invocation to
 * execute the call.
 * 
 * <p/> A {@link TargetSource} can be used though it doesn't offer localized
 * exceptions (unless information is passed around). The biggest difference as
 * opposed to a target source is that mixins call do not require a service
 * behind.
 * 
 * However, in the future, this interceptor might be replaced with a
 * TargetSource.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ServiceInvoker implements MethodInterceptor, ServiceReferenceProvider, DisposableBean {

	protected transient final Log log = LogFactory.getLog(getClass());


	/**
	 * Actual invocation - the class is being executed on a different object
	 * then the one exposed in the invocation object.
	 * 
	 * @param service
	 * @param invocation
	 * @return
	 * @throws Throwable
	 */
	protected Object doInvoke(Object service, MethodInvocation invocation) throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(service, invocation.getMethod(), invocation.getArguments());
	}

	public final Object invoke(MethodInvocation invocation) throws Throwable {
		return doInvoke(getTarget(), invocation);
	}

	/**
	 * Determine the target object to execute the invocation upon.
	 * 
	 * @return
	 * @throws Throwable
	 */
	protected abstract Object getTarget();

	/**
	 * Convenience method exposing the target (OSGi service) reference so that
	 * subinterceptors can access it. By default, returns null.
	 * 
	 * @return
	 */
	public ServiceReference getServiceReference() {
		return null;
	}

	// override so no exception is thrown
	public abstract void destroy();
}