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

package org.eclipse.gemini.blueprint.service.importer.support.internal.util;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.ServiceReference;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class OsgiServiceBindingUtils {

	private static final Log log = LogFactory.getLog(OsgiServiceBindingUtils.class);

	public static void callListenersBind(Object serviceProxy, ServiceReference reference,
			OsgiServiceLifecycleListener[] listeners) {
		if (!ObjectUtils.isEmpty(listeners)) {
			boolean debug = log.isDebugEnabled();

			// get a Dictionary implementing a Map
			Dictionary properties = OsgiServiceReferenceUtils.getServicePropertiesSnapshot(reference);
			for (int i = 0; i < listeners.length; i++) {
				if (debug)
					log.debug("Calling bind on " + listeners[i] + " w/ reference " + reference);
				try {
					listeners[i].bind(serviceProxy, (Map) properties);
				} catch (Exception ex) {
					log.warn("Bind method on listener " + listeners[i] + " threw exception ", ex);
				}
				if (debug)
					log.debug("Called bind on " + listeners[i] + " w/ reference " + reference);
			}
		}
	}

	public static void callListenersUnbind(Object serviceProxy, ServiceReference reference,
			OsgiServiceLifecycleListener[] listeners) {
		if (!ObjectUtils.isEmpty(listeners)) {
			boolean debug = log.isDebugEnabled();
			// get a Dictionary implementing a Map
			Dictionary properties =
					(reference != null ? OsgiServiceReferenceUtils.getServicePropertiesSnapshot(reference) : null);
			for (int i = 0; i < listeners.length; i++) {
				if (debug)
					log.debug("Calling unbind on " + listeners[i] + " w/ reference " + reference);
				try {
					listeners[i].unbind(serviceProxy, (Map) properties);
				} catch (Exception ex) {
					log.warn("Unbind method on listener " + listeners[i] + " threw exception ", ex);
				}
				if (debug)
					log.debug("Called unbind on " + listeners[i] + " w/ reference " + reference);
			}
		}
	}
}