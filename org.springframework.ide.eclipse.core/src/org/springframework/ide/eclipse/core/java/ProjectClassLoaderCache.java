/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Internal cache of classpath urls and corresponding classloaders.
 * @author Christian Dupuis
 * @since 2.2.5
 */
class ProjectClassLoaderCache {

	private static final int CACHE_SIZE = 24;

	private static List<ClassLoaderCacheEntry> CLASSLOADER_CACHE = new ArrayList<ClassLoaderCacheEntry>(CACHE_SIZE);

	private static final String DEBUG_OPTION = SpringCore.PLUGIN_ID + "/java/classloader/debug";

	private static boolean DEBUG_CLASSLOADER = SpringCore.isDebug(DEBUG_OPTION);

	private static final String FILE_SCHEME = "file";

	private static ClassLoader addClassLoaderToCache(IProject project, List<URL> urls, ClassLoader parentClassLoader) {
		synchronized (CLASSLOADER_CACHE) {
			int nEntries = CLASSLOADER_CACHE.size();
			if (nEntries >= CACHE_SIZE) {
				// find obsolete entries or remove entry that was least recently accessed
				ClassLoaderCacheEntry oldest = null;
				List<ClassLoaderCacheEntry> obsoleteClassLoaders = new ArrayList<ClassLoaderCacheEntry>(CACHE_SIZE);
				for (int i = 0; i < nEntries; i++) {
					ClassLoaderCacheEntry entry = (ClassLoaderCacheEntry) CLASSLOADER_CACHE.get(i);
					IProject curr = entry.getProject();
					if (!curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
						obsoleteClassLoaders.add(entry);
					}
					else {
						if (oldest == null || entry.getLastAccess() < oldest.getLastAccess()) {
							oldest = entry;
						}
					}
				}
				if (!obsoleteClassLoaders.isEmpty()) {
					for (int i = 0; i < obsoleteClassLoaders.size(); i++) {
						removeClassLoaderEntryFromCache((ClassLoaderCacheEntry) obsoleteClassLoaders.get(i));
					}
				}
				else if (oldest != null) {
					removeClassLoaderEntryFromCache(oldest);
				}
			}
			ClassLoaderCacheEntry newEntry = new ClassLoaderCacheEntry(project, urls, parentClassLoader);
			CLASSLOADER_CACHE.add(newEntry);
			return newEntry.getClassLoader();
		}
	}

	/**
	 * Add {@link URL}s to the given set of <code>paths</code>.
	 */
	private static void addClassPathUrls(IProject project, List<URL> paths, Set<IProject> resolvedProjects) {

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
			if (JdtUtils.isJavaProject(project)) {
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
							paths.add(file.toURI().toURL());
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
				for (IJavaProject p : JdtUtils.getAllDependingJavaProjects(jp)) {
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

	private static void addUri(List<URL> paths, URI uri) throws MalformedURLException {
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

	private static void covertPathToUrl(IProject project, List<URL> paths, IPath path) throws MalformedURLException {
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
					IPath sourceControlPath = project.findMember(path.removeFirstSegments(1)).getLocation();
					File sourceControlFile = sourceControlPath.toFile();
					if (sourceControlFile.exists()) {
						addUri(paths, sourceControlFile.toURI());
					}
				}
				else {
					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
					addUri(paths, variableManager.resolveURI(uri));
				}
			}
		}
	}

	private static ClassLoader findClassLoaderInCache(IProject project, ClassLoader parentClassLoader) {
		synchronized (CLASSLOADER_CACHE) {
			for (int i = CLASSLOADER_CACHE.size() - 1; i >= 0; i--) {
				ClassLoaderCacheEntry entry = (ClassLoaderCacheEntry) CLASSLOADER_CACHE.get(i);
				IProject curr = entry.getProject();
				if (!curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
					removeClassLoaderEntryFromCache(entry);
					if (DEBUG_CLASSLOADER) {
						System.out.println(String.format("> removing classloader for '%s' : total %s", entry
								.getProject(), CLASSLOADER_CACHE.size()));
					}
				}
				else {
					if (entry.matches(project, parentClassLoader)) {
						entry.markAsAccessed();
						return entry.getClassLoader();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Iterates all class path entries of the given <code>project</code> and all depending projects.
	 * <p>
	 * Note: if <code>useParentClassLoader</code> is true, the Spring, AspectJ, Commons Logging and ASM bundles are
	 * automatically added to the paths.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader use the OSGi classloader as parent
	 * @return a set of {@link URL}s that can be used to construct a {@link URLClassLoader}
	 */
	private static List<URL> getClassPathUrls(IProject project, ClassLoader parentClassLoader) {

		// needs to be linked to preserve ordering
		List<URL> paths = new ArrayList<URL>();
		if (parentClassLoader == null) {
			// add required libraries from osgi bundles
			paths.addAll(JdtUtils.getBundleClassPath("org.springframework.core"));
			paths.addAll(JdtUtils.getBundleClassPath("org.springframework.beans"));
			paths.addAll(JdtUtils.getBundleClassPath("org.springframework.context"));
			paths.addAll(JdtUtils.getBundleClassPath("org.springframework.aop"));
			paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.aspectj.weaver"));
			paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.apache.commons.logging"));
			paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.objectweb.asm"));
			paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.aopalliance"));
		}

		Set<IProject> resolvedProjects = new HashSet<IProject>();
		addClassPathUrls(project, paths, resolvedProjects);

		if (parentClassLoader == null) {
			// search for slf4j and remove it; evil classloading issues if it ends up on the classpath and confuses
			// Spring classes
			for (URL path : new HashSet<URL>(paths)) {
				if (path.getFile() != null
						&& (path.getFile().contains("com.springsource.slf4j.org.apache.commons.logging") || path
								.getFile().contains("jcl-over-slf4j"))) {
					paths.remove(path);
				}
			}
		}

		return paths;
	}

	private static void removeClassLoaderEntryFromCache(ClassLoaderCacheEntry entry) {
		synchronized (CLASSLOADER_CACHE) {
			entry.dispose();
			CLASSLOADER_CACHE.remove(entry);
		}
	}

	/**
	 * Returns a {@link ClassLoader} for the given project.
	 */
	protected synchronized static ClassLoader getClassLoader(IProject project, ClassLoader parentClassLoader) {
		ClassLoader classLoader = findClassLoaderInCache(project, parentClassLoader);
		if (classLoader == null) {
			List<URL> urls = getClassPathUrls(project, parentClassLoader);
			classLoader = addClassLoaderToCache(project, urls, parentClassLoader);
			if (DEBUG_CLASSLOADER) {
				System.out.println(String.format("> creating new classloader for '%s' with parent '%s' : total %s",
						project.getName(), parentClassLoader, CLASSLOADER_CACHE.size()));
			}
		}
		return classLoader;
	}

	/**
	 * Internal cache entry
	 */
	private static class ClassLoaderCacheEntry implements IElementChangedListener {

		private URL[] directories;

		private URLClassLoader jarClassLoader;

		private long lastAccess;

		private IProject project;

		private URL[] urls;

		private ClassLoader parentClassLoader;

		public ClassLoaderCacheEntry(IProject project, List<URL> urls, ClassLoader parentClassLoader) {
			this.project = project;
			this.urls = urls.toArray(new URL[urls.size()]);
			this.parentClassLoader = parentClassLoader;
			markAsAccessed();
			JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
		}

		public void dispose() {
			JavaCore.removeElementChangedListener(this);
			this.urls = null;
			this.jarClassLoader = null;
		}

		public void elementChanged(ElementChangedEvent event) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project);
			if (javaProject != null) {
				for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
					if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
							|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
						if (javaProject.equals(delta.getElement()) || javaProject.isOnClasspath(delta.getElement())) {
							if (DEBUG_CLASSLOADER) {
								System.out.println(String.format("> removing classloader for '%s' : total %s", project
										.getName(), CLASSLOADER_CACHE.size()));
							}
							removeClassLoaderEntryFromCache(this);
						}
					}
				}
			}
		}

		public ClassLoader getClassLoader() {
			// if (useParentClassLoader) {
			// return new URLClassLoader(urls, this.getClass().getClassLoader());
			// }
			// else {
			// return new URLClassLoader(urls);
			// }
			ClassLoader parent = getJarClassLoader();
			return new URLClassLoader(directories, parent);
		}

		public long getLastAccess() {
			return lastAccess;
		}

		public IProject getProject() {
			return this.project;
		}

		public void markAsAccessed() {
			lastAccess = System.currentTimeMillis();
		}

		public boolean matches(IProject project, ClassLoader parentClassLoader) {
			return this.project.equals(project)
					&& ((parentClassLoader == null && this.parentClassLoader == null) || (parentClassLoader != null && parentClassLoader
							.equals(this.parentClassLoader)));
		}

		private synchronized ClassLoader getJarClassLoader() {
			if (jarClassLoader == null) {
				Set<URL> jars = new LinkedHashSet<URL>();
				List<URL> dirs = new ArrayList<URL>();
				for (URL url : urls) {
					if (shouldLoadFromParent(url)) {
						jars.add(url);
					}
					else {
						dirs.add(url);
					}
				}
				if (parentClassLoader != null) {
					// We use the parent class loader of the org.springframework.ide.eclipse.beans.core bundle
					jarClassLoader = new URLClassLoader((URL[]) jars.toArray(new URL[jars.size()]), parentClassLoader);
				}
				else {
					jarClassLoader = new URLClassLoader((URL[]) jars.toArray(new URL[jars.size()]));
				}
				directories = dirs.toArray(new URL[dirs.size()]);
			}
			return jarClassLoader;
		}

		private boolean shouldLoadFromParent(URL url) {
			String path = url.getPath();
			if (path.endsWith(".jar") || path.endsWith(".zip")) {
				return true;
			}
			else if (path.contains("/org.eclipse.osgi/bundles/")) {
				return true;
			}
			return false;
		}
	}

}
