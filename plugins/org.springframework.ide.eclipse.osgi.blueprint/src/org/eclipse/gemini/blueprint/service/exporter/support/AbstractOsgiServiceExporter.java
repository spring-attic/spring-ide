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

package org.eclipse.gemini.blueprint.service.exporter.support;

import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.support.ListenerNotifier;
import org.springframework.beans.factory.DisposableBean;

/**
 * Base exporter class providing common functionality for registering (also known as exporting) Spring beans as OSGi
 * services.
 * 
 * @author Costin Leau
 */
abstract class AbstractOsgiServiceExporter implements DisposableBean {

	/** listeners */
	private OsgiServiceRegistrationListener[] listeners = new OsgiServiceRegistrationListener[0];
	/** lazy callbacks */
	private boolean lazyListeners = false;
	private ListenerNotifier notifier;

	ListenerNotifier getNotifier() {
		return notifier;
	}

	/**
	 * Sets the listeners interested in registration and unregistration events.
	 * 
	 * @param listeners registration/unregistration listeners.
	 */
	public void setListeners(OsgiServiceRegistrationListener[] listeners) {
		if (listeners != null) {
			this.listeners = listeners;
			this.notifier = new ListenerNotifier(listeners);
		}
	}

	public void destroy() {
		unregisterService();
	}

	/**
	 * Registers/Exports the OSGi service.
	 */
	abstract void registerService();

	/**
	 * Unregisters/de-exports the OSGi service.
	 */
	abstract void unregisterService();

	/**
	 * Sets the laziness of the exporter listeners. Eager listeners (default) will cause the listeners to be called when
	 * the service is being exported. In contract, if true is passed, the listeners will be called not when the service
	 * is registered but after the first bundle actually requests it or another component requests the service
	 * registration. "Lazy listeners" are the equivalent of lazy activated service managers in Blueprint Service (OSGi
	 * 4.2).
	 * 
	 * @param lazyListeners false if the listeners should be called when the service is registered, true if the
	 * invocations should occur after the first service/factory bean request
	 */
	public void setLazyListeners(boolean lazyListeners) {
		this.lazyListeners = lazyListeners;
	}

	public boolean getLazyListeners() {
		return lazyListeners;
	}
}