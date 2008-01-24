/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import org.eclipse.core.resources.IProject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.Assert;

/**
 * Eclipse specific {@link ResourceLoader} implementation that creates
 * {@link EclipseClassPathResource}s or {@link EclipseResource} depending on
 * the given location String.
 * @author Christian Dupuis
 * @since 2.0.3
 * @see EclipsePathMatchingResourcePatternResolver
 */
class EclipseFileResourceLoader implements ResourceLoader {

	private final IProject project;
	
	private final ClassLoader classLoader;
	
	/**
	 * Constructor taking a {@link IProject}.
	 */
	public EclipseFileResourceLoader(IProject project) {
		this.project = project;
		this.classLoader = JdtUtils.getClassLoader(project);
	}
	
	/**
	 * Returns the {@link IProject}'s classloader constructed from the Eclipse 
	 * build path.
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");
		if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new EclipseClassPathResource(location
					.substring(CLASSPATH_URL_PREFIX.length()), project);
		}
		return new EclipseResource('/' + project.getName() + location);
	}

}
