/*******************************************************************************
 * Copyright (c) 2007 - 2013 Spring IDE Developers
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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class that provides several helper methods for working with java
 * reflect components.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.0
 */
public class ClassUtils {

	public static final String CLASS_FILE_SUFFIX = ".class";

	public static String getClassFileName(String className) {
		className = StringUtils.replace(className, ".", "/");
		return className + CLASS_FILE_SUFFIX;
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

	public static Object invokeMethod(Object target, String methodName,
			Object... parameters) throws Throwable {

		if (target == null) {
			return null;
		}

		if (parameters != null && parameters.length > 0) {
			List<Object> parameterClasses = new ArrayList<Object>();
			for (Object parameter : parameters) {
				parameterClasses.add(parameter.getClass());
			}
			return invokeMethod(target, methodName, parameters, 
					parameterClasses.toArray(new Class[parameterClasses.size()]));
		}
		else {
			return invokeMethod(target, methodName, parameters, new Class[0]);
		}
	}

	/**
	 * Invokes a target method identified by given <code>methodName</code>,
	 * <code>parameters</code> and <code>parameterClasses</code>.
	 * <p>
	 * If the <code>target</code> is an instance of {@link Class} this method
	 * assumes that the invocation should be made statically.
	 * <p>
	 * Note: This method - in contrast to the
	 * {@link #invokeMethod(Object, String, Object...)} method - does support
	 * primitive types as parameter types.
	 * @since 2.0.1
	 */
	public static Object invokeMethod(Object target, String methodName,
			Object[] parameters, Class[] parameterClasses) throws Throwable {

		if (target == null) {
			return null;
		}

		Class targetClass = target.getClass();
		if (target instanceof Class) {
			targetClass = (Class) target;
		}
		Method targetMethod = ReflectionUtils.findMethod(targetClass, 
				methodName, parameterClasses);

		if (targetMethod != null) {
			ReflectionUtils.makeAccessible(targetMethod);
			if (target instanceof Class) {
				return ReflectionUtils.invokeMethod(targetMethod, null, parameters);
			}
			else {
				return ReflectionUtils.invokeMethod(targetMethod, target, parameters);
			}
		}
		return null;

	}

	public static Class<?> loadClass(String className)
			throws ClassNotFoundException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loadClass(className, loader);
	}

	public static Class<?> loadClass(Class clazz) throws ClassNotFoundException {
		return loadClass(clazz.getName());
	}
	
	public static Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
		try {
			return loader.loadClass(className);
		}
		catch (ClassNotFoundException ex) {
			int lastDotIndex = className.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName = className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
				try {
					return loader.loadClass(innerClassName);
				}
				catch (ClassNotFoundException ex2) {
					// swallow - let original exception get through
				}
			}
			throw ex;
		}
	}
	
}
