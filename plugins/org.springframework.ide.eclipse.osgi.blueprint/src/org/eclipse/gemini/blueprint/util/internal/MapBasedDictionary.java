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

package org.eclipse.gemini.blueprint.util.internal;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * Dictionary implementation backed by a map instance. While the JDK provides a
 * Dictionary implementation through Hashtable, the class itself is always
 * synchronized and does not maintain the internal order.
 * 
 * <p/>
 * This simple wrapper, accepts any type of Map as backing storage allowing more
 * options in choosing the appropriate implementation. By default, a
 * {@link java.util.LinkedHashMap} is used, if no Map is specified.
 * 
 * <p/>
 * This implementation will enforce the Dictionary behaviour over the map when
 * it comes to handling null values. As opposed to a Map, the Dictionary always
 * throws {@link NullPointerException} if a given argument is null.
 * 
 * @see java.util.Map
 * @see java.util.Dictionary
 * @author Costin Leau
 */
public class MapBasedDictionary<K, V> extends Dictionary<K, V> implements Map<K, V> {

	private Map<K, V> map;


	/**
	 * Enumeration wrapper around an Iterator.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private static class IteratorBasedEnumeration<E> implements Enumeration<E> {

		private Iterator<E> it;


		public IteratorBasedEnumeration(Iterator<E> it) {
			Assert.notNull(it);
			this.it = it;
		}

		public IteratorBasedEnumeration(Collection<E> col) {
			this(col.iterator());
		}

		public boolean hasMoreElements() {
			return it.hasNext();
		}

		public E nextElement() {
			return it.next();
		}
	}


	public MapBasedDictionary(Map<K, V> map) {
		this.map = (map == null ? new LinkedHashMap<K, V>() : map);
	}

	/**
	 * Default constructor.
	 * 
	 */
	public MapBasedDictionary() {
		this.map = new LinkedHashMap<K, V>();
	}

	public MapBasedDictionary(int initialCapacity) {
		this.map = new LinkedHashMap<K, V>(initialCapacity);
	}

	/**
	 * Constructor for dealing with existing Dictionary. Will copy the content
	 * into the inner Map.
	 * 
	 * @param dictionary
	 */
	public MapBasedDictionary(Dictionary<? extends K, ? extends V> dictionary) {
		this(new LinkedHashMap<K, V>(), dictionary);
	}

	public MapBasedDictionary(Map<K, V> map, Dictionary<? extends K, ? extends V> dictionary) {
		this(map);
		if (dictionary != null)
			putAll(dictionary);
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	public V get(Object key) {
		if (key == null)
			throw new NullPointerException();
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public V put(K key, V value) {
		if (key == null || value == null)
			throw new NullPointerException();

		return map.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		map.putAll(t);
	}

	public <T extends K> void putAll(Dictionary<T, ? extends V> dictionary) {
		if (dictionary != null)
			// copy the dictionary
			for (Enumeration<T> enm = dictionary.keys(); enm.hasMoreElements();) {
				T key = enm.nextElement();
				map.put(key, dictionary.get(key));
			}
	}

	public V remove(Object key) {
		if (key == null)
			throw new NullPointerException();

		return map.remove(key);
	}

	public int size() {
		return map.size();
	}

	public Collection<V> values() {
		return map.values();
	}

	public Enumeration<V> elements() {
		return new IteratorBasedEnumeration<V>(map.values());
	}

	public Enumeration<K> keys() {
		return new IteratorBasedEnumeration<K>(map.keySet());
	}

	public String toString() {
		return map.toString();
	}

	public boolean equals(Object obj) {
		// this should work nicely since the Dictionary implementations inside
		// the JDK are Maps also
		return map.equals(obj);
	}

	public int hashCode() {
		return map.hashCode();
	}
}