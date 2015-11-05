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

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author Costin Leau
 * 
 */
public class ProxyPlusCallback {

	/** proxy object */
	public final ImportedOsgiServiceProxy proxy;
	/** destruction callback */
	public final DisposableBean destructionCallback;


	/**
	 * Constructs a new <code>ProxyPlusCallback</code> instance.
	 * 
	 * @param proxy
	 * @param destructionCallback
	 */
	public ProxyPlusCallback(Object proxy, DisposableBean destructionCallback) {
		super();
		this.proxy = (ImportedOsgiServiceProxy) proxy;
		this.destructionCallback = destructionCallback;
	}
}
