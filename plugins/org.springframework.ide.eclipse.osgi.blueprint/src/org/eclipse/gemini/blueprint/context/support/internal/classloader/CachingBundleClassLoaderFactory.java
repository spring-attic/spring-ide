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

package org.eclipse.gemini.blueprint.context.support.internal.classloader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;

/**
 * Default implementation for {@link BundleClassLoaderFactory}.
 * 
 * @author Costin Leau
 */
class CachingBundleClassLoaderFactory implements BundleClassLoaderFactory {

	private static final String DELIMITER = "|";

	/** bundle -> map of class loaders (as a bundle can be refreshed) */
	private final Map<Bundle, Map<Object, WeakReference<ClassLoader>>> cache = new WeakHashMap<Bundle, Map<Object, WeakReference<ClassLoader>>>();


	public ClassLoader createClassLoader(Bundle bundle) {
		ClassLoader loader = null;
		// create a bundle identity object
		Object key = createKeyFor(bundle);

		Map<Object, WeakReference<ClassLoader>> loaders = null;
		// get associated class loaders (if any)
		synchronized (cache) {
			loaders = cache.get(bundle);
			if (loaders == null) {
				loaders = new HashMap<Object, WeakReference<ClassLoader>>(4);
				loader = createBundleClassLoader(bundle);
				loaders.put(key, new WeakReference<ClassLoader>(loader));
				return loader;
			}
		}
		// check the associated loaders
		synchronized (loaders) {
			WeakReference<ClassLoader> reference = loaders.get(key);
			if (reference != null)
				loader = (ClassLoader) reference.get();
			// loader not found (or already recycled)
			if (loader == null) {
				loader = createBundleClassLoader(bundle);
				loaders.put(key, new WeakReference<ClassLoader>(loader));
			}
			return loader;
		}
	}

	/**
	 * Creates a key for the given bundle. This is needed since the bundle can
	 * be updated or refreshed and thus can have different class loaders during
	 * its lifetime which are not reflected in its identity. Additionally, the
	 * given key will behave the same across the OSGi implementations and
	 * provide weak reference semantics.
	 * 
	 * @param bundle OSGi bundle
	 * @return key generated for the given bundle
	 */
	private Object createKeyFor(Bundle bundle) {
		StringBuilder buffer = new StringBuilder();
		// add the bundle id first
		buffer.append(bundle.getBundleId());
		// followed by its update time (in hex to reduce its length)
		buffer.append(DELIMITER);
		buffer.append(Long.toHexString(bundle.getLastModified()));
		// plus the bundle class name (just to be triple sure)
		buffer.append(DELIMITER);
		buffer.append(bundle.getClass().getName());
		return buffer.toString();
	}

	private ClassLoader createBundleClassLoader(Bundle bundle) {
		return BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);
	}
}