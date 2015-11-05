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

package org.eclipse.gemini.blueprint.service.importer.support;

import org.osgi.framework.ServiceReference;

/**
 * The type of the service collection. The members can be either service instances (proxies to the actual service
 * objects) or the appropriate service references.
 * 
 * @author Costin Leau
 */
public enum MemberType {

	/**
	 * Indicates proxies for the imported services.
	 */
	SERVICE_OBJECT,

	/**
	 * Indicates {@link ServiceReference}s matching the target service type
	 */
	SERVICE_REFERENCE;
}
