/*
 * Copyright 2002-2004 the original author or authors.
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

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * ResourceLoader implementation that resolves paths as file system resources
 * rather than as class path resources (Spring's DefaultResourceLoader's
 * strategy).
 * @see org.springframework.core.io.DefaultResourceLoader
 */
public class FileResourceLoader implements ResourceLoader {

	/**
	 * Resolve resource paths as file system paths.
	 * @param path path to the resource
	 * @return Resource handle
	 * @see FileResource
	 */
	public Resource getResource(String location) {
		Assert.notNull(location, "location is required");
		return new FileResource(location);
	}
}
