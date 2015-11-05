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

package org.eclipse.gemini.blueprint.service.exporter;

import java.util.Map;

/**
 * Registration listener that needs notifications of registration and unregistration of OSGi services exported through
 * Spring OSGi. Implementations can throws exceptions if they need/have to but they are not be propagated to other
 * listeners nor do they stop the other listeners from being notified.
 * 
 * @author Costin Leau
 * @see org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean
 */
public interface OsgiServiceRegistrationListener {

	/**
	 * Called when the the service exported has been registered in the OSGi space. The service properties are made
	 * available as a {@link Map} which can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service object registered as an OSGi service
	 * @param serviceProperties OSGi service registration properties
	 * @throws Exception custom exception that is logged but not propagated to other listeners
	 */
	void registered(Object service, Map serviceProperties) throws Exception;

	/**
	 * Called when the OSGi service has been unregistered (removed from OSGi space). The service properties are made
	 * available as a {@link Map} which can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service object unregistered as a service from the OSGi space
	 * @param serviceProperties OSGi service registration properties
	 * @throws Exception custom exception that is logged but not propagated to other listeners
	 */
	void unregistered(Object service, Map serviceProperties) throws Exception;

}
