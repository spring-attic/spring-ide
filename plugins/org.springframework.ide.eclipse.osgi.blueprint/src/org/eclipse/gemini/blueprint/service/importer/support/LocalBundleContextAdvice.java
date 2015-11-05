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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Simple interceptor for temporarily pushing the invoker BundleContext to a
 * threadLocal.
 * 
 * This class has package visibility to be able to access the
 * {@link LocalBundleContextAdvice} setter method.
 * 
 * <strong>Note</strong>: This class is state-less so the same instance can be
 * used by several proxies at the same time.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
class LocalBundleContextAdvice implements MethodInterceptor {

	private static final int hashCode = LocalBundleContextAdvice.class.hashCode() * 13;

	private final BundleContext context;


	LocalBundleContextAdvice(Bundle bundle) {
		this(OsgiBundleUtils.getBundleContext(bundle));
	}

	LocalBundleContextAdvice(BundleContext bundle) {
		this.context = bundle;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		// save the old context
		BundleContext oldContext = LocalBundleContext.setInvokerBundleContext(context);

		try {
			return invocation.proceed();
		}
		finally {
			// restore old context
			LocalBundleContext.setInvokerBundleContext(oldContext);
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof LocalBundleContextAdvice) {
			LocalBundleContextAdvice oth = (LocalBundleContextAdvice) other;
			return context.equals(oth.context);
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
