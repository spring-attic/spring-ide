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
import java.util.Map;

import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.util.Assert;

/**
 * Decorator class for {@link ServiceReference} which add notification for {@link ServiceRegistration#unregister()}
 * method when dealing with listeners.
 * 
 * @author Costin Leau
 */
public class ServiceRegistrationDecorator implements ServiceRegistration {

	/** actual service registration */
	private final ServiceRegistration delegate;
	private volatile UnregistrationNotifier notifier;

	public ServiceRegistrationDecorator(ServiceRegistration registration) {
		Assert.notNull(registration);
		this.delegate = registration;
	}

	void setNotifier(UnregistrationNotifier notifier) {
		this.notifier = notifier;
	}

	public ServiceReference getReference() {
		return delegate.getReference();
	}

	public void setProperties(Dictionary properties) {
		delegate.setProperties(properties);
	}

	// call unregister on the actual service but inform also listeners
	public void unregister() {
		// if the delegate is unregistered then an exception will be thrown
		ServiceReference reference = delegate.getReference();

		Map properties =
				(reference != null ? (Map) OsgiServiceReferenceUtils.getServicePropertiesSnapshot(reference) : null);

		if (notifier != null) {
			notifier.unregister(properties);
		}
		delegate.unregister();
	}

	public String toString() {
		return "ServiceRegistrationWrapper for " + delegate.toString();
	}
}