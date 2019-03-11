/*******************************************************************************
 * Copyright (c) 2009, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Extension to {@link URLClassLoader} that filters resource loading attempts by
 * calling {@link ProjectClassLoaderCache#shouldFilter(String)} before delegating to the super
 * implementation.
 * 
 * @since 2.7.0
 */
public class FilteringURLClassLoader extends URLClassLoader {
	
	private static final Enumeration<URL> EMPTY_ENUMERATION = Collections.enumeration(new ArrayList<URL>());

	public FilteringURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	@Override
	public URL findResource(String resourceName) {
		if (ProjectClassLoaderCache.shouldFilter(resourceName)) return null;
		return super.findResource(resourceName);
	}
	
	@Override
	public Enumeration<URL> findResources(String resourceName) throws IOException {
		if (ProjectClassLoaderCache.shouldFilter(resourceName)) return EMPTY_ENUMERATION;
		return super.findResources(resourceName);
	}
	
	@Override
	public URL getResource(String name) {
		if (ProjectClassLoaderCache.shouldFilter(name)) return null;
		return super.getResource(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		if (ProjectClassLoaderCache.shouldFilter(name)) return null;
		return super.getResourceAsStream(name);
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (ProjectClassLoaderCache.shouldFilter(name)) return EMPTY_ENUMERATION;
		return super.getResources(name);
	}
}