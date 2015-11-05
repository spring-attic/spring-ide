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

package org.eclipse.gemini.blueprint.service.importer;

import java.util.Map;

/**
 * Listener tracking binding and unbinding of OSGi services used as normal object references inside a Spring application
 * context. Implementations can throws exceptions if they need/have to but they are not be propagated to other listeners
 * nor do they stop the other listeners from being notified.
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiServiceLifecycleListener {

	/**
	 * Called when a service is being binded inside the proxy (be it single or multi value). The service properties are
	 * made available as a {@link Map} which can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service the OSGi service instance
	 * @param properties the service properties
	 * @throws Exception custom exception that is logged but not propagated to other listeners
	 */
	void bind(Object service, Map properties) throws Exception;

	/**
	 * Called when a service is being unbinded inside the proxy (be it single or multi value). The service properties
	 * are made available as a {@link Map} which can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service the OSGi service instance
	 * @param properties the service properties
	 * @throws Exception custom exception that is logged but not propagated to other listeners
	 */
	void unbind(Object service, Map properties) throws Exception;
}
