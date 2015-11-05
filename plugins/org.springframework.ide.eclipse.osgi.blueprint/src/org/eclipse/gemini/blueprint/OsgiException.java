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

package org.eclipse.gemini.blueprint;

/**
 * Root of the Spring DM exception hierarchy.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiException extends RuntimeException {

	private static final long serialVersionUID = -2484573525557843394L;

	/**
	 * Default constructor using no specified cause or detailed message.
	 */
	public OsgiException() {
		super();
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param message detailed message
	 * @param cause exception cause
	 */
	public OsgiException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param message detailed message
	 */
	public OsgiException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param cause exception cause
	 */
	public OsgiException(Throwable cause) {
		super(cause);
	}

}
