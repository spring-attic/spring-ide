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

package org.eclipse.gemini.blueprint.service.exporter.support;

/**
 * Enum describing the the possible thread context class loader (tccl) options for exporter OSGi services. If managed,
 * the tccl will be set to the appropriate class loader, on each service call for the duration of the invocation.
 * 
 * <p/> Used by {@link OsgiServiceFactoryBean} for exported services that depend on certain tccl to be set.
 * 
 * @see ImportContextClassLoaderEnum
 * @author Costin Leau
 */
public enum ExportContextClassLoaderEnum {

	/** The tccl will not be managed */
	UNMANAGED,
	/** The tccl will be set to that of the service provider upon service invocation */
	SERVICE_PROVIDER
}
