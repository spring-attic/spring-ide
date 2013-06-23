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
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
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
	private long lastModified;

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

	public long lastModified() {
		if (this.lastModified == 0)
			this.lastModified = new File(this.zipFile.getName()).lastModified();
		return this.lastModified;
	}

	private boolean isPackage(String qualifiedPackageName) {
		if (this.knownPackageNames != null)
			return this.knownPackageNames.contains(qualifiedPackageName);

		try {
			this.knownPackageNames = findPackageSet();
		} catch(Exception e) {
			this.knownPackageNames = new HashSet<String>();
		}
		return this.knownPackageNames.contains(qualifiedPackageName);
	}

	private Set<String> findPackageSet() {
		String zipFileName = zipFile.getName();
		long lastModified = lastModified();
		long fileSize = new File(zipFileName).length();
		PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
		if (cacheEntry != null && cacheEntry.lastModified == lastModified && cacheEntry.fileSize == fileSize)
			return cacheEntry.packageSet;
		
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

		PackageCache.put(zipFileName, new PackageCacheEntry(lastModified, fileSize, packageSet));
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
