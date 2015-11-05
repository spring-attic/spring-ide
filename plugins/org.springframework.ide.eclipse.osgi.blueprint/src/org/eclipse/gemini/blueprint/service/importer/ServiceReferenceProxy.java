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

import org.osgi.framework.ServiceReference;

/**
 * {@link ServiceReference} extension used by Spring-DM service importers.
 * 
 * <p/> The interface aim is to decouple clients from the dynamics of the
 * imported services. Without such a proxy, a target service change (when
 * dealing with dynamic proxies), would force the corresponding service
 * reference to changes as well causing returned references to become stale. To
 * avoid this situation, the importer returns a proxy which is updated
 * automatically so the client always calls the updated, correct service
 * reference.
 * 
 * <p/> In most cases, users should not be aware of this interface. However, for
 * cases where the service reference has to be reused for going service lookups
 * and the OSGi platform demands the raw service reference class, this interface
 * allows access to the target, unwrapped service reference instance (which does
 * does not support service tracking and might become stale as explained above).
 * 
 * @author Costin Leau
 * 
 */
public interface ServiceReferenceProxy extends ServiceReference {

	/**
	 * Returns the target, native service reference used, at the moment of the
	 * call, by the proxy.
	 * 
	 * @return target service reference
	 */
	ServiceReference getTargetServiceReference();
}
