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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.util.StringUtils;

/**
 * Utility class that provides several helper methods for working with Eclipse's
 * JDT.
 * @author Christian Dupuis
 * @since 2.0
 */
public class JdtUtils {

	private static final String AJDT_NATURE = "org.eclipse.ajdt.ui.ajnature";

	private static final String AJDT_CLASS = "org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager";

	private static final boolean IS_AJDT_PRESENT;

	static {
		// this is not working if AJDT will be installed
		// without restarting Eclipse
		IS_AJDT_PRESENT = isAjdtPresent();
	}

	public static IJavaProject getJavaProject(IResource config) {
		IJavaProject project = JavaCore.create(config.getProject());
		return project;
	}

	public static int getLineNumber(IJavaElement element) {

		if (element != null && element instanceof IMethod) {
			try {
				IMethod method = (IMethod) element;
				int lines = 0;
				String targetsource;
				if (method.getDeclaringType() != null
						&& method.getDeclaringType().getCompilationUnit() != null) {
					targetsource = method.getDeclaringType()
							.getCompilationUnit().getSource();
					String sourceuptomethod = targetsource.substring(0, method
							.getNameRange().getOffset());

					char[] chars = new char[sourceuptomethod.length()];
					sourceuptomethod.getChars(0, sourceuptomethod.length(),
							chars, 0);
					for (char element0 : chars) {
						if (element0 == '\n') {
							lines++;
						}
					}
					return new Integer(lines + 1);
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IType) {
			try {
				IType type = (IType) element;
				int lines = 0;
				String targetsource;
				targetsource = type.getCompilationUnit().getSource();
				String sourceuptomethod = targetsource.substring(0, type
						.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars,
						0);
				for (char element0 : chars) {
					if (element0 == '\n') {
						lines++;
					}
				}
				return new Integer(lines + 1);
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IField) {
			try {
				IField type = (IField) element;
				int lines = 0;
				String targetsource;
				targetsource = type.getCompilationUnit().getSource();
				String sourceuptomethod = targetsource.substring(0, type
						.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars,
						0);
				for (char element0 : chars) {
					if (element0 == '\n') {
						lines++;
					}
				}
				return new Integer(lines + 1);
			}
			catch (JavaModelException e) {
			}
		}
		return new Integer(-1);
	}

	public static IMethod getMethod(IType type, String methodName,
			Class[] parameterTypes) {
		String[] parameterTypesAsString = getParameterTypesAsStringArray(parameterTypes);
		return getMethod(type, methodName, parameterTypesAsString);
	}

	public static IMethod getMethod(IType type, String methodName,
			String[] parameterTypes) {
		int index = methodName.indexOf('(');
		if (index >= 0) {
			methodName = methodName.substring(0, index);
		}
		try {
			Set<IMethod> methods = Introspector.getAllMethods(type);
			for (IMethod method : methods) {
				if (method.getElementName().equals(methodName)
						&& method.getParameterTypes().length == parameterTypes.length) {
					String[] methodParameterTypes = getParameterTypesAsStringArray(method);
					if (Arrays.deepEquals(parameterTypes, methodParameterTypes)) {
						return method;
					}
				}
			}

			return Introspector.findMethod(type, methodName,
					parameterTypes.length, Public.YES, Static.DONT_CARE);
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	private static String[] getParameterTypesAsStringArray(
			Class[] parameterTypes) {
		String[] parameterTypesAsString = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesAsString[i] = parameterTypes[i].getName();
		}
		return parameterTypesAsString;
	}

	private static String[] getParameterTypesAsStringArray(IMethod method) {
		String[] parameterTypesAsString = new String[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameterTypesAsString[i] = resolveClassName(method
					.getParameterTypes()[i], method.getDeclaringType());
		}
		return parameterTypesAsString;
	}

	public static final String resolveClassName(String className, IType type) {
		try {
			className = Signature.toString(className).replace('$', '.');
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		}
		catch (JavaModelException e) {
		}

		return className;
	}

	private static List<URL> getBundleClassPath(String bundleId) {
		List<URL> paths = new ArrayList<URL>();
		try {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null) {
				String bundleClassPath = (String) bundle.getHeaders().get(
						org.osgi.framework.Constants.BUNDLE_CLASSPATH);
				String[] classPathEntries = StringUtils
						.delimitedListToStringArray(bundleClassPath, ",");
				for (String classPathEntry : classPathEntries) {
					if (".".equals(classPathEntry.trim())) {
						paths.add(FileLocator.toFileURL(bundle.getEntry("/")));
					}
					else {
						paths.add(FileLocator.toFileURL(new URL(bundle
								.getEntry("/"), "/" + classPathEntry.trim())));
					}
				}
			}
		}
		catch (MalformedURLException e) {
			SpringCore.log(e);
		}
		catch (IOException e) {
			SpringCore.log(e);
		}
		return paths;
	}

	public static ClassLoader getClassLoader(IJavaProject project) {
		return getClassLoader(project, true);
	}

	public static ClassLoader getClassLoader(IJavaProject project,
			boolean useParentClassLoader) {
		List<URL> paths = getClassPathURLs(project, useParentClassLoader);
		if (useParentClassLoader) {
			return new URLClassLoader(paths.toArray(new URL[paths.size()]),
					Thread.currentThread().getContextClassLoader());
		}
		else {
			return new URLClassLoader(paths.toArray(new URL[paths.size()]));
		}
	}

	public static ClassLoader getClassLoader(IResource resource) {
		if (isJavaProject(resource)) {
			return getClassLoader(JavaCore.create(resource.getProject()));
		}
		return Thread.currentThread().getContextClassLoader();
	}

	public static List<URL> getClassPathURLs(IJavaProject project,
			boolean useParentClassLoader) {
		List<URL> paths = new ArrayList<URL>();

		if (!useParentClassLoader) {
			// add required libraries from osgi bundles
			paths.addAll(getBundleClassPath("org.springframework"));
			paths.addAll(getBundleClassPath("org.aspectj.aspectjweaver"));
			paths.addAll(getBundleClassPath("jakarta.commons.logging"));
			paths.addAll(getBundleClassPath("org.objectweb.asm"));
		}

		try {
			// configured classpath
			IClasspathEntry[] classpath = project.getResolvedClasspath(true);
			// build output, relative to project
			IPath location = SpringCoreUtils.getProjectLocation(project
					.getProject());
			IPath outputPath = location.append(project.getOutputLocation()
					.removeFirstSegments(1));

			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry path = classpath[i];
				if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					File file = path.getPath().toFile();
					if (file.exists()) {
						URL url = path.getPath().toFile().toURL();
						paths.add(url);
					}
					else {
						IPath relPath = path.getPath().removeFirstSegments(1);
						URL url = new URL("file:" + location + File.separator
								+ relPath.toOSString());
						paths.add(url);
					}
				}
				// add source output locations for different source folders
				else if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath sourceOutputPath = path.getOutputLocation();
					if (sourceOutputPath != null) {
						sourceOutputPath = location.append(sourceOutputPath
								.removeFirstSegments(1));
						paths.add(sourceOutputPath.toFile().toURL());
					}
				}
			}
			// add all depending java projects
			for (IJavaProject p : getAllDependingJavaProjects(project)) {
				paths.addAll(getClassPathURLs(p, true));
			}
			paths.add(outputPath.toFile().toURL());
		}
		catch (Exception e) {
			// ignore
		}
		return paths;
	}

	public static List<IJavaProject> getAllDependingJavaProjects(
			IJavaProject project) {
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace()
				.getRoot());
		if (model != null) {
			try {
				String[] names = project.getRequiredProjectNames();
				IJavaProject[] projects = model.getJavaProjects();
				for (int index = 0; index < projects.length; index++) {
					for (int offset = 0; offset < names.length; offset++) {
						String name = projects[index].getProject().getName();
						if (name.equals(names[offset])) {
							javaProjects.add(projects[index]);
						}
					}
				}
			}
			catch (JavaModelException exception) {
			}
		}
		return javaProjects;
	}

	/**
	 * Returns the corresponding Java project or <code>null</code> a for given
	 * project.
	 * @param project the project the Java project is requested for
	 * @return the requested Java project or <code>null</code> if the Java
	 * project is not defined or the project is not accessible
	 */
	public static IJavaProject getJavaProject(IProject project) {
		if (project.isAccessible()) {
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				}
			}
			catch (CoreException e) {
				SpringCore.log("Error getting Java project for project '"
						+ project.getName() + "'", e);
			}
		}
		return null;
	}

	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = getJavaProject(project);
		if (IS_AJDT_PRESENT && javaProject != null && className != null) {

			try {
				IType type = null;
				// First look for the type in the project
				if (isAjdtProject(project)) {
					type = AjdtUtils.getAjdtType(project, className);
				}

				if (type != null) {
					return type;
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					if (isAjdtProject(refProject)) {
						type = AjdtUtils.getAjdtType(project, className);
						if (type != null) {
							return type;
						}
					}
				}
			}
			catch (CoreException e) {
				SpringCore
						.log("Error getting Java type '" + className + "'", e);
			}
		}
		return null;
	}

	public static boolean isAjdtPresent() {
		try {
			Class.forName(AJDT_CLASS);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns true if given resource's project is a ADJT project.
	 */
	public static boolean isAjdtProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			IProject project = resource.getProject();
			if (project != null) {
				try {
					return project.hasNature(AJDT_NATURE);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if given resource's project is a Java project.
	 */
	public static boolean isJavaProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			IProject project = resource.getProject();
			if (project != null) {
				try {
					return project.hasNature(JavaCore.NATURE_ID);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		return false;
	}

	public static boolean isTypeAjdtElement(IType type) {
		if (IS_AJDT_PRESENT) {
			return AjdtUtils.isTypeAjdtElement(type);
		}
		return false;
	}
}
