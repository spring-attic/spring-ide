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

import org.eclipse.gemini.blueprint.service.ServiceException;

/**
 * Exception indicating the accessed OSGi service proxy has been destroyed.
 * 
 * Usually this exception is thrown when certain operations (normally those that
 * involve accessing the proxy target service ) are called on an OSGi service
 * proxy that has been destroyed.
 * 
 * @author Costin Leau
 */
public class ServiceProxyDestroyedException extends ServiceException {

	private static final long serialVersionUID = 1773620969162174421L;


	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 */
	public ServiceProxyDestroyedException() {
		super("service proxy has been destroyed");
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param message
	 * @param cause
	 */
	public ServiceProxyDestroyedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param message
	 */
	public ServiceProxyDestroyedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param cause
	 */
	public ServiceProxyDestroyedException(Throwable cause) {
		super(cause);
	}
}