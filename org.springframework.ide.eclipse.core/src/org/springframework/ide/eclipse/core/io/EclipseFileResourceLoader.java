/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Christian Dupuis
 * @since 2.0.3
 */
class EclipseFileResourceLoader extends DefaultResourceLoader {

	private final ClassLoader classLoader;

	private final ResourcePatternResolver patternResolver;
	
	public EclipseFileResourceLoader(ResourcePatternResolver patternResolver, ClassLoader classLoader) {
		super(classLoader);
		this.patternResolver = patternResolver;
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public Resource getResource(String location) {
		return patternResolver.getResource(location);
	}

}
