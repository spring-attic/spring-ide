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

package org.springframework.ide.eclipse.osgi.blueprint.internal.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.support.ManagedProperties;

/**
 * Extension that adds ordering to {@link ManagedProperties} class intended for
 * preserving declaration order.
 * 
 * @author Costin Leau
 */
public class OrderedManagedProperties extends ManagedProperties {

	private final Map<Object, Object> orderedStorage = new LinkedHashMap<Object, Object>();

	public void clear() {
		orderedStorage.clear();
	}

	public boolean containsKey(Object key) {
		return orderedStorage.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return orderedStorage.containsValue(value);
	}

	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return orderedStorage.entrySet();
	}

	public boolean equals(Object o) {
		return orderedStorage.equals(o);
	}

	public Object get(Object key) {
		return orderedStorage.get(key);
	}

	public int hashCode() {
		return orderedStorage.hashCode();
	}

	public boolean isEmpty() {
		return orderedStorage.isEmpty();
	}

	public Set<Object> keySet() {
		return orderedStorage.keySet();
	}

	public Object put(Object key, Object value) {
		return orderedStorage.put(key, value);
	}

	public void putAll(Map<? extends Object, ? extends Object> t) {
		orderedStorage.putAll(t);
	}

	public Object remove(Object key) {
		return orderedStorage.remove(key);
	}

	public int size() {
		return orderedStorage.size();
	}

	public Collection<Object> values() {
		return orderedStorage.values();
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String val = getProperty(key);
		return (val == null ? defaultValue : val);
	}

	@Override
	public String getProperty(String key) {
		Object val = orderedStorage.get(key);
		return (val instanceof String ? (String) val : null);
	}

	@Override
	public Enumeration<?> propertyNames() {
		return new ArrayEnumeration<String>(filter(orderedStorage.keySet(), String.class));
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		return orderedStorage.put(key, value);
	}

	@Override
	public synchronized boolean contains(Object value) {
		return orderedStorage.containsKey(value);
	}

	@Override
	public synchronized Enumeration<Object> elements() {
		return new ArrayEnumeration<Object>(filter(orderedStorage.values(), Object.class));
	}

	@Override
	public synchronized Enumeration<Object> keys() {
		return new ArrayEnumeration<Object>(filter(orderedStorage.keySet(), Object.class));
	}

	@Override
	public synchronized String toString() {
		return orderedStorage.toString();
	}

	public Object merge(Object parent) {
		if (!isMergeEnabled()) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Properties)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		Properties merged = new OrderedManagedProperties();
		merged.putAll((Properties) parent);
		merged.putAll(this);
		return merged;
	}

	private <T> T[] filter(Collection<?> collection, Class<T> type) {
		List<T> list = new ArrayList<T>();
		for (Object member : collection) {
			if (type.isInstance(member)) {
				list.add((T) member);
			}
		}
		return (T[]) list.toArray(new Object[list.size()]);
	}

	private static class ArrayEnumeration<E> implements Enumeration<E> {

		private final E[] array;
		private int counter = 0;

		ArrayEnumeration(E[] array) {
			this.array = array;
		}

		public boolean hasMoreElements() {
			return (counter < array.length);
		}

		public E nextElement() {
			if (hasMoreElements())
				return array[counter++];
			throw new NoSuchElementException();
		}
	}
}