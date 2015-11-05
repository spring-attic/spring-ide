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

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.util.Assert;

/**
 * Mix-in implementation for ImportedOsgiServiceProxy.
 * 
 * @author Costin Leau
 * 
 */
public class ImportedOsgiServiceProxyAdvice extends DelegatingIntroductionInterceptor implements
		ImportedOsgiServiceProxy {

	private static final long serialVersionUID = 6455437774724678999L;

	private static final int hashCode = ImportedOsgiServiceProxyAdvice.class.hashCode() * 13;

	private final transient ServiceReferenceProxy reference;


	public ImportedOsgiServiceProxyAdvice(ServiceReference reference) {
		Assert.notNull(reference);
		this.reference = (reference instanceof ServiceReferenceProxy ? (ServiceReferenceProxy) reference
				: new StaticServiceReferenceProxy(reference));
	}

	public ServiceReferenceProxy getServiceReference() {
		return reference;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ImportedOsgiServiceProxyAdvice) {
			ImportedOsgiServiceProxyAdvice oth = (ImportedOsgiServiceProxyAdvice) other;
			return (reference.equals(oth.reference));
		}
		else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
