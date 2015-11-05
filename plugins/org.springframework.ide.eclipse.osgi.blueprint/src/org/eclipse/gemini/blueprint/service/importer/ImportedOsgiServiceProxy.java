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


/**
 * Infrastructure interface available on Spring-DM managed OSGi services. Gives
 * read-only access to the proxy backing object service reference.
 * 
 * @see ServiceReferenceProxy
 * @author Costin Leau
 * 
 */
public interface ImportedOsgiServiceProxy {

	/**
	 * Provides access to the service reference used for accessing the backing
	 * object. The returned object is a proxy over the native ServiceReference
	 * obtained from the OSGi platform, so that proper service tracking is
	 * obtained. This means that if the imported service change, the updates are
	 * reflected to the returned service reference automatically.
	 * 
	 * @return backing object service reference
	 */
	ServiceReferenceProxy getServiceReference();
}
