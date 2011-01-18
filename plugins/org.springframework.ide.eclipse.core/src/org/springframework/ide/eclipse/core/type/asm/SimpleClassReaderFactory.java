/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.type.asm;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of the {@link ClassReaderFactory} interface, creating a
 * new ClassReader for every request.
 * @author Christian Dupuis
 * @author Juergen Hoeller
 * @since 2.0.2
 */
public class SimpleClassReaderFactory implements ClassReaderFactory {

	private final ResourceLoader resourceLoader;

	/**
	 * Create a new SimpleClassReaderFactory for the default class loader.
	 */
	public SimpleClassReaderFactory() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new SimpleClassReaderFactory for the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use (also determines
	 * the ClassLoader to use)
	 */
	public SimpleClassReaderFactory(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader
				: new DefaultResourceLoader());
	}

	/**
	 * Create a new SimpleClassReaderFactory for the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public SimpleClassReaderFactory(ClassLoader classLoader) {
		this.resourceLoader = (classLoader != null ? new DefaultResourceLoader(
				classLoader) : new DefaultResourceLoader());
	}

	public ClassReader getClassReader(String className) throws IOException {
		String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX
				+ ClassUtils.convertClassNameToResourcePath(className)
				+ ClassUtils.CLASS_FILE_SUFFIX;
		return getClassReader(this.resourceLoader.getResource(resourcePath));
	}

	public ClassReader getClassReader(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		try {
			return new ClassReader(is);
		}
		finally {
			is.close();
		}
	}

}
