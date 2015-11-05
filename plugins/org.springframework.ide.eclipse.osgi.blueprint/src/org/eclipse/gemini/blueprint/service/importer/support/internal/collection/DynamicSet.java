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
import java.util.Iterator;
import java.util.Set;

/**
 * Wrapper extension to {@link DynamicCollection} which prevents duplicates.
 * 
 * @see DynamicCollection
 * @see Set
 * @author Costin Leau
 * 
 */
public class DynamicSet<E> extends DynamicCollection<E> implements Set<E> {

	public DynamicSet() {
		super();
	}

	public DynamicSet(Collection<? extends E> c) {
		super(c);
	}

	public DynamicSet(int size) {
		super(size);
	}

	public boolean add(E o) {
		synchronized (storage) {
			if (storage.contains(o))
				return false;
			storage.add(o);
		}
		return true;
	}

	public boolean addAll(Collection<? extends E> c) {
		if (c == null)
			throw new NullPointerException();
		boolean result = false;
		synchronized (storage) {
			for (Iterator<? extends E> iter = c.iterator(); iter.hasNext();) {
				result |= add(iter.next());
			}
		}

		return result;
	}
}