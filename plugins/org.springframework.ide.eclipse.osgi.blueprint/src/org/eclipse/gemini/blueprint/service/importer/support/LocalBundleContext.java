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

import org.osgi.framework.BundleContext;
import org.springframework.core.NamedInheritableThreadLocal;

/**
 * Class containing static methods used to obtain information about the current
 * OSGi service invocation.
 * 
 * <p>
 * The <code>getInvokerBundleContext()</code> method offers access to the
 * {@link BundleContext} of the entity accessing an OSGi service. The invoked
 * entity can thus discover information about the caller context.
 * 
 * <p>
 * The functionality in this class might be used by a target object that needed
 * access to resources on the invocation. However, this approach should not be
 * used when there is a reasonable alternative, as it makes application code
 * dependent on usage under AOP and the Spring Dynamic Modules and AOP framework
 * in particular.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public abstract class LocalBundleContext {

	/**
	 * ThreadLocal holder for the invoker context.
	 */
	private final static ThreadLocal<BundleContext> invokerBundleContext = new NamedInheritableThreadLocal<BundleContext>(
		"Current invoker bundle context");


	/**
	 * Try to get the current invoker bundle context. Note that this can be
	 * <code>null</code> if the caller is not a Spring-DM importer.
	 * 
	 * @return the invoker bundle context (can be null)
	 */
	public static BundleContext getInvokerBundleContext() {
		return invokerBundleContext.get();
	}

	/**
	 * Set the invoker bundle context. Note that callers should take care in
	 * cleaning up the thread-local when the invocation ends.
	 * 
	 * @param bundleContext invoker bundle context
	 * @return the old context in case there was one; maybe <code>null</code> is
	 *         none is set
	 */
	static BundleContext setInvokerBundleContext(BundleContext bundleContext) {
		BundleContext old = invokerBundleContext.get();
		invokerBundleContext.set(bundleContext);
		return old;
	}
}