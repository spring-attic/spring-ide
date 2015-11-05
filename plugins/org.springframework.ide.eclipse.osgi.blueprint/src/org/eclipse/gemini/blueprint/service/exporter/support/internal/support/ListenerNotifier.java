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

import java.util.Map;

import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;

/**
 * Basic class supporting the various types of listener invocations.
 * 
 * @author Costin Leau
 */
public class ListenerNotifier {

	private final OsgiServiceRegistrationListener[] listeners;

	public ListenerNotifier(OsgiServiceRegistrationListener[] listeners) {
		this.listeners = listeners;
	}

	public void callRegister(Object service, Map properties) {
		for (OsgiServiceRegistrationListener listener : listeners) {
			if (listener != null) {
				try {
					listener.registered(service, properties);
				} catch (Exception ex) {
					// no need to log exceptions, the wrapper already does this
				}
			}
		}
	}

	public void callUnregister(Object service, Map properties) {
		for (OsgiServiceRegistrationListener listener : listeners) {
			if (listener != null) {
				try {
					listener.unregistered(service, properties);
				} catch (Exception ex) {
					// no need to log exceptions, the wrapper already does this
				}
			}
		}
	}
}