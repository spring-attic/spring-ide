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
package org.springframework.ide.eclipse.core.type.asm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Caching implementation of the {@link ClassReaderFactory} interface, caching a
 * ClassReader per Spring Resource handle (i.e. per ".class" file).
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class CachingClassReaderFactory extends SimpleClassReaderFactory {

	private final Map<Resource, ClassReader> classReaderCache = 
		new HashMap<Resource, ClassReader>();

	/**
	 * Create a new CachingClassReaderFactory for the default class loader.
	 */
	public CachingClassReaderFactory() {
		super();
	}

	/**
	 * Create a new CachingClassReaderFactory for the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use (also determines
	 * the ClassLoader to use)
	 */
	public CachingClassReaderFactory(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	/**
	 * Create a new CachingClassReaderFactory for the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public CachingClassReaderFactory(ClassLoader classLoader) {
		super(classLoader);
	}

	public ClassReader getClassReader(Resource resource) throws IOException {
		synchronized (this.classReaderCache) {
			ClassReader classReader = this.classReaderCache.get(resource);
			if (classReader == null) {
				classReader = super.getClassReader(resource);
				this.classReaderCache.put(resource, classReader);
			}
			return classReader;
		}
	}

}
