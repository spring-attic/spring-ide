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

import java.util.Comparator;

import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

/**
 * Ordered list similar to a SortedSet with the difference, that it accepts duplicates.
 * 
 * @see Comparable
 * @see Comparator
 * @see java.util.SortedSet
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceSortedList extends OsgiServiceList {

	private final Comparator comparator;

	/**
	 * @param filter
	 * @param context
	 * @param classLoader
	 */
	public OsgiServiceSortedList(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator, boolean useServiceReference) {
		this(filter, context, classLoader, null, proxyCreator, useServiceReference);
	}

	public OsgiServiceSortedList(Filter filter, BundleContext context, ClassLoader classLoader, Comparator comparator,
			ServiceProxyCreator proxyCreator, boolean useServiceReference) {
		super(filter, context, classLoader, proxyCreator, useServiceReference);
		this.comparator = comparator;
	}

	protected DynamicCollection createInternalDynamicStorage() {
		storage = new DynamicSortedList(comparator);
		return (DynamicCollection) storage;
	}

	public Comparator comparator() {
		return comparator;
	}
}
