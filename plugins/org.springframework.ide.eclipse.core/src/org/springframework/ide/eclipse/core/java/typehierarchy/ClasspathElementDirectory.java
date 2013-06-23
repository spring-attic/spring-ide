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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class ClasspathElementDirectory implements ClasspathElement {
	
	private String directoryPath;
	private Map<String, String[]> directoryCache;
	private String[] missingPackageHolder;

	public ClasspathElementDirectory(File file) {
		this.directoryPath = file.getAbsolutePath();
		this.directoryCache = new ConcurrentHashMap<String, String[]>();
		this.missingPackageHolder = new String[1];
	}

	public InputStream getStream(String fullyQualifiedClassFileName, String packageName, String classFileName) throws Exception {
		if (!doesFileExist(fullyQualifiedClassFileName, packageName, classFileName)) return null;
		
		try {
			return new FileInputStream(this.directoryPath + File.separatorChar + fullyQualifiedClassFileName);
		}
		catch (IOException e) {
			return null;
		}
	}

	private boolean doesFileExist(String fullyQualifiedClassFileName, String packageName, String classFileName) {
		String[] dirList = directoryList(packageName);
		if (dirList == null) return false;

		for (int i = dirList.length; --i >= 0;)
			if (classFileName.equals(dirList[i]))
				return true;
		return false;
	}
	
	private String[] directoryList(String qualifiedPackageName) {
		String[] dirList = this.directoryCache.get(qualifiedPackageName);
		if (dirList == this.missingPackageHolder) return null;
		if (dirList != null) return dirList;

		File packageDir = new File(directoryPath + File.separatorChar + qualifiedPackageName);
		String[] list = packageDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}
		});

		if (list != null) {
			this.directoryCache.put(qualifiedPackageName, list);
			return list;
		}

		this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
		return null;
	}

}
