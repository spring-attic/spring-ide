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

import org.springframework.util.Assert;

/**
 * A specilized subtype of DynamicList which impose an order between its
 * elements.
 * 
 * @author Costin Leau
 * 
 */
public class DynamicSortedList<E> extends DynamicList<E> {

	private final Comparator<? super E> comparator;


	public DynamicSortedList() {
		this((Comparator<? super E>) null);
	}

	public DynamicSortedList(Comparator<? super E> c) {
		super();
		this.comparator = c;

	}

	public DynamicSortedList(Collection<? extends E> c) {
		this.comparator = null;
		addAll(c);
	}

	public DynamicSortedList(int size) {
		super(size);
		this.comparator = null;
	}

	// this is very similar but not identical from DynamicSortedSet
	// the main difference is that duplicates are accepted
	@SuppressWarnings("unchecked")
	public boolean add(E o) {
		Assert.notNull(o);

		if (comparator == null && !(o instanceof Comparable))
			throw new ClassCastException("given object does not implement " + Comparable.class.getName()
					+ " and no Comparator is set on the collection");

		int index = 0;

		synchronized (storage) {
			index = Collections.binarySearch(storage, o, comparator);
			// duplicate found; it's okay since it's a list
			boolean duplicate = (index >= 0);

			// however, make sure we add the element at the end of the
			// duplicates
			if (duplicate) {
				boolean stillEqual = true;
				while (index + 1 < storage.size() && stillEqual) {

					stillEqual = false;
					E next = storage.get(index + 1);

					if ((comparator != null ? comparator.compare(o, next) == 0
							: ((Comparable<E>) o).compareTo(next) == 0)) {
						stillEqual = true;
						index++;
					}
				}
			}

			// translate index
			else
				index = -index - 1;

			if (duplicate)
				super.add(index + 1, o);
			else
				super.add(index, o);
		}
		return true;
	}

	//
	// DISABLED OPERATIONS
	// 

	public void add(int index, E o) {
		throw new UnsupportedOperationException("This is a sorted list; it is illegal to specify the element position");
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("This is a sorted list; it is illegal to specify the element position");
	}

	public E set(int index, E o) {
		throw new UnsupportedOperationException("This is a sorted list; it is illegal to specify the element position");
	}
}