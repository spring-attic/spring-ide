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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * ServiceReference based map. Offers a dynamic view of a service properties,
 * which can reflect updates done through the ServiceRegistration.
 * 
 * @see org.osgi.framework.ServiceRegistration
 * @see org.osgi.framework.ServiceReference
 * 
 * @author Costin Leau
 * 
 */
public class ServiceReferenceBasedMap extends AbstractMap {

	private ServiceReference reference;

	private static final String READ_ONLY_MSG = "this is a readonly map";


	private static class SimpleEntry implements Map.Entry {

		Object key;

		Object value;


		public SimpleEntry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			Object oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}

		public int hashCode() {
			return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
		}

		public String toString() {
			return key + "=" + value;
		}

		private boolean eq(Object o1, Object o2) {
			return (o1 == null ? o2 == null : o1.equals(o2));
		}
	}


	public ServiceReferenceBasedMap(ServiceReference ref) {
		Assert.notNull(ref);
		this.reference = ref;
	}

	public void clear() {
		throw new UnsupportedOperationException(READ_ONLY_MSG);
	}

	public boolean containsKey(Object key) {
		return (get(key) != null);
	}

	public boolean containsValue(Object value) {
		Assert.notNull(value);
		String[] keys = reference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++) {
			if (value.equals(reference.getProperty(keys[i])))
				return true;
		}
		return false;
	}

	public Set entrySet() {
		String[] keys = reference.getPropertyKeys();
		Set entrySet = new LinkedHashSet(keys.length);

		for (int i = 0; i < keys.length; i++) {
			entrySet.add(new SimpleEntry(keys[i], reference.getProperty(keys[i])));
		}
		return Collections.unmodifiableSet(entrySet);
	}

	public Object get(Object key) {
		if (key instanceof String)
			return reference.getProperty((String) key);
		else
			throw new IllegalArgumentException("only String keys are allowed");
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException(READ_ONLY_MSG);
	}

	public void putAll(Map t) {
		throw new UnsupportedOperationException(READ_ONLY_MSG);
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException(READ_ONLY_MSG);
	}
}
