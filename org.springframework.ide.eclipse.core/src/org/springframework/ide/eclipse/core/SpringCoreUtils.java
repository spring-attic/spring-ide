/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.springframework.util.StringUtils;

/**
 * Some helper methods.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class SpringCoreUtils {

	/**
	 * Creates specified simple project.
	 */
	public static IProject createProject(String projectName, IProjectDescription description,
			IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists()) {
			if (description == null) {
				project.create(monitor);
			} else {
				project.create(description, monitor);
			}
		} else {
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
	public static IJavaProject createJavaProject(String projectName, IProgressMonitor monitor)
			throws CoreException {
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
	public static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
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
	public static void addProjectNature(IProject project, String nature, IProgressMonitor monitor)
			throws CoreException {
		if (project != null && nature != null) {
			if (!project.hasNature(nature)) {
				IProjectDescription desc = project.getDescription();
				String[] oldNatures = desc.getNatureIds();
				String[] newNatures = new String[oldNatures.length + 1];
				newNatures[0] = nature;
				if (oldNatures.length > 0) {
					System.arraycopy(oldNatures, 0, newNatures, 1, oldNatures.length);
				}
				desc.setNatureIds(newNatures);
				project.setDescription(desc, monitor);
			}
		}
	}

	/**
	 * Removes given nature from specified project.
	 */
	public static void removeProjectNature(IProject project, String nature, IProgressMonitor monitor)
			throws CoreException {
		if (project != null && nature != null) {
			if (project.exists() && project.hasNature(nature)) {

				// first remove all problem markers (including the
				// inherited ones) from Spring beans project
				if (nature.equals(SpringCore.NATURE_ID)) {
					project.deleteMarkers(SpringCore.MARKER_ID, true, IResource.DEPTH_INFINITE);
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
					System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
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
			try {
				return resource.getProject().hasNature(JavaCore.NATURE_ID);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	/**
	 * Returns true if given resource's project is a Spring project.
	 */
	public static boolean isSpringProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			try {
				return resource.getProject().hasNature(SpringCore.NATURE_ID);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	/**
	 * Removes all Spring problem markers (including the inherited ones) from given resource.
	 */
	public static void deleteProblemMarkers(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			try {
				resource.deleteMarkers(SpringCore.MARKER_ID, true, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

	/**
	 * Returns true if Eclipse's runtime bundle has the same or a newer than given version.
	 */
	public static boolean isEclipseSameOrNewer(int majorVersion, int minorVersion) {
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
			} catch (NoSuchElementException e) {
				// ignore
			} catch (NumberFormatException e) {
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

	public static ClassLoader getClassLoader(IJavaProject project, boolean useParentClassLoader) {
		List<URL> paths = getClassPathURLs(project, useParentClassLoader);
		if (useParentClassLoader) {
			return new URLClassLoader(paths.toArray(new URL[paths.size()]), Thread.currentThread()
					.getContextClassLoader());
		} else {
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
				String[] classPathEntries = StringUtils.delimitedListToStringArray(bundleClassPath,
						",");
				for (String classPathEntry : classPathEntries) {
					paths.add(FileLocator.toFileURL(new URL(bundle.getEntry("/"), "/"
							+ classPathEntry.trim())));
				}
			}
		} catch (MalformedURLException e) {
			SpringCore.log(e);
		} catch (IOException e) {
			SpringCore.log(e);
		}
		return paths;
	}

	public static List<URL> getClassPathURLs(IJavaProject project, boolean useParentClassLoader) {
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
			IClasspathEntry classpath[] = project.getRawClasspath();
			// build output, relative to project
			IPath location = getProjectLocation(project.getProject());
			IPath outputPath = location.append(project.getOutputLocation().removeFirstSegments(1));
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry path = classpath[i];
				if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					File file = path.getPath().toFile();
					if (file.exists()) {
						URL url = path.getPath().toFile().toURL();
						paths.add(url);
					} else {
						IPath relPath = path.getPath().removeFirstSegments(1);
						URL url = new URL("file:" + location + File.separator
								+ relPath.toOSString());
						paths.add(url);
					}
				}
			}
			paths.add(outputPath.toFile().toURL());
		} catch (Exception e) {
			// ignore
		}
		return paths;
	}

	public static IPath getProjectLocation(IProject project) {
		return (project.getRawLocation() != null ? project.getRawLocation() : project.getLocation());
	}
}
