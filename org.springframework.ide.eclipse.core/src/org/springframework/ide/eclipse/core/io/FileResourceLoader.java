/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import org.eclipse.core.runtime.Assert;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link ResourceLoader} implementation that resolves paths as Eclipse
 * {@link FileResource file system resources} rather than as class path resources
 * (Spring's {@link DefaultResourceLoader}'s strategy).
 * 
 * @author Torsten Juergeleit
 */
public class FileResourceLoader implements ResourceLoader {

	/**
	 * Resolve resource paths as Eclipse
	 * {@link FileResource file system resources}.
	 * 
	 * @param path  path to the resource
	 * @return Resource handle
	 */
	public Resource getResource(String location) {
		Assert.isNotNull(location, "location is required");
		return new FileResource(location);
	}

	/**
	 * Returns <code>null</code> because Spring IDE's plug-in classloader is
	 * not useable in Spring's context.
	 */
	public ClassLoader getClassLoader() {
		return null;
	}
}
