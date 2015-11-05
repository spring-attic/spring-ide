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



/**
 * Internal contract for creating standard AOP class loaders. Implementations
 * can differ by using caching or returning new objects on each call.
 * 
 * <p/> Implementations <b>must</b> be thread-safe since the proxy generation
 * can occur at any time.
 * 
 * @author Costin Leau
 */
interface InternalAopClassLoaderFactory {

	/**
	 * Return the AOP class loader for the given bundle.
	 * 
	 * @param classLoader OSGi bundle
	 * @return AOP class loader for it
	 */
	ChainedClassLoader createClassLoader(ClassLoader classLoader);
}
