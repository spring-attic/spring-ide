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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.eclipse.jdt.launching.JavaRuntime;
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

	static class DefaultProjectClassLoaderSupport implements
			IProjectClassLoaderSupport {

		private ClassLoader classLoader;

		private ClassLoader weavingClassLoader;

		public DefaultProjectClassLoaderSupport(IJavaProject javaProject) {
			setupClassLoaders(javaProject);
		}

		/**
		 * Activates the weaving class loader as thread context classloader.
		 * <p>
		 * Use {@link #recoverClassLoader()} to recover the original thread
		 * context classloader
		 */
		private void activateWeavingClassLoader() {
			Thread.currentThread().setContextClassLoader(weavingClassLoader);
		}

		public void executeCallback(IProjectClassLoaderAwareCallback callback)
				throws Throwable {
			try {
				activateWeavingClassLoader();
				callback.doWithActiveProjectClassLoader();
			}
			finally {
				recoverClassLoader();
			}
		}

		public ClassLoader getProjectClassLoader() {
			return this.weavingClassLoader;
		}

		private void recoverClassLoader() {
			Thread.currentThread().setContextClassLoader(classLoader);
		}

		private void setupClassLoaders(IJavaProject javaProject) {
			classLoader = Thread.currentThread().getContextClassLoader();
			weavingClassLoader = JdtUtils.getClassLoader(javaProject
					.getProject(), false);
		}
	}

	private static final String AJDT_CLASS = "org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager";

	private static final String AJDT_NATURE = "org.eclipse.ajdt.ui.ajnature";

	private static final String CLASSPATH_FILENAME = ".classpath";

	private static final String DEBUG_OPTION = SpringCore.PLUGIN_ID
			+ "/java/classloader/debug";

	private static boolean DEBUG_CLASSLOADER = SpringCore.isDebug(DEBUG_OPTION);

	private static final boolean IS_AJDT_PRESENT = isAjdtPresent();

	/**
	 * Add {@link URL}s to the given set of <code>paths</code>.
	 */
	private static void addClassPathUrls(IProject project, Set<URL> paths,
			Set<IProject> resolvedProjects) {

		// add project to local cache to prevent adding its classpaths
		// multiple times
		if (resolvedProjects.contains(project)) {
			return;
		}
		else {
			resolvedProjects.add(project);
		}

		try {
			if (isJavaProject(project)) {
				IJavaProject jp = JavaCore.create(project);
				// configured classpath
				IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
				// build output, relative to project
				IPath location = SpringCoreUtils.getProjectLocation(project
						.getProject());
				IPath outputPath = location.append(jp.getOutputLocation()
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
							String projectName = path.getPath().segment(0);
							IProject pathProject = ResourcesPlugin
									.getWorkspace().getRoot().getProject(
											projectName);
							IPath pathLocation = SpringCoreUtils
									.getProjectLocation(pathProject);
							IPath relPath = path.getPath().removeFirstSegments(
									1);
							URL url = new URL("file:" + pathLocation
									+ File.separator + relPath.toOSString());
							paths.add(url);
						}
					}
					else if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						// add source path as well for non java resources
						IPath sourcePath = path.getPath();
						if (sourcePath != null) {
							sourcePath = location.append(sourcePath
									.removeFirstSegments(1));
							paths.add(sourcePath.toFile().toURL());
						}
						// add source output locations for different source
						// folders
						IPath sourceOutputPath = path.getOutputLocation();
						if (sourceOutputPath != null) {
							sourceOutputPath = location.append(sourceOutputPath
									.removeFirstSegments(1));
							paths.add(sourceOutputPath.toFile().toURL());
						}
					}
				}
				// add all depending java projects
				for (IJavaProject p : getAllDependingJavaProjects(jp)) {
					paths.addAll(getClassPathUrls(p.getProject(), true));
				}
				paths.add(outputPath.toFile().toURL());
			}
			else {
				for (IProject p : project.getReferencedProjects()) {
					addClassPathUrls(p, paths, resolvedProjects);
				}
			}
		}
		catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Creates specified Java project.
	 */
	public static IJavaProject createJavaProject(String projectName,
			IProgressMonitor monitor) throws CoreException {
		IProject project = SpringCoreUtils.createProject(projectName, null,
				monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			SpringCoreUtils.addProjectNature(project, JavaCore.NATURE_ID,
					monitor);
		}
		IJavaProject jproject = JavaCore.create(project);
		// append JRE entry
		jproject.setRawClasspath(
				new IClasspathEntry[] { getJreVariableEntry() }, monitor);
		jproject.setOutputLocation(project.getFullPath(), monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return jproject;
	}

	public static IType getAjdtType(IProject project, String className) {
		IJavaProject javaProject = getJavaProject(project);
		if (IS_AJDT_PRESENT && javaProject != null && className != null) {

			try {
				IType type = null;

				// First look for the type in the project
				if (isAjdtProject(project)) {
					type = AjdtUtils.getAjdtType(project, className);
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					if (isAjdtProject(refProject)) {
						type = AjdtUtils.getAjdtType(refProject, className);
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
	 * Creates a Set of {@link URL}s from the OSGi bundle class path manifest
	 * entry.
	 */
	private static Set<URL> getBundleClassPath(String bundleId) {
		Set<URL> paths = new HashSet<URL>();
		try {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null) {
				String bundleClassPath = (String) bundle.getHeaders().get(
						org.osgi.framework.Constants.BUNDLE_CLASSPATH);
				if (bundleClassPath != null) {
					String[] classPathEntries = StringUtils
							.delimitedListToStringArray(bundleClassPath, ",");
					for (String classPathEntry : classPathEntries) {
						if (".".equals(classPathEntry.trim())) {
							paths.add(FileLocator.toFileURL(bundle
									.getEntry("/")));
						}
						else {
							paths.add(FileLocator
									.toFileURL(new URL(bundle.getEntry("/"),
											"/" + classPathEntry.trim())));
						}
					}
				}
				else {
					paths.add(FileLocator.toFileURL(bundle.getEntry("/")));
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

	/**
	 * Create a {@link ClassLoader} from the class path configuration of the
	 * given <code>project</code>.
	 * <p>
	 * Note: Calling this method is the same as calling
	 * {@link #getClassLoader(IProject, true)}
	 * @param project the {@link IProject}
	 * @return {@link ClassLoader} instance constructed from the
	 * <code>project</code>'s build path configuration
	 */
	public static ClassLoader getClassLoader(IResource project) {
		return getClassLoader(project.getProject(), true);
	}

	/**
	 * Create a {@link ClassLoader} from the class path configuration of the
	 * given <code>project</code>.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader true if the current OSGi class loader should
	 * be used as parent class loader for the constructed class loader.
	 * @return {@link ClassLoader} instance constructed from the
	 * <code>project</code>'s build path configuration
	 */
	public static ClassLoader getClassLoader(IProject project,
			boolean useParentClassLoader) {
		// prepare for tracing
		long start = System.currentTimeMillis();
		try {
			Set<URL> paths = getClassPathUrls(project, useParentClassLoader);
			if (useParentClassLoader) {
				return new URLClassLoader(paths.toArray(new URL[paths.size()]),
						Thread.currentThread().getContextClassLoader());
			}
			else {
				return new URLClassLoader(paths.toArray(new URL[paths.size()]));
			}
		}
		finally {
			if (DEBUG_CLASSLOADER) {
				System.out.println("getClassLoader for '"
						+ project.getProject().getName() + "' took "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		}
	}

	/**
	 * Iterates all class path entries of the given <code>project</code> and
	 * all depending projects.
	 * <p>
	 * Note: if <code>useParentClassLoader</code> is true, the Spring,
	 * AspectJ, Commons Logging and ASM bundles are automatically added to the
	 * paths.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader use the OSGi classloader as parent
	 * @return a set of {@link URL}s that can be used to construct a
	 * {@link URLClassLoader}
	 */
	private static Set<URL> getClassPathUrls(IProject project,
			boolean useParentClassLoader) {

		// needs to be linked to preserve ordering
		Set<URL> paths = new LinkedHashSet<URL>();
		if (!useParentClassLoader) {
			// add required libraries from osgi bundles
			paths.addAll(getBundleClassPath("org.springframework"));
			paths.addAll(getBundleClassPath("org.aspectj.aspectjweaver"));
			paths.addAll(getBundleClassPath("jakarta.commons.logging"));
			paths.addAll(getBundleClassPath("org.objectweb.asm"));
		}

		Set<IProject> resolvedProjects = new HashSet<IProject>();
		addClassPathUrls(project, paths, resolvedProjects);

		return paths;
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

	public static IJavaProject getJavaProject(IResource config) {
		IJavaProject project = JavaCore.create(config.getProject());
		return project;
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project the JDT project the class belongs to
	 * @param className the full qualified class name of the requested Java type
	 * @return the requested Java type or null if the class is not defined or
	 * the project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (className != null) {
			boolean innerClass = false;
			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0 && pos < (className.length() - 1)) {
				className = className.substring(0, pos) + '.'
						+ className.substring(pos + 1);
				innerClass = true;
			}
			try {
				IType type = null;
				// First look for the type in the Java project
				if (javaProject != null) {
					type = javaProject.findType(className);
					if (type != null
							&& ((type.getDeclaringType() == null && !innerClass) || (type
									.getDeclaringType() != null && innerClass))) {
						return type;
					}
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					IJavaProject refJavaProject = JdtUtils
							.getJavaProject(refProject);
					if (refJavaProject != null) {
						type = refJavaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
				}

				// fall back and try to locate the class using AJDT
				return getAjdtType(project, className);
			}
			catch (CoreException e) {
				SpringCore
						.log("Error getting Java type '" + className + "'", e);
			}
		}

		return null;
	}

	public static IClasspathEntry getJreVariableEntry() {
		return JavaRuntime.getDefaultJREContainerEntry();
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

	public static IProjectClassLoaderSupport getProjectClassLoaderSupport(
			IJavaProject je) {
		return new DefaultProjectClassLoaderSupport(je);
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
	 * Determines if the <code>resource</code> under question is the
	 * .classpath file of a {@link IJavaProject}.
	 */
	public static boolean isClassPathFile(IResource resource) {
		String classPathFileName = resource.getProject().getFullPath().append(
				CLASSPATH_FILENAME).toString();
		return resource.getFullPath().toString().equals(classPathFileName);
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

	public static String resolveClassName(String className, IType type) {
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
}
