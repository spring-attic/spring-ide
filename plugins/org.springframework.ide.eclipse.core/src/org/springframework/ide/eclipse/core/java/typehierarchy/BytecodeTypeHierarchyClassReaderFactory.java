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
import java.util.List;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.java.ProjectClassLoaderCache;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BytecodeTypeHierarchyClassReaderFactory implements TypeHierarchyClassReaderFactory {

	public TypeHierarchyClassReader createClassReader(IProject project) {
		List<URL> urls = ProjectClassLoaderCache.getClassPathUrls(project, null);
		List<ClasspathElement> locations = new ArrayList<ClasspathElement>();
		
		for (URL url : urls) {
			if (url.toString().endsWith(".jar") || url.toString().endsWith(".zip")) {
				try {
					ZipFile zipFile = new ZipFile(new File(url.toURI()));
					locations.add(new ZipClasspathElement(zipFile));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					File file = new File(url.toURI());
					locations.add(new FileClasspathElement(file));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return new BytecodeTypeHierarchyClassReader((ClasspathElement[]) locations.toArray(new ClasspathElement[locations.size()]));
	}

}
