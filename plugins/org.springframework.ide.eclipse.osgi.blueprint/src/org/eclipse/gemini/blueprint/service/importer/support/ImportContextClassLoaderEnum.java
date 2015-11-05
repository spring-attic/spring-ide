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

import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;

/**
 * Enum describing the possible thread context class loader (tccl) options for imported OSGi services. If managed, the
 * tccl will be set to the appropriate class loader, on each service call for the duration of the invocation.
 * 
 * Used by {@link OsgiServiceProxyFactoryBean} and {@link OsgiServiceCollectionProxyFactoryBean} for imported services
 * that depend on a certain tccl to be set.
 * 
 * @see ExportContextClassLoaderEnum
 * @author Costin Leau
 */
public enum ImportContextClassLoaderEnum {

	/** The tccl will not be managed */
	UNMANAGED,
	/** The tccl will be set to that of the service provider upon service invocation */
	SERVICE_PROVIDER,
	/** The tccl will be set to that of the client upon service invocation */
	CLIENT;
}
