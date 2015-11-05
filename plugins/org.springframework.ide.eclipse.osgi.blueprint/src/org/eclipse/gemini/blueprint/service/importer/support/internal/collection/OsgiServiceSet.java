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

package org.eclipse.gemini.blueprint.service.importer.support.internal.collection;

import java.util.Set;

import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

/**
 * 
 * OSGi service dynamic collection - allows iterating while the underlying storage is being shrunk/expanded. This
 * collection is read-only - its content is being retrieved dynamically from the OSGi platform.
 * 
 * <p/> This collection and its iterators are thread-safe. That is, multiple threads can access the collection. However,
 * since the collection is read-only, it cannot be modified by the client.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceSet extends OsgiServiceCollection implements Set {

	public OsgiServiceSet(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator, boolean useServiceReferences) {
		super(filter, context, classLoader, proxyCreator, useServiceReferences);
	}

	protected DynamicCollection createInternalDynamicStorage() {
		return new DynamicSet();
	}
}
