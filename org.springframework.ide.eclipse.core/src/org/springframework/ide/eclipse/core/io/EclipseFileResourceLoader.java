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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Eclipse specific {@link ResourceLoader} implementation that creates
 * {@link EclipseClassPathResource}s or {@link EclipseResource} depending on
 * the given location String.
 * @author Christian Dupuis
 * @since 2.0.3
 * @see EclipsePathMatchingResourcePatternResolver
 */
class EclipseFileResourceLoader extends DefaultResourceLoader {

	private final IProject project;
	
	private final ClassLoader classLoader;
	
	/**
	 * Constructor taking a {@link IProject}.
	 */
	public EclipseFileResourceLoader(IProject project, ClassLoader classLoader) {
		super(classLoader);
		this.project = project;
		this.classLoader = classLoader;
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
		// Eclipse does not know anything about class files
		if (location.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
			return super.getResource(location);
		}
		else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new EclipseClassPathResource(location
					.substring(CLASSPATH_URL_PREFIX.length()), project);
		}
		else {
			return new EclipseResource('/' + project.getName() + location);
		}
	}

}
