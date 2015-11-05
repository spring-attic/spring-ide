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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.support;

import java.util.Dictionary;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Simple wrapper that prevents a service registration from being unregistered.
 * 
 * @author Costin Leau
 */
public class ServiceRegistrationWrapper implements ServiceRegistration {

	private volatile ServiceRegistration delegate;

	public ServiceRegistrationWrapper(ServiceRegistration delegate) {
		this.delegate = delegate;
	}

	public ServiceReference getReference() {
		return delegate.getReference();
	}

	public void setProperties(Dictionary properties) {
		delegate.setProperties(properties);
	}

	public void swap(ServiceRegistration other) {
		this.delegate = other;
	}

	public void unregister() {
		throw new UnsupportedOperationException("Sevice unregistration is not allowed");
	}
}