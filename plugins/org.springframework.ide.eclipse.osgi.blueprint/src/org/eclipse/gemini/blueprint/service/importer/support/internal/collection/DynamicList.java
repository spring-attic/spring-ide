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
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Subclass offering a List extension for a DynamicCollection. This allows not
 * just forward, but also backwards iteration through the
 * <code>ListIterator</list>.
 * 
 * @author Costin Leau
 * 
 */
public class DynamicList<E> extends DynamicCollection<E> implements List<E>, RandomAccess {

	/**
	 * List iterator.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class DynamicListIterator extends DynamicIterator implements ListIterator<E> {

		/**
		 * Similar to {@link DynamicIterator#tailGhost} in functionality but
		 * representing the last seen object in the head of the collection.
		 */
		protected volatile E headGhost = null;

		// flag used for enforcing the iterator consistency:
		// null - do not enforce anything
		// true - should not throw exception
		// false - should throw exception
		/**
		 * Iterator variable - not thread-safe/synchronized since only one
		 * thread should use the iterator.
		 */
		protected Boolean hasPrevious = null;

		/**
		 * Boolean field used by the {@link #set(Object)} and {@link #remove()}
		 * operation. True indicates next() was called, and false previous().
		 */
		private boolean previousOperationCalled = true;


		private DynamicListIterator(int index) {
			super.cursor = index;
		}

		public void add(E o) {
			removalAllowed = false;
			synchronized (storage) {
				synchronized (lock) {
					DynamicList.this.add(cursor, o);
				}
			}
		}

		/**
		 * Updates the hasPrevious field.
		 * 
		 * Internal unprotected method to avoid nested synchronization blocks.
		 * To execute this code, one needs the storage, iteratorsLock and
		 * iterator lock.
		 * 
		 * @return
		 */
		private boolean unsafeHasPrevious() {
			hasPrevious = (cursor - 1 >= 0 ? Boolean.TRUE : Boolean.FALSE);
			return hasPrevious.booleanValue();
		}

		public boolean hasPrevious() {
			synchronized (lock) {
				headGhost = null;
				return unsafeHasPrevious();
			}
		}

		public int nextIndex() {
			synchronized (lock) {
				return cursor;
			}
		}

		public E next() {
			previousOperationCalled = true;
			return super.next();
		}

		public E previous() {
			try {
				removalAllowed = true;
				previousOperationCalled = false;
				// no enforcement
				if (hasPrevious == null) {
					synchronized (storage) {
						synchronized (lock) {
							if (unsafeHasPrevious())
								return storage.get(--cursor);
							else
								throw new NoSuchElementException();
						}
					}
				}
				// need to return an object no matter what
				else if (hasPrevious.booleanValue()) {
					synchronized (storage) {
						synchronized (lock) {
							// if there is an element available, return it
							if (unsafeHasPrevious()) {
								return storage.get(--cursor);
							}
							else {
								// otherwise return the last one seen
								return headGhost;
							}
						}
					}
				}
				// should throw exception no matter what
				else {
					throw new NoSuchElementException();
				}
			}
			finally {
				// no matter what, reset hasPrevious
				hasPrevious = null;
				// remove ghost object
				synchronized (lock) {
					headGhost = null;
				}
			}
		}

		public int previousIndex() {
			synchronized (lock) {
				return (cursor - 1);
			}
		}

		public void set(E o) {
			if (!removalAllowed)
				throw new IllegalStateException();
			synchronized (storage) {
				synchronized (lock) {
					int index = (previousOperationCalled ? cursor - 1 : cursor);
					if (index < 0) {
						index = 0;
					}
					else {
						int length = storage.size();
						if (index > length) {
							index = length;
						}
					}
					storage.set(index, o);
				}
			}
		}

		protected int removalIndex(int cursor) {
			int index = (previousOperationCalled ? cursor - 1 : cursor);
			if (index < 0) {
				index = 0;
			}
			else {
				int length;
				synchronized (storage) {
					length = storage.size();
				}
				if (index > length) {
					index = length;
				}
			}
			return index;
		}
	}


	public DynamicList() {
		super();
	}

	public DynamicList(Collection<? extends E> c) {
		super(c);
	}

	public DynamicList(int size) {
		super(size);
	}

	public void add(int index, E o) {
		super.add(index, o);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		synchronized (storage) {
			return storage.addAll(index, c);
		}
	}

	public E get(int index) {
		synchronized (storage) {
			return storage.get(index);
		}
	}

	public int indexOf(Object o) {
		synchronized (storage) {
			return storage.indexOf(o);
		}
	}

	public int lastIndexOf(Object o) {
		synchronized (storage) {
			return storage.lastIndexOf(o);
		}
	}

	public ListIterator<E> listIterator() {
		DynamicListIterator iter = new DynamicListIterator(0);

		synchronized (iterators) {
			iterators.put(iter, null);
		}

		return iter;
	}

	public ListIterator<E> listIterator(int index) {
		return new DynamicListIterator(index);
	}

	public E remove(int index) {
		return super.remove(index);
	}

	public E set(int index, E o) {
		synchronized (storage) {
			return storage.set(index, o);
		}
	}

	// TODO: test behavior to see if the returned list properly behaves under
	// dynamic circumstances
	public List<E> subList(int fromIndex, int toIndex) {
		synchronized (storage) {
			return storage.subList(fromIndex, toIndex);
		}
	}
}