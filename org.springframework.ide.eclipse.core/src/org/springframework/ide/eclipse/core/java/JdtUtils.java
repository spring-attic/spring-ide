/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.util.StringUtils;

/**
 * Utility class that provides several helper methods for working with Eclipse's JDT.
 * @author Christian Dupuis
 * @since 2.0
 */
public class JdtUtils {

	public static final String JAVA_FILE_EXTENSION = ".java";

	public static final String CLASS_FILE_EXTENSION = ".class";

	private static final String FILE_SCHEME = "file";

	private static final String AJDT_CLASS = "org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager";

	private static final String AJDT_NATURE = "org.eclipse.ajdt.ui.ajnature";

	private static final String CLASSPATH_FILENAME = ".classpath";

	private static final String DEBUG_OPTION = SpringCore.PLUGIN_ID + "/java/classloader/debug";

	private static boolean DEBUG_CLASSLOADER = SpringCore.isDebug(DEBUG_OPTION);

	private static final boolean IS_AJDT_PRESENT = isAjdtPresent();

	/**
	 * Add {@link URL}s to the given set of <code>paths</code>.
	 */
	private static void addClassPathUrls(IProject project, Set<URL> paths,
			Set<IProject> resolvedProjects) {
 
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

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

				// add class path entries
				for (int i = 0; i < classpath.length; i++) {
					IClasspathEntry path = classpath[i];
					if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						IPath entryPath = path.getPath();
						File file = entryPath.toFile();
						if (file.exists()) {
							paths.add(file.toURL());
						}
						else {
							// case for project relative links
							String projectName = entryPath.segment(0);
							IProject pathProject = root.getProject(projectName);
							covertPathToUrl(pathProject, paths, entryPath);
						}
					}
					else if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						// add source path as well for non java resources
						IPath sourcePath = path.getPath();
						covertPathToUrl(project, paths, sourcePath);
						// add source output locations for different source
						// folders
						IPath sourceOutputPath = path.getOutputLocation();
						covertPathToUrl(project, paths, sourceOutputPath);
					}
				}
				// add all depending java projects
				for (IJavaProject p : getAllDependingJavaProjects(jp)) {
					addClassPathUrls(p.getProject(), paths, resolvedProjects);
				}

				// get default output directory
				IPath outputPath = jp.getOutputLocation();
				covertPathToUrl(project, paths, outputPath);
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

	private static void covertPathToUrl(IProject project, Set<URL> paths, IPath path)
			throws MalformedURLException {
		if (path != null && project != null && path.removeFirstSegments(1) != null
				&& project.findMember(path.removeFirstSegments(1)) != null) {

			URI uri = project.findMember(path.removeFirstSegments(1)).getRawLocationURI();

			if (uri != null) {
				String scheme = uri.getScheme();
				if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
					addUri(paths, uri);
				}
				else if ("sourcecontrol".equals(scheme)) {
					// special case of Rational Team Concert
					IPath sourceControlPath = project.findMember(path.removeFirstSegments(1))
							.getLocation();
					File sourceControlFile = sourceControlPath.toFile();
					if (sourceControlFile.exists()) {
						addUri(paths, sourceControlFile.toURI());
					}
				}
				else {
					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace()
							.getPathVariableManager();
					addUri(paths, variableManager.resolveURI(uri));
				}
			}
		}
	}

	private static void addUri(Set<URL> paths, URI uri) throws MalformedURLException {
		File file = new File(uri);
		if (file.exists()) {
			if (file.isDirectory()) {
				paths.add(new URL(uri.toString() + File.separator));
			}
			else {
				paths.add(uri.toURL());
			}
		}
	}

	/**
	 * Creates specified Java project.
	 */
	public static IJavaProject createJavaProject(String projectName, IProgressMonitor monitor)
			throws CoreException {
		IProject project = SpringCoreUtils.createProject(projectName, null, monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			SpringCoreUtils.addProjectNature(project, JavaCore.NATURE_ID, monitor);
		}
		IJavaProject jproject = JavaCore.create(project);
		// append JRE entry
		jproject.setRawClasspath(new IClasspathEntry[] { getJreVariableEntry() }, monitor);
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
					if (type != null) {
						return type;
					}
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
				SpringCore.log("Error getting Java type '" + className + "'", e);
			}
		}
		return null;
	}

	public static List<IJavaProject> getAllDependingJavaProjects(IJavaProject project) {
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
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
	 * Creates a Set of {@link URL}s from the OSGi bundle class path manifest entry.
	 */
	private static Set<URL> getBundleClassPath(String bundleId) {
		Set<URL> paths = new HashSet<URL>();
		try {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null) {
				String bundleClassPath = (String) bundle.getHeaders().get(
						org.osgi.framework.Constants.BUNDLE_CLASSPATH);
				if (bundleClassPath != null) {
					String[] classPathEntries = StringUtils.delimitedListToStringArray(
							bundleClassPath, ",");
					for (String classPathEntry : classPathEntries) {
						if (".".equals(classPathEntry.trim())) {
							paths.add(FileLocator.toFileURL(bundle.getEntry("/")));
						}
						else {
							paths.add(FileLocator.toFileURL(new URL(bundle.getEntry("/"), "/"
									+ classPathEntry.trim())));
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
	 * Create a {@link ClassLoader} from the class path configuration of the given
	 * <code>project</code>.
	 * <p>
	 * Note: Calling this method is the same as calling {@link #getClassLoader(IProject, true)}
	 * @param project the {@link IProject}
	 * @return {@link ClassLoader} instance constructed from the <code>project</code>'s build path
	 * configuration
	 */
	public static ClassLoader getClassLoader(IResource project) {
		return getClassLoader(project.getProject(), true);
	}

	/**
	 * Create a {@link ClassLoader} from the class path configuration of the given
	 * <code>project</code>.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader true if the current OSGi class loader should be used as parent
	 * class loader for the constructed class loader.
	 * @return {@link ClassLoader} instance constructed from the <code>project</code>'s build path
	 * configuration
	 */
	public static ClassLoader getClassLoader(IProject project, boolean useParentClassLoader) {
		// prepare for tracing
		long start = System.currentTimeMillis();
		try {
			Set<URL> paths = getClassPathUrls(project, useParentClassLoader);
			if (useParentClassLoader) {
				return new URLClassLoader(paths.toArray(new URL[paths.size()]), Thread
						.currentThread().getContextClassLoader());
			}
			else {
				return new URLClassLoader(paths.toArray(new URL[paths.size()]));
			}
		}
		finally {
			if (DEBUG_CLASSLOADER) {
				System.out.println("getClassLoader for '" + project.getProject().getName()
						+ "' took " + (System.currentTimeMillis() - start) + "ms");
			}
		}
	}

	/**
	 * Iterates all class path entries of the given <code>project</code> and all depending projects.
	 * <p>
	 * Note: if <code>useParentClassLoader</code> is true, the Spring, AspectJ, Commons Logging and
	 * ASM bundles are automatically added to the paths.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader use the OSGi classloader as parent
	 * @return a set of {@link URL}s that can be used to construct a {@link URLClassLoader}
	 */
	private static Set<URL> getClassPathUrls(IProject project, boolean useParentClassLoader) {

		// needs to be linked to preserve ordering
		Set<URL> paths = new LinkedHashSet<URL>();
		if (!useParentClassLoader) {
			// add required libraries from osgi bundles
			paths.addAll(getBundleClassPath("org.springframework.core"));
			paths.addAll(getBundleClassPath("org.springframework.beans"));
			paths.addAll(getBundleClassPath("org.springframework.aop"));
			paths.addAll(getBundleClassPath("com.springsource.org.aspectj.weaver"));
			paths.addAll(getBundleClassPath("com.springsource.org.apache.logging"));
			paths.addAll(getBundleClassPath("com.springsource.org.objectweb.asm"));
		}

		Set<IProject> resolvedProjects = new HashSet<IProject>();
		addClassPathUrls(project, paths, resolvedProjects);

		return paths;
	}

	/**
	 * Returns the corresponding Java project or <code>null</code> a for given project.
	 * @param project the project the Java project is requested for
	 * @return the requested Java project or <code>null</code> if the Java project is not defined or
	 * the project is not accessible
	 */
	public static IJavaProject getJavaProject(IProject project) {
		if (project.isAccessible()) {
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				}
			}
			catch (CoreException e) {
				SpringCore.log(
						"Error getting Java project for project '" + project.getName() + "'", e);
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
	 * @return the requested Java type or null if the class is not defined or the project is not
	 * accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (className != null) {
			boolean innerClass = false;
			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0) {
				className = className.replace('$', '.');
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
					IJavaProject refJavaProject = JdtUtils.getJavaProject(refProject);
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
				SpringCore.log("Error getting Java type '" + className + "'", e);
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
					targetsource = method.getDeclaringType().getCompilationUnit().getSource();
					String sourceuptomethod = targetsource.substring(0, method.getNameRange()
							.getOffset());

					char[] chars = new char[sourceuptomethod.length()];
					sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
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
		else if (element != null && element instanceof IType
				&& ((IType) element).getCompilationUnit() != null) {
			try {
				IType type = (IType) element;
				int lines = 0;
				String targetsource;
				targetsource = type.getCompilationUnit().getSource();
				String sourceuptomethod = targetsource
						.substring(0, type.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
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
				String sourceuptomethod = targetsource
						.substring(0, type.getNameRange().getOffset());

				char[] chars = new char[sourceuptomethod.length()];
				sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
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

	public static IMethod getMethod(IType type, String methodName, Class[] parameterTypes) {
		String[] parameterTypesAsString = getParameterTypesAsStringArray(parameterTypes);
		return getMethod(type, methodName, parameterTypesAsString);
	}

	public static IMethod getMethod(IType type, String methodName, String[] parameterTypes) {
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

			return Introspector.findMethod(type, methodName, parameterTypes.length, Public.YES,
					Static.DONT_CARE);
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	private static String[] getParameterTypesAsStringArray(Class[] parameterTypes) {
		String[] parameterTypesAsString = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesAsString[i] = parameterTypes[i].getName();
		}
		return parameterTypesAsString;
	}

	private static String[] getParameterTypesAsStringArray(IMethod method) {
		String[] parameterTypesAsString = new String[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameterTypesAsString[i] = resolveClassNameBySignature(method.getParameterTypes()[i],
					method.getDeclaringType());
		}
		return parameterTypesAsString;
	}

	public static IProjectClassLoaderSupport getProjectClassLoaderSupport(IProject je) {
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
	 * Determines if the <code>resource</code> under question is the .classpath file of a
	 * {@link IJavaProject}.
	 */
	public static boolean isClassPathFile(IResource resource) {
		String classPathFileName = resource.getProject().getFullPath().append(CLASSPATH_FILENAME)
				.toString();
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
		// replace binary $ inner class name syntax with . for source level
		className = className.replace('$', '.');
		String dotClassName = new StringBuilder().append('.').append(className).toString();

		IProject project = type.getJavaProject().getProject();

		try {
			// Special handling for some well-know classes
			if (className.startsWith("java.lang") && getJavaType(project, className) != null) {
				return className;
			}

			// Check if the class is imported
			if (!type.isBinary()) {

				// Strip className to first segment to support ReflectionUtils.MethodCallback
				int ix = className.lastIndexOf('.');
				String firstClassNameSegment = className;
				if (ix > 0) {
					firstClassNameSegment = className.substring(0, ix);
				}

				// Iterate the imports
				for (IImportDeclaration importDeclaration : type.getCompilationUnit().getImports()) {
					String importName = importDeclaration.getElementName();
					// Wildcard imports -> check if the package + className is a valid type
					if (importDeclaration.isOnDemand()) {
						String newClassName = new StringBuilder(importName.substring(0, importName
								.length() - 1)).append(className).toString();
						if (getJavaType(project, newClassName) != null) {
							return newClassName;
						}
					}
					// Concrete import matching .className at the end -> check if type exists
					else if (importName.endsWith(dotClassName)
							&& getJavaType(project, importName) != null) {
						return importName;
					}
					// Check if className is multi segmented (ReflectionUtils.MethodCallback) 
					// -> check if the first segment
					else if (!className.equals(firstClassNameSegment)) {
						if (importName.endsWith(firstClassNameSegment)) {
							String newClassName = new StringBuilder(importName.substring(0,
									importName.lastIndexOf('.') + 1)).append(className).toString();
							if (getJavaType(project, newClassName) != null) {
								return newClassName;
							}
						}
					}
				}
			}
			
			// Check if the class is in the same package as the type
			String packageName = type.getPackageFragment().getElementName();
			String newClassName = new StringBuilder(packageName).append(dotClassName).toString();
			if (getJavaType(project, newClassName) != null) {
				return newClassName;
			}

			// Check if the className is sufficient (already fully-qualified)
			if (getJavaType(project, className) != null) {
				return className;
			}

			// Check if the class is coming from the java.lang
			newClassName = new StringBuilder("java.lang").append(dotClassName).toString();
			if (getJavaType(project, newClassName) != null) {
				return newClassName;
			}

			// Fall back to full blown resolution
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		}
		catch (JavaModelException e) {
			SpringCore.log(e);
		}

		return className;
	}

	public static String resolveClassNameBySignature(String className, IType type) {
		className = Signature.toString(className).replace('$', '.');
		return resolveClassName(className, type);
	}

	public static IType getJavaTypeFromSignatureClassName(String className, IType contextType) {
		if (contextType == null || className == null) {
			return null;
		}
		try {
			return JdtUtils.getJavaType(contextType.getJavaProject().getProject(), JdtUtils
					.resolveClassNameBySignature(className, contextType));
		}
		catch (IllegalArgumentException e) {
			// do Nothing
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static final List<IType> getJavaTypesForMethodParameterTypes(IMethod method,
			IType contextType) {
		if (method == null || method.getParameterTypes() == null
				|| method.getParameterTypes().length == 0) {
			return Collections.EMPTY_LIST;
		}
		List<IType> parameterTypes = new ArrayList<IType>(method.getParameterTypes().length);
		String[] parameterTypeNames = method.getParameterTypes();
		for (String parameterTypeName : parameterTypeNames) {
			parameterTypes.add(JdtUtils.getJavaTypeFromSignatureClassName(parameterTypeName,
					contextType));
		}
		return parameterTypes;
	}

	public static final IType getJavaTypeForMethodReturnType(IMethod method, IType contextType) {
		try {
			return JdtUtils.getJavaTypeFromSignatureClassName(method.getReturnType(), contextType);
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static String[] getParameterTypesString(IMethod method) {
		try {
			String[] parameterQualifiedTypes = Signature.getParameterTypes(method.getSignature());
			int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
			String[] parameterPackages = new String[length];
			for (int i = 0; i < length; i++) {
				parameterQualifiedTypes[i] = parameterQualifiedTypes[i].replace('/', '.');
				parameterPackages[i] = Signature.getSignatureSimpleName(parameterQualifiedTypes[i]);
			}
			return parameterPackages;
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static String getReturnTypeString(IMethod method, boolean classTypesOnly) {
		try {
			String qualifiedReturnType = Signature.getReturnType(method.getSignature());
			if (!classTypesOnly || qualifiedReturnType.startsWith("L")
					|| qualifiedReturnType.startsWith("Q")) {
				return Signature.getSignatureSimpleName(qualifiedReturnType.replace('/', '.'));
			}
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static String getPropertyNameFromMethodName(IMethod method) {
		// Special support Ajdt intertype declarations
		String methodName = method.getElementName();
		int index = methodName.lastIndexOf('.');
		if (index > 0) {
			methodName = methodName.substring(index + 1);
		}
		String replaceText = methodName.substring("set".length());
		if (replaceText != null) {
			replaceText = java.beans.Introspector.decapitalize(replaceText);
		}
		return replaceText;
	}

	/**
	 * Returns a flat list of all interfaces and super types for the given {@link IType}.
	 */
	public static List<String> getFlatListOfClassAndInterfaceNames(IType parameterType, IType type) {
		List<String> requiredTypes = new ArrayList<String>();
		if (parameterType != null) {
			do {
				try {
					requiredTypes.add(parameterType.getFullyQualifiedName());
					String[] interfaceNames = parameterType.getSuperInterfaceNames();
					for (String interfaceName : interfaceNames) {
						if (interfaceName != null) {
							if (type.isBinary()) {
								requiredTypes.add(interfaceName);
							}
							String resolvedName = resolveClassName(interfaceName, type);
							if (resolvedName != null) {
								requiredTypes.add(resolvedName);
							}
						}
					}
					parameterType = Introspector.getSuperType(parameterType);
				}
				catch (JavaModelException e) {
				}
			} while (parameterType != null
					&& !parameterType.getFullyQualifiedName().equals(Object.class.getName()));
		}
		return requiredTypes;
	}

	public static void visitTypeAst(IType type, ASTVisitor visitor) {
		if (type != null && type.getCompilationUnit() != null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(type.getCompilationUnit());
			parser.setResolveBindings(true);
			ASTNode node = parser.createAST(new NullProgressMonitor());
			node.accept(visitor);
		}
	}

	public static IResource getSourceResource(IResource classFile) {
		try {
			if (isJavaProject(classFile) && classFile.getName().endsWith(CLASS_FILE_EXTENSION)) {
				IPath classFilePath = classFile.getFullPath();
				String classFileName = null;

				IJavaProject project = getJavaProject(classFile);
				IPath defaultOutput = project.getOutputLocation();

				if (defaultOutput.isPrefixOf(classFilePath)) {
					classFileName = classFilePath.removeFirstSegments(defaultOutput.segmentCount())
							.toString();
				}
				else {
					for (IClasspathEntry entry : project.getRawClasspath()) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath output = entry.getOutputLocation();
							if (output != null) {
								if (classFilePath.isPrefixOf(output)) {
									classFileName = classFilePath.removeFirstSegments(
											output.segmentCount()).toString();
								}
							}
						}
					}
				}

				if (classFileName != null) {
					// Replace file extension
					String sourceFileName = classFileName.replace(".class", ".java");
					for (IClasspathEntry entry : project.getRawClasspath()) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath path = entry.getPath().append(sourceFileName)
									.removeFirstSegments(1);
							IResource resource = project.getProject().findMember(path);
							if (resource != null) {
								return resource;
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	static class DefaultProjectClassLoaderSupport implements IProjectClassLoaderSupport {

		private ClassLoader classLoader;

		private ClassLoader weavingClassLoader;

		public DefaultProjectClassLoaderSupport(IProject javaProject) {
			setupClassLoaders(javaProject);
		}

		/**
		 * Activates the weaving class loader as thread context classloader.
		 * <p>
		 * Use {@link #recoverClassLoader()} to recover the original thread context classloader
		 */
		private void activateWeavingClassLoader() {
			Thread.currentThread().setContextClassLoader(weavingClassLoader);
		}

		public void executeCallback(IProjectClassLoaderAwareCallback callback) throws Throwable {
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

		private void setupClassLoaders(IProject project) {
			classLoader = Thread.currentThread().getContextClassLoader();
			weavingClassLoader = JdtUtils.getClassLoader(project, false);
		}
	}

}
