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

import org.osgi.framework.ServiceReference;

/**
 * Simple interface passed around to decouple proxy creation (which is highly
 * contextual and configuration dependent) from the overall OSGi infrastructure
 * which is concerned with synchronization and events.
 * 
 * @author Costin Leau
 */
public interface ServiceProxyCreator {

	/**
	 * Create a service proxy for the given service reference. The proxy purpose
	 * is to transparently decouple the client from holding a strong reference
	 * to the service (which might go away) and provide various decorations.
	 * 
	 * 
	 * <p/> The method returns a container object with the proxy and a
	 * destruction callback for it (normally an invocation interceptor). The
	 * same functionality can be achieved by casting the proxy to Advised but
	 * for security reasons (users could disable the proxies themselves) the
	 * proxies are now created in an opaque manner.
	 * 
	 * @param reference service reference
	 * @return the proxy plus a destruction callback to it
	 */
	ProxyPlusCallback createServiceProxy(ServiceReference reference);
}
