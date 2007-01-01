/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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
