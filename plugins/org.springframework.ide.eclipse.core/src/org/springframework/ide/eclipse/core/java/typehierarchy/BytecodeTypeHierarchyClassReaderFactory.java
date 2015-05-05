/*******************************************************************************
 * Copyright (c) 2013, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.java.ProjectClassLoaderCache;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BytecodeTypeHierarchyClassReaderFactory implements TypeHierarchyClassReaderFactory {

	public TypeHierarchyClassReader createClassReader(IProject project) {
		List<URL> urls = ProjectClassLoaderCache.getClassPathUrls(project, null);
		ClasspathLookup classpathLookup = new ClasspathLookup(urls.toArray(new URL[0]));
		return new BytecodeTypeHierarchyClassReader(classpathLookup);
	}

}
