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

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BytecodeTypeHierarchyClassReaderFactory implements TypeHierarchyClassReaderFactory {

	public TypeHierarchyClassReader createClassReader(IProject project) {
//		List<URL> urls = ProjectClassLoaderCache.getClassPathUrls(project, null);
//		ClasspathLookup lookup = new ClasspathLookupDirect(urls.toArray(new URL[0]));
		
		ClassLoader loader = JdtUtils.getClassLoader(project, null);
		ClasspathLookup lookup = new ClasspathLookupClassloader(loader);

		return new BytecodeTypeHierarchyClassReader(lookup);
	}

}
