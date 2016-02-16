/*******************************************************************************
 * Copyright (c) 2009, 2015 Spring IDE Developers
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.xbean.classloader.NonLockingJarFileClassLoader;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Internal cache of classpath urls and corresponding classloaders.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
@SuppressWarnings("deprecation")
public class ProjectClassLoaderCache {

	private static final String FILE_SCHEME = "file";
	private static final int CACHE_SIZE = 12;
	private static final List<ClassLoaderCacheEntry> CLASSLOADER_CACHE = new ArrayList<ClassLoaderCacheEntry>(CACHE_SIZE);

	private static final String DEBUG_OPTION = SpringCore.PLUGIN_ID + "/java/classloader/debug";
	private static final boolean DEBUG_CLASSLOADER = SpringCore.isDebug(DEBUG_OPTION);

	private static ClassLoader cachedParentClassLoader = null;
	private static IPropertyChangeListener propertyChangeListener = null;
	private static IResourceChangeListener resourceChangeListener = null;

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

		// add project to local cache to prevent adding its classpaths multiple times
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
		// If we keep the following check, non-existing output folders will never be used for the lifetime of
		// a classloader. this causes issues with clean projects with not-yet existing output folders.
		// if (file.exists()) {
		if (file.isDirectory()) {
			paths.add(new URL(uri.toString() + File.separator));
		}
		else {
			paths.add(uri.toURL());
		}
		// }
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
				if (curr == null || !curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
					removeClassLoaderEntryFromCache(entry);
					if (DEBUG_CLASSLOADER) {
						System.out.println(String.format("> removing classloader for '%s' : total %s",
								entry.getProject(), CLASSLOADER_CACHE.size()));
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
	public static List<URL> getClassPathUrls(IProject project, ClassLoader parentClassLoader) {

		// needs to be linked to preserve ordering
		List<URL> paths = new ArrayList<URL>();
		Set<IProject> resolvedProjects = new HashSet<IProject>();
		addClassPathUrls(project, paths, resolvedProjects);

		return paths;
	}

	/**
	 * Registers internal listeners that listen to changes relevant to clear out stale cache entries.
	 */
	private static void registerListenersIfRequired() {
		if (propertyChangeListener == null) {
			propertyChangeListener = new EnablementPropertyChangeListener();
			SpringCore.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		}
		if (resourceChangeListener == null) {
			resourceChangeListener = new SourceAndOutputLocationResourceChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		}
	}

	/**
	 * Removes the given {@link ClassLoaderCacheEntry} from the internal cache.
	 * @param entry the entry to remove
	 */
	private static void removeClassLoaderEntryFromCache(ClassLoaderCacheEntry entry) {
		synchronized (CLASSLOADER_CACHE) {
			if (DEBUG_CLASSLOADER) {
				System.out.println(String.format("> removing classloader for '%s' : total %s", entry.getProject()
						.getName(), CLASSLOADER_CACHE.size()));
			}
			entry.dispose();
			CLASSLOADER_CACHE.remove(entry);
		}
	}

	public static boolean shouldFilter(String name) {
		if ("commons-logging.properties".equals(name)) return true;
		if (name != null && name.startsWith("META-INF/services/")) {
			return (name.indexOf('/', 18) == -1
					&& !name.startsWith("org.springframework", 18));
		}
		return false;
	}

	private static boolean useNonLockingClassLoader() {
		boolean useNonLockingClassloaderPreference = SpringCore.getDefault().getPluginPreferences().getBoolean(SpringCore.USE_NON_LOCKING_CLASSLOADER);
		if (useNonLockingClassloaderPreference) {
			NonLockingJarFileClassLoader.setCheckForUpdates(false);
		}

		return useNonLockingClassloaderPreference;
	}

	/**
	 * Returns a {@link ClassLoader} for the given project.
	 */
	protected static ClassLoader getClassLoader(IProject project, ClassLoader parentClassLoader) {
		synchronized (ProjectClassLoaderCache.class) {
			// Setup the root class loader to be used when no explicit parent class loader is given
			if (parentClassLoader == null && cachedParentClassLoader == null) {
				List<URL> paths = new ArrayList<URL>();
				Enumeration<String> libs = SpringCore.getDefault().getBundle().getEntryPaths("/lib/");
				while (libs.hasMoreElements()) {
					String lib = libs.nextElement();
					// Don't add the non locking classloader jar
					if (!lib.contains("xbean-nonlocking-classloader")) {
						paths.add(SpringCore.getDefault().getBundle().getEntry(lib));
					}
				}
				paths.addAll(JdtUtils.getBundleClassPath("org.aspectj.runtime"));
				paths.addAll(JdtUtils.getBundleClassPath("org.aspectj.weaver"));
				paths.addAll(JdtUtils.getBundleClassPath("org.objectweb.asm"));
				paths.addAll(JdtUtils.getBundleClassPath("org.aopalliance"));
				cachedParentClassLoader = new URLClassLoader(paths.toArray(new URL[paths.size()]));
			}

			if (project == null) {
				return cachedParentClassLoader;
			}

			registerListenersIfRequired();
		}

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
	 * Removes any cached {@link ClassLoaderCacheEntry} for the given {@link IProject}.
	 * @param project the project to remove {@link ClassLoaderCacheEntry} for
	 */
	protected static void removeClassLoaderEntryFromCache(IProject project) {
		synchronized (CLASSLOADER_CACHE) {
			if (DEBUG_CLASSLOADER) {
				System.out.println(String.format("> removing classloader for '%s' : total %s", project.getName(),
						CLASSLOADER_CACHE.size()));
			}
			for (ClassLoaderCacheEntry entry : new ArrayList<ClassLoaderCacheEntry>(CLASSLOADER_CACHE)) {
				if (project.equals(entry.getProject())) {
					entry.dispose();
					CLASSLOADER_CACHE.remove(entry);
				}
			}
		}
	}
	
	/**
	 * Internal cache entry
	 */
	static class ClassLoaderCacheEntry implements IElementChangedListener {

		private URL[] directories;

		private ClassLoader jarClassLoader;

		private long lastAccess;

		private ClassLoader parentClassLoader;

		private IProject project;

		private URL[] urls;

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
							removeClassLoaderEntryFromCache(this);
						}
					}
				}
			}
		}

		public ClassLoader getClassLoader() {
			ClassLoader parent = getJarClassLoader();
			if (useNonLockingClassLoader()) {
				return new FilteringNonLockingJarFileClassLoader(String.format("ClassLoader for '%s'", project.getName()),
						directories, parent);
			}
			else {
				return new FilteringURLClassLoader(directories, parent);
			}
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
					if (useNonLockingClassLoader()) {
						jarClassLoader = new FilteringNonLockingJarFileClassLoader(String.format("ClassLoader for '%s'",
								project.getName()), (URL[]) jars.toArray(new URL[jars.size()]), parentClassLoader);
					}
					else {
						jarClassLoader = new FilteringURLClassLoader((URL[]) jars.toArray(new URL[jars.size()]),
								parentClassLoader);
					}
				}
				else {
					if (useNonLockingClassLoader()) {
						jarClassLoader = new FilteringNonLockingJarFileClassLoader(String.format("ClassLoader for '%s'",
								project.getName()), (URL[]) jars.toArray(new URL[jars.size()]), cachedParentClassLoader);
					}
					else {
						jarClassLoader = new FilteringURLClassLoader((URL[]) jars.toArray(new URL[jars.size()]),
								cachedParentClassLoader);
					}
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
	
	/**
	 * {@link IPropertyChangeListener} to clear the cache whenever the setting is changed.
	 * @since 2.5.0
	 */
	static class EnablementPropertyChangeListener implements IPropertyChangeListener {

		/**
		 * {@inheritDoc}
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (SpringCore.USE_NON_LOCKING_CLASSLOADER.equals(event.getProperty())) {
				synchronized (CLASSLOADER_CACHE) {
					CLASSLOADER_CACHE.clear();
				}
			}
		}
	}
	
	/**
	 * {@link IResourceChangeListener} to clear the cache whenever new source or output folders are being added.
	 * @since 2.5.2
	 */
	static class SourceAndOutputLocationResourceChangeListener implements IResourceChangeListener {

		private static final int VISITOR_FLAGS = IResourceDelta.ADDED | IResourceDelta.CHANGED;

		/**
		 * {@inheritDoc}
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getSource() instanceof IWorkspace) {
				int eventType = event.getType();
				switch (eventType) {
				case IResourceChangeEvent.POST_CHANGE:
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						try {
							delta.accept(getVisitor(), VISITOR_FLAGS);
						}
						catch (CoreException e) {
							SpringCore.log("Error while traversing resource change delta", e);
						}
					}
					break;
				}
			}
			else if (event.getSource() instanceof IProject) {
				int eventType = event.getType();
				switch (eventType) {
				case IResourceChangeEvent.POST_CHANGE:
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						try {
							delta.accept(getVisitor(), VISITOR_FLAGS);
						}
						catch (CoreException e) {
							SpringCore.log("Error while traversing resource change delta", e);
						}
					}
					break;
				}
			}

		}

		protected IResourceDeltaVisitor getVisitor() {
			return new SourceAndOutputLocationResourceVisitor();
		}

		/**
		 * Internal resource delta visitor.
		 */
		protected class SourceAndOutputLocationResourceVisitor implements IResourceDeltaVisitor {

			public final boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					return resourceAdded(resource);
				}
				return true;
			}

			protected boolean resourceAdded(IResource resource) {
				if (resource instanceof IFolder && JdtUtils.isJavaProject(resource)) {
					try {
						IJavaProject javaProject = JdtUtils.getJavaProject(resource);
						// Safe guard once again
						if (javaProject == null) {
							return false;
						}

						// Check the default output location
						if (javaProject.getOutputLocation() != null
								&& javaProject.getOutputLocation().equals(resource.getFullPath())) {
							removeClassLoaderEntryFromCache(resource.getProject());
							return false;
						}

						// Check any source and output folder location
						for (IClasspathEntry entry : javaProject.getRawClasspath()) {
							if (resource.getFullPath() != null && resource.getFullPath().equals(entry.getPath())) {
								removeClassLoaderEntryFromCache(resource.getProject());
								return false;
							}
							else if (resource.getFullPath() != null
									&& resource.getFullPath().equals(entry.getOutputLocation())) {
								removeClassLoaderEntryFromCache(resource.getProject());
								return false;
							}
						}
					}
					catch (JavaModelException e) {
						SpringCore.log("Error traversing resource change delta", e);
					}
				}
				return true;
			}
		}
	}

}
