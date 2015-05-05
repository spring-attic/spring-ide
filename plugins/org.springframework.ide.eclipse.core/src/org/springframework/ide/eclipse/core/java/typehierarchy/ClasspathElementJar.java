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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class ClasspathElementJar implements ClasspathElement {
	
	private JarFile jarFile;
	private String jarFileName;
	private Set<String> knownPackageNames;
	private long lastModified;

	public ClasspathElementJar(String jarFileName) {
		this.jarFileName = jarFileName;
	}

	public InputStream getStream(String fullyQualifiedClassFileName, String packageName, String classFileName) throws Exception {
		if (!isPackage(packageName)) return null;
		
		ZipEntry entry = jarFile.getEntry(fullyQualifiedClassFileName);
		if (entry != null) {
			return jarFile.getInputStream(entry);
		}
		return null;
	}

	public void cleanup() {
		synchronized(this) {
			if (this.jarFile != null) {
				try {
					this.jarFile.close();
				} catch(IOException e) { // ignore it
				}
				this.jarFile = null;
			}
			this.knownPackageNames = null;
		}
	}

	public long lastModified() {
		if (this.lastModified == 0)
			this.lastModified = new File(this.jarFile.getName()).lastModified();
		return this.lastModified;
	}

	private boolean isPackage(String qualifiedPackageName) {
		if (this.knownPackageNames != null)
			return this.knownPackageNames.contains(qualifiedPackageName);

		try {
			synchronized(this) {
				if (this.jarFile == null) {
					this.jarFile = new JarFile(this.jarFileName);
				}
				this.knownPackageNames = findPackageSet();
			}
		} catch(Exception e) {
			this.knownPackageNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		}
		return this.knownPackageNames.contains(qualifiedPackageName);
	}

	private Set<String> findPackageSet() {
		long lastModified = lastModified();
		long fileSize = new File(jarFileName).length();
		PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(jarFileName);
		if (cacheEntry != null && cacheEntry.lastModified == lastModified && cacheEntry.fileSize == fileSize)
			return cacheEntry.packageSet;
		
		Set<String> packageSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		packageSet.add(""); //$NON-NLS-1$
		nextEntry : for (Enumeration e = jarFile.entries(); e.hasMoreElements(); ) {
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

		PackageCache.put(jarFileName, new PackageCacheEntry(lastModified, fileSize, packageSet));
		return packageSet;
	}
	
	// global zip file content cache
	private static Map<String, PackageCacheEntry> PackageCache = new ConcurrentHashMap<String, PackageCacheEntry>();
	
	private static class PackageCacheEntry {
		long lastModified;
		long fileSize;
		Set<String> packageSet;

		public PackageCacheEntry(long lastModified, long fileSize, Set<String> packageSet) {
			this.lastModified = lastModified;
			this.fileSize = fileSize;
			this.packageSet = packageSet;
		}
	}

}
