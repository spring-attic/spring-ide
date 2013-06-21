/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.ProjectClassLoaderCache;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BytecodeTypeHierarchyClassReaderFactory implements TypeHierarchyClassReaderFactory {

	public TypeHierarchyClassReader createClassReader(IProject project) {
		List<URL> urls = ProjectClassLoaderCache.getClassPathUrls(project, null);
		List<ClasspathElement> locations = new ArrayList<ClasspathElement>();
		
		Set<URL> usedURLs = new HashSet<URL>();
		for (URL url : urls) {
			if (!usedURLs.contains(url)) {
				if (url.toString().endsWith(".jar") || url.toString().endsWith(".zip")) {
					try {
						ZipFile zipFile = new ZipFile(new File(url.toURI()));
						locations.add(new ZipClasspathElement(zipFile));
						usedURLs.add(url);
					} catch (Exception e) {
						SpringCore.log(e);
					}
				}
				else {
					try {
						File file = new File(url.toURI());
						locations.add(new FileClasspathElement(file));
						usedURLs.add(url);
					} catch (Exception e) {
						SpringCore.log(e);
					}
				}
			}
		}
		
		return new BytecodeTypeHierarchyClassReader((ClasspathElement[]) locations.toArray(new ClasspathElement[locations.size()]));
	}

}
