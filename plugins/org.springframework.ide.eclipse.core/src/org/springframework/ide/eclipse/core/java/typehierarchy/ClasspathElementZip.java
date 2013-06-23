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

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class ClasspathElementZip implements ClasspathElement {
	
	private ZipFile zipFile;
	private Set<String> knownPackageNames;

	public ClasspathElementZip(ZipFile zipFile) {
		this.zipFile = zipFile;
	}

	public InputStream getStream(String fullyQualifiedClassFileName, String packageName, String classFileName) throws Exception {
		if (!isPackage(packageName)) return null;
		
		ZipEntry entry = zipFile.getEntry(fullyQualifiedClassFileName);
		if (entry != null) {
			return zipFile.getInputStream(entry);
		}
		return null;
	}

	private boolean isPackage(String qualifiedPackageName) {
		if (this.knownPackageNames != null)
			return this.knownPackageNames.contains(qualifiedPackageName);

		try {
			this.knownPackageNames = findPackageSet(this.zipFile);
		} catch(Exception e) {
			this.knownPackageNames = new HashSet<String>();
		}
		return this.knownPackageNames.contains(qualifiedPackageName);
	}

	private Set<String> findPackageSet(ZipFile zipFile) {
		Set<String> packageSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		packageSet.add(""); //$NON-NLS-1$
		nextEntry : for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = ((ZipEntry) e.nextElement()).getName();

			// add the package name & all of its parent packages
			int last = fileName.lastIndexOf('/');
			while (last > 0) {
				// extract the package name
				String packageName = fileName.substring(0, last);
				if (!packageSet.add(packageName))
					continue nextEntry; // already existed
				last = packageName.lastIndexOf('/');
			}
		}
		return packageSet;
	}

}
