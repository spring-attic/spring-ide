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

import org.osgi.framework.Bundle;

/**
 * Simple factory for creating class loaders for bundles.
 * 
 * @author Costin Leau
 * 
 */
interface BundleClassLoaderFactory {

	/**
	 * Creates a class loader for the given bundle.
	 * 
	 * @param bundle OSGi bundle
	 * @return associated class loader
	 */
	ClassLoader createClassLoader(Bundle bundle);
}
