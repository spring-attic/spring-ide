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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

/**
 * OSGi service dynamic collection - allows iterating while the underlying storage is being shrunk/expanded. This
 * collection is read-only - its content is being retrieved dynamically from the OSGi platform.
 * 
 * <p/> This collection and its iterators are thread-safe. That is, multiple threads can access the collection. However,
 * since the collection is read-only, it cannot be modified by the client.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceList extends OsgiServiceCollection implements List, RandomAccess {

	protected class OsgiServiceListIterator implements ListIterator {

		// dynamic iterator
		private final ListIterator iter;

		public OsgiServiceListIterator(int index) {
			iter = storage.listIterator(index);
		}

		public Object next() {
			return iter.next();
		}

		public Object previous() {
			return iter.previous();
		}

		//
		// index operations
		//
		public boolean hasNext() {
			return iter.hasNext();
		}

		public boolean hasPrevious() {
			return iter.hasPrevious();
		}

		public int nextIndex() {
			return iter.nextIndex();
		}

		public int previousIndex() {
			return iter.previousIndex();
		}

		//
		// read-only operations
		//
		public void add(Object o) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(Object o) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * cast the collection to a specialized collection
	 */
	protected List storage;

	public OsgiServiceList(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator, boolean useServiceReference) {
		super(filter, context, classLoader, proxyCreator, useServiceReference);
	}

	protected DynamicCollection createInternalDynamicStorage() {
		storage = new DynamicList();
		return (DynamicCollection) storage;
	}

	public Object get(int index) {
		return storage.get(index);
	}

	public int indexOf(Object o) {
		// FIXME: implement this
		throw new UnsupportedOperationException();
	}

	public int lastIndexOf(Object o) {
		// FIXME: implement this
		throw new UnsupportedOperationException();
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(final int index) {
		return new OsgiServiceListIterator(index);
	}

	public List subList(int fromIndex, int toIndex) {
		// FIXME: implement this
		// note: the trick here is to return a list which is backed up by this
		// one (i.e. read-only)
		throw new UnsupportedOperationException();
	}

	//
	// WRITE operations forbidden
	//
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}
}