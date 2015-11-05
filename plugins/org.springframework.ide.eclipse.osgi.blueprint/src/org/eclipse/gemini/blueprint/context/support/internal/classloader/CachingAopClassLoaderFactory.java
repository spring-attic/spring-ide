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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.aop.framework.ProxyFactory;

/**
 * Default implementation for {@link InternalAopClassLoaderFactory}. Uses an internal {@link WeakHashMap} to cache aop
 * class loaders to prevent duplicated copies.
 * 
 * @author Costin Leau
 */
class CachingAopClassLoaderFactory implements InternalAopClassLoaderFactory {

	private static final String CGLIB_CLASS = "net.sf.cglib.proxy.Enhancer";
	/** CGLIB class (if it's present) */
	private final Class<?> cglibClass;

	/** class loader -> aop class loader cache */
	private final Map<ClassLoader, WeakReference<ChainedClassLoader>> cache =
			new WeakHashMap<ClassLoader, WeakReference<ChainedClassLoader>>();

	CachingAopClassLoaderFactory() {
		// load CGLIB through Spring-AOP
		ClassLoader springAopClassLoader = ProxyFactory.class.getClassLoader();
		Class<?> clazz = null;
		try {
			clazz = springAopClassLoader.loadClass(CGLIB_CLASS);
		} catch (ClassNotFoundException cnfe) {
			// assume cglib is not present
		}
		cglibClass = clazz;
	}

	public ChainedClassLoader createClassLoader(final ClassLoader classLoader) {
		// search key (should be fast as the default classloader (BundleDelegatingClassLoader) has identity
		// equality/hashcode)
		synchronized (cache) {
			ChainedClassLoader aopClassLoader = null;
			WeakReference<ChainedClassLoader> loaderReference = cache.get(classLoader);

			if (loaderReference != null) {
				aopClassLoader = loaderReference.get();
			}

			// no associated class loader found, create one and put it in the cache
			if (aopClassLoader == null) {
				if (System.getSecurityManager() != null) {
					aopClassLoader = AccessController.doPrivileged(new PrivilegedAction<ChainedClassLoader>() {
						public ChainedClassLoader run() {
							return doCreateClassLoader(classLoader);
						}
					});
				} else {
					aopClassLoader = doCreateClassLoader(classLoader);
				}
				// save the class loader as a weak reference (since it refers to the given class loader)
				cache.put(classLoader, new WeakReference<ChainedClassLoader>(aopClassLoader));
			}
			return aopClassLoader;
		}
	}

	private ChainedClassLoader doCreateClassLoader(ClassLoader classLoader) {
		// use the given class loader, spring-aop, cglib (if available) and then spring-dm core class loader (for its
		// infrastructure interfaces)
		if (cglibClass != null) {
			return new ChainedClassLoader(new ClassLoader[] { classLoader, ProxyFactory.class.getClassLoader(),
					cglibClass.getClassLoader(), CachingAopClassLoaderFactory.class.getClassLoader() });
		} else {
			return new ChainedClassLoader(new ClassLoader[] { classLoader, ProxyFactory.class.getClassLoader(),
					CachingAopClassLoaderFactory.class.getClassLoader() });
		}
	}
}