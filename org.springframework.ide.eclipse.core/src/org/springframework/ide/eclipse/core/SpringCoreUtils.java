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
package org.springframework.ide.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class SpringCoreUtils {

	private static final String AJDT_NATURE = 
		"org.eclipse.ajdt.ui.ajnature";

	private static final String AJDT_CLASS = 
		"org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager";

	private static final boolean IS_AJDT_PRESENT;

	static {
		// this is not working if AJDT will be installed 
		// without restarting Eclipse
		IS_AJDT_PRESENT = isAjdtPresent();
	}

	/**
	 * Returns the specified adapter for the given object or <code>null</code>
	 * if adapter is not supported.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(Object object, Class<T> adapter) {
		if (object != null && adapter != null) {
			if (adapter.isAssignableFrom(object.getClass())) {
				return (T) object;
			}
			if (object instanceof IAdaptable) {
				return (T) ((IAdaptable) object).getAdapter(adapter);
			}
		}
		return null;
	}

	/**
	 * Returns a list of all projects with the Spring project nature.
	 */
	public static Set<IProject> getSpringProjects() {
		Set<IProject> projects = new LinkedHashSet<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {
			if (isSpringProject(project)) {
				projects.add(project);
			}
		}
		return projects;
	}

	/**
	 * Creates specified simple project.
	 */
	public static IProject createProject(String projectName,
			IProjectDescription description, IProgressMonitor monitor)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists()) {
			if (description == null) {
				project.create(monitor);
			}
			else {
				project.create(description, monitor);
			}
		}
		else {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.isOpen()) {
			project.open(monitor);
		}
		return project;
	}

	/**
	 * Creates specified Java project.
	 */
	public static IJavaProject createJavaProject(String projectName,
			IProgressMonitor monitor) throws CoreException {
		IProject project = createProject(projectName, null, monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			addProjectNature(project, JavaCore.NATURE_ID, monitor);
		}
		IJavaProject jproject = JavaCore.create(project);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		jproject.setRawClasspath(new IClasspathEntry[0], monitor);
		return jproject;
	}

	/**
	 * Creates given folder and (if necessary) all of it's parents.
	 */
	public static void createFolder(IFolder folder, IProgressMonitor monitor)
			throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent, monitor);
			}
			folder.create(true, true, monitor);
		}
	}

	/**
	 * Adds given nature as first nature to specified project.
	 */
	public static void addProjectNature(IProject project, String nature,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && nature != null) {
			if (!project.hasNature(nature)) {
				IProjectDescription desc = project.getDescription();
				String[] oldNatures = desc.getNatureIds();
				String[] newNatures = new String[oldNatures.length + 1];
				newNatures[0] = nature;
				if (oldNatures.length > 0) {
					System.arraycopy(oldNatures, 0, newNatures, 1,
							oldNatures.length);
				}
				desc.setNatureIds(newNatures);
				project.setDescription(desc, monitor);
			}
		}
	}

	/**
	 * Removes given nature from specified project.
	 */
	public static void removeProjectNature(IProject project, String nature,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && nature != null) {
			if (project.exists() && project.hasNature(nature)) {

				// first remove all problem markers (including the
				// inherited ones) from Spring beans project
				if (nature.equals(SpringCore.NATURE_ID)) {
					project.deleteMarkers(SpringCore.MARKER_ID, true,
							IResource.DEPTH_INFINITE);
				}

				// now remove project nature
				IProjectDescription desc = project.getDescription();
				String[] oldNatures = desc.getNatureIds();
				String[] newNatures = new String[oldNatures.length - 1];
				int newIndex = oldNatures.length - 2;
				for (int i = oldNatures.length - 1; i >= 0; i--) {
					if (!oldNatures[i].equals(nature)) {
						newNatures[newIndex--] = oldNatures[i];
					}
				}
				desc.setNatureIds(newNatures);
				project.setDescription(desc, monitor);
			}
		}
	}

	/**
	 * Removes given builder from specified project.
	 */
	public static void removeProjectBuilder(IProject project, String builder,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && builder != null) {
			IProjectDescription desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();
			for (int i = commands.length - 1; i >= 0; i--) {
				if (commands[i].getBuilderName().equals(builder)) {
					ICommand[] newCommands = new ICommand[commands.length - 1];
					System.arraycopy(commands, 0, newCommands, 0, i);
					System.arraycopy(commands, i + 1, newCommands, i,
							commands.length - i - 1);
					// Commit the spec change into the project
					desc.setBuildSpec(newCommands);
					project.setDescription(desc, monitor);
					break;
				}
			}
		}
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

	/**
	 * Returns true if given resource's project is a Spring project.
	 */
	public static boolean isSpringProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			IProject project = resource.getProject();
			if (project != null) {
				try {
					return project.hasNature(SpringCore.NATURE_ID);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		return false;
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
	 * Returns true if Eclipse's runtime bundle has the same or a newer than
	 * given version.
	 */
	public static boolean isEclipseSameOrNewer(int majorVersion,
			int minorVersion) {
		Bundle bundle = Platform.getBundle(Platform.PI_RUNTIME);
		if (bundle != null) {
			String version = (String) bundle.getHeaders().get(
					org.osgi.framework.Constants.BUNDLE_VERSION);
			StringTokenizer st = new StringTokenizer(version, ".");
			try {
				int major = Integer.parseInt(st.nextToken());
				if (major > majorVersion) {
					return true;
				}
				if (major == majorVersion) {
					int minor = Integer.parseInt(st.nextToken());
					if (minor >= minorVersion) {
						return true;
					}
				}
			}
			catch (NoSuchElementException e) {
				// ignore
			}
			catch (NumberFormatException e) {
				// ignore
			}
		}
		return false;
	}

	public static ClassLoader getClassLoader(IResource resource) {
		if (isJavaProject(resource)) {
			return getClassLoader(JavaCore.create(resource.getProject()));
		}
		return Thread.currentThread().getContextClassLoader();
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

	public static ClassLoader getClassLoader(IJavaProject project) {
		return getClassLoader(project, true);
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
			IPath location = getProjectLocation(project.getProject());
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

	public static IPath getProjectLocation(IProject project) {
		return (project.getRawLocation() != null ? project.getRawLocation()
				: project.getLocation());
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

	public static String getClassVersion(Class clazz) {
		String version = "unkown";
		if (clazz.getPackage().getImplementationVersion() != null) {
			version = clazz.getPackage().getImplementationVersion();
		}
		return version;
	}

	public static String getClassLocation(Class clazz) {
		Assert.notNull(clazz);
		String resourceName = ClassUtils.getClassFileName(clazz);
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
	
	public static boolean isTypeAjdtElement(IType type) {
		if (IS_AJDT_PRESENT) {
			return SpringCoreAjdtUtils.isTypeAjdtElement(type);
		}
		return false;
	}

	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = SpringCoreUtils.getJavaProject(project);
		if (IS_AJDT_PRESENT && javaProject != null && className != null) {

			try {
				IType type = null;
				// First look for the type in the project
				if (isAjdtProject(project)) {
					type = SpringCoreAjdtUtils.getAjdtType(project, className);
				}

				if (type != null) {
					return type;
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					if (isAjdtProject(refProject)) {
						type = SpringCoreAjdtUtils.getAjdtType(project,
								className);
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
}
