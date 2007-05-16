/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class that provides several helper methods for working with java
 * reflect components.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ClassUtils {

	private static final String CLASS_FILE_SUFFIX = ".class";

	public static String getClassFileName(String className) {
		className = StringUtils.replace(className, ".", "/");
		return className + CLASS_FILE_SUFFIX;
	}

	public static Class<?> loadClass(String className)
			throws ClassNotFoundException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loader.loadClass(className);
	}

	public static String getClassLoaderHierachy(Class clazz) {
		ClassLoader cls = clazz.getClassLoader();
		StringBuffer buf = new StringBuffer(cls.getClass().getName());
		while (cls.getParent() != null) {
			cls = cls.getParent();
			buf.append(" -> ");
			buf.append(cls.getClass().getName());
		}
		return buf.toString();
	}

	public static String getClassLocation(Class clazz) {
		Assert.notNull(clazz);
		String resourceName = org.springframework.util.ClassUtils
				.getClassFileName(clazz);
		String location = null;
		try {
			URL url = clazz.getResource(resourceName);
			if (url != null) {
				URL nativeUrl = FileLocator.resolve(url);
				if (nativeUrl != null) {
					location = nativeUrl.getFile();
				}
			}
		}
		catch (IOException e) {
		}

		if (location != null) {
			// remove path behind jar file
			int ix = location.lastIndexOf('!');
			location = location.substring(0, ix);
		}

		return location;
	}

	public static String getClassVersion(Class clazz) {
		String version = "unkown";
		if (clazz.getPackage().getImplementationVersion() != null) {
			version = clazz.getPackage().getImplementationVersion();
		}
		return version;
	}

}
