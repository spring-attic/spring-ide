/*******************************************************************************
 * Copyright (c) 2012, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class CachingJdtMetadataReaderFactory implements MetadataReaderFactory {
	
	private final JdtMetadataReaderFactory factory;
	private final Map<String, MetadataReader> cache = new HashMap<String, MetadataReader>();
	
	public CachingJdtMetadataReaderFactory(IJavaProject project, ClassLoader classloader) {
		this.factory = new JdtMetadataReaderFactory(project, classloader);
	}

	public MetadataReader getMetadataReader(String className) throws IOException {
		synchronized(cache) {
			if (!cache.containsKey(className)) {
				cache.put(className, factory.getMetadataReader(className));
			}
		}
		
		return cache.get(className);
	}

	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		throw new JdtMetadataReaderException("'getMetadataReader' is not supported");
	}

}
