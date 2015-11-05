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

import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;
import org.springframework.util.Assert;

/**
 * Simple factory for generating Bundle/AOP-suitable class loaders used
 * internally by Spring-DM for generating proxies. The factory acts as a generic
 * facade for framework components hiding the implementation details (or the
 * changes in strategy).
 * 
 * <p/> Internally the factory will try to use a cache to avoid creating
 * unneeded class loader (even if lightweight) to avoid polluting the JDK/CGLIB
 * class loader maps.
 * 
 * @author Costin Leau
 */
public abstract class ClassLoaderFactory {

	/** plug-able, private, class loader factory */
	private static InternalAopClassLoaderFactory aopClassLoaderFactory = new CachingAopClassLoaderFactory();
	/** plug-able, private, bundle loader factory */
	private static BundleClassLoaderFactory bundleClassLoaderFactory = new CachingBundleClassLoaderFactory();


	/**
	 * Returns the standard, extended AOP class loader based on the given class
	 * loader.
	 * 
	 * @param classLoader base class loader
	 * @return AOP class loader created using the given argument
	 */
	public static ChainedClassLoader getAopClassLoaderFor(ClassLoader classLoader) {
		Assert.notNull(classLoader);
		return aopClassLoaderFactory.createClassLoader(classLoader);
	}

	/**
	 * Returns the wrapped class loader for the given bundle.
	 * 
	 * <p/> This method is similar to {@link #getAopClassLoaderFor(ClassLoader)}
	 * but considers the {@link BundleDelegatingClassLoader} associated with a
	 * bundle. Namely, the implementation will check if there is a wrapping
	 * class loader associated with the given bundle, creating one if none if
	 * found.
	 * 
	 * <p/> Useful when creating importers/exporters programmatically.
	 * 
	 * @param bundle OSGi bundle
	 * @return associated wrapping class loader
	 */
	public static ClassLoader getBundleClassLoaderFor(Bundle bundle) {
		Assert.notNull(bundle);
		return bundleClassLoaderFactory.createClassLoader(bundle);
	}
}
