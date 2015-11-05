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
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.springframework.util.Assert;

/**
 * Dynamic sorted set. The elements added at runtime, while preserve their
 * natural order which means
 * 
 * @author Costin Leau
 * 
 */
public class DynamicSortedSet<E> extends DynamicSet<E> implements SortedSet<E> {

	private final Comparator<? super E> comparator;


	public DynamicSortedSet() {
		this((Comparator<? super E>) null);
	}

	public DynamicSortedSet(Collection<? extends E> c) {
		comparator = null;
		addAll(c);
	}

	public DynamicSortedSet(int size) {
		super(size);
		comparator = null;
	}

	public DynamicSortedSet(SortedSet<E> ss) {
		this.comparator = ss.comparator();
		addAll(ss);
	}

	public DynamicSortedSet(Comparator<? super E> c) {
		this.comparator = c;

	}

	public Comparator<? super E> comparator() {
		return comparator;
	}

	public boolean add(E o) {
		Assert.notNull(o);

		if (comparator == null && !(o instanceof Comparable))
			throw new ClassCastException("given object does not implement " + Comparable.class.getName()
					+ " and no Comparator is set on the collection");

		int index = 0;

		synchronized (storage) {
			index = Collections.binarySearch(storage, o, comparator);
			// duplicate found; bail out
			if (index >= 0)
				return false;

			// translate index
			index = -index - 1;

			super.add(index, o);
		}

		return true;
	}

	public boolean remove(Object o) {
		Assert.notNull(o);
		return super.remove(o);
	}

	public E first() {
		synchronized (storage) {
			if (storage.isEmpty())
				throw new NoSuchElementException();
			else
				return storage.get(0);
		}
	}

	public SortedSet<E> headSet(Object toElement) {
		throw new UnsupportedOperationException();
	}

	public E last() {
		synchronized (storage) {
			if (storage.isEmpty())
				throw new NoSuchElementException();
			else
				return storage.get(storage.size() - 1);
		}
	}

	public SortedSet<E> subSet(Object fromElement, Object toElement) {
		throw new UnsupportedOperationException();
	}

	public SortedSet<E> tailSet(Object fromElement) {
		throw new UnsupportedOperationException();
	}
}