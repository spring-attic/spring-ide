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

package org.eclipse.gemini.blueprint.service;

import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * Exception indicating that an OSGi service became unavailable/unregistered. Normally this exception is used to
 * indicate that no suitable replacement is found (in case service rebinding is actually performed).
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class ServiceUnavailableException extends ServiceException {

	private static final long serialVersionUID = -3479837278220329490L;

	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param filter service filter
	 */
	public ServiceUnavailableException(Filter filter) {
		super("service matching filter=[" + filter + "] unavailable");
	}

	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param filter service filter (passed as a string)
	 */
	public ServiceUnavailableException(String filter) {
		super("service matching filter=[" + filter + "] unavailable");
	}

	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param reference service reference
	 */
	public ServiceUnavailableException(ServiceReference reference) {
		super("service with id=["
				+ (reference == null ? "null" : "" + OsgiServiceReferenceUtils.getServiceId(reference))
				+ "] unavailable");
	}
}