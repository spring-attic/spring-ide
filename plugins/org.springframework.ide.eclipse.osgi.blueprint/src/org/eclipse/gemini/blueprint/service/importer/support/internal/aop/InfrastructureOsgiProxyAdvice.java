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

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.core.InfrastructureProxy;
import org.springframework.util.Assert;

/**
 * Mixin implementation for {@link InfrastructureProxy} interface.
 * 
 * @author Costin Leau
 * 
 */
public class InfrastructureOsgiProxyAdvice extends DelegatingIntroductionInterceptor implements InfrastructureProxy {

	private static final long serialVersionUID = -496653472310304413L;

	private static final int hashCode = InfrastructureOsgiProxyAdvice.class.hashCode() * 13;

	private final transient ServiceInvoker invoker;


	public InfrastructureOsgiProxyAdvice(ServiceInvoker serviceInvoker) {
		Assert.notNull(serviceInvoker);
		this.invoker = serviceInvoker;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns the OSGi target service.
	 */
	public Object getWrappedObject() {
		return invoker.getTarget();
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof InfrastructureOsgiProxyAdvice) {
			InfrastructureOsgiProxyAdvice oth = (InfrastructureOsgiProxyAdvice) other;
			return (invoker.equals(oth.invoker));
		}
		else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
