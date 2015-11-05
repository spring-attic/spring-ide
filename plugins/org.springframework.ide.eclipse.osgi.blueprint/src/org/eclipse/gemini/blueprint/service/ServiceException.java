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

import org.eclipse.gemini.blueprint.OsgiException;

/**
 * OSGi service specific exception.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceException extends OsgiException {

	private static final long serialVersionUID = 8290043693193600721L;


	/**
	 * Constructs a new <code>ServiceException</code> instance.
	 * 
	 */
	public ServiceException() {
		super();
	}

	/**
	 * Constructs a new <code>ServiceException</code> instance.
	 * 
	 * @param message exception detailed message
	 * @param cause exception cause
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>ServiceException</code> instance.
	 * 
	 * @param message exception detailed message
	 */
	public ServiceException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>ServiceException</code> instance.
	 * 
	 * @param cause exception cause
	 */
	public ServiceException(Throwable cause) {
		super(cause);
	}
}
