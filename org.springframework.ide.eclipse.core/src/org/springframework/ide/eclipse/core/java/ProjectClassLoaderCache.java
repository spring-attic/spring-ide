/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import org.eclipse.core.resources.IResource;
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

	private static final String DEBUG_OPTION = SpringCore.PLUGIN_ID + "/java/classloader/debug";

	private static boolean DEBUG_CLASSLOADER = SpringCore.isDebug(DEBUG_OPTION);

	private static final String FILE_SCHEME = "file";

	private static final int CACHE_SIZE = 24;

	private static List<ClassLoaderCacheEntry> CLASSLOADER_CACHE = new ArrayList<ClassLoaderCacheEntry>(CACHE_SIZE);

	private static ClassLoader addClassLoaderToCache(IProject project, Set<URL> urls, boolean useParentClassLoader) {
		synchronized (CLASSLOADER_CACHE) {
			int nEntries = CLASSLOADER_CACHE.size();
			if (nEntries >= CACHE_SIZE) {
				// find obsolete entries or remove entry that was least recently
				// accessed
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
			ClassLoaderCacheEntry newEntry = new ClassLoaderCacheEntry(project, urls, useParentClassLoader);
			CLASSLOADER_CACHE.add(newEntry);
			return newEntry.getClassLoader();
		}
	}

	private static ClassLoader findClassLoaderInCache(IProject project, boolean useParentClassLoader) {
		synchronized (CLASSLOADER_CACHE) {
			for (int i = CLASSLOADER_CACHE.size() - 1; i >= 0; i--) {
				ClassLoaderCacheEntry entry = (ClassLoaderCacheEntry) CLASSLOADER_CACHE.get(i);
				IProject curr = entry.getProject();
				if (!curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
					removeClassLoaderEntryFromCache(entry);
				}
				else {
					if (entry.matches(project, useParentClassLoader)) {
						entry.markAsAccessed();
						return entry.getClassLoader();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns a {@link ClassLoader} for the given project.
	 */
	protected static ClassLoader getClassLoader(IResource project) {
		return getClassLoader(project.getProject(), true);
	}

	/**
	 * Returns a {@link ClassLoader} for the given project.
	 */
	protected static ClassLoader getClassLoader(IProject project, boolean useParentClassLoader) {
		ClassLoader classLoader = findClassLoaderInCache(project, useParentClassLoader);
		if (classLoader == null) {
			Set<URL> urls = getClassPathUrls(project, useParentClassLoader);
			classLoader = addClassLoaderToCache(project, urls, useParentClassLoader);
		}
		return classLoader;
	}

	private static void removeClassLoaderEntryFromCache(ClassLoaderCacheEntry entry) {
		synchronized (CLASSLOADER_CACHE) {
			entry.dispose();
			CLASSLOADER_CACHE.remove(entry);
		}
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
	private static Set<URL> getClassPathUrls(IProject project, boolean useParentClassLoader) {
		// prepare for tracing
		long start = System.currentTimeMillis();
		try {

			// needs to be linked to preserve ordering
			Set<URL> paths = new LinkedHashSet<URL>();
			if (!useParentClassLoader) {
				// add required libraries from osgi bundles
				paths.addAll(JdtUtils.getBundleClassPath("org.springframework.core"));
				paths.addAll(JdtUtils.getBundleClassPath("org.springframework.beans"));
				paths.addAll(JdtUtils.getBundleClassPath("org.springframework.aop"));
				paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.aspectj.weaver"));
				paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.apache.commons.logging"));
				paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.objectweb.asm"));
				paths.addAll(JdtUtils.getBundleClassPath("com.springsource.org.aopalliance"));
			}

			Set<IProject> resolvedProjects = new HashSet<IProject>();
			addClassPathUrls(project, paths, resolvedProjects);

			if (!useParentClassLoader) {
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
		finally {
			if (DEBUG_CLASSLOADER) {
				System.out.println("getClassLoader for '" + project.getProject().getName() + "' took "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		}
	}

	/**
	 * Add {@link URL}s to the given set of <code>paths</code>.
	 */
	private static void addClassPathUrls(IProject project, Set<URL> paths, Set<IProject> resolvedProjects) {

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

	private static void covertPathToUrl(IProject project, Set<URL> paths, IPath path) throws MalformedURLException {
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
	 * Internal cache entry
	 */
	private static class ClassLoaderCacheEntry implements IElementChangedListener {

		private long lastAccess;

		private IProject project;

		private URL[] urls;

		private boolean useParentClassLoader;

		public ClassLoaderCacheEntry(IProject project, Set<URL> urls, boolean useParentClassLoader) {
			this.project = project;
			this.urls = (URL[]) urls.toArray(new URL[urls.size()]);
			this.useParentClassLoader = useParentClassLoader;
			markAsAccessed();
			JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
		}

		public void dispose() {
			JavaCore.removeElementChangedListener(this);
			this.urls = null;
		}

		public long getLastAccess() {
			return lastAccess;
		}

		public ClassLoader getClassLoader() {
			if (useParentClassLoader) {
				return new URLClassLoader(urls, this.getClass().getClassLoader());
			}
			else {
				return new URLClassLoader(urls);
			}
		}

		public IProject getProject() {
			return this.project;
		}

		public boolean matches(IProject project, boolean useParentClassLoader) {
			return this.project.equals(project)
					&& Boolean.valueOf(this.useParentClassLoader).equals(Boolean.valueOf(useParentClassLoader));
		}

		public void markAsAccessed() {
			lastAccess = System.currentTimeMillis();
		}

		public void elementChanged(ElementChangedEvent event) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project);
			if (javaProject != null) {
				for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
					if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
							|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
						if (javaProject.equals(delta.getElement()) || javaProject.isOnClasspath(delta.getElement())) {
							removeClassLoaderEntryFromCache(this);
						}
					}
				}
			}
		}
	}

}
