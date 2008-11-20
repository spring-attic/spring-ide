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
package org.springframework.ide.eclipse.core.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class that provides methods to resolve location patterns within the Eclipse file system.
 * @author Christian Dupuis
 * @since 2.0.3
 * @see #getAllResources(String, IProject)
 * @see #getResource(String, IProject)
 */
@SuppressWarnings("restriction")
abstract class ResourceUtils {

	private static final Path JVM_CLASSPATH_CONTAINER = new Path(
			"org.eclipse.jdt.launching.JRE_CONTAINER");

	public static interface RelevantLocationAwareResourceHolder {

		String getRelevantPath();

		Resource getResource();
	}

	public static class ResourceHolder implements RelevantLocationAwareResourceHolder {

		private final String parentPath;

		private final IResource resource;

		public ResourceHolder(String parentPath, IResource resource) {
			this.parentPath = parentPath;
			this.resource = resource;
		}

		public String getRelevantPath() {
			String resourcePath = resource.getRawLocation().toString();
			if (parentPath.length() <= resourcePath.length()) {
				String relevantPath = resourcePath.substring(parentPath.length());
				if (relevantPath.length() > 1 && relevantPath.charAt(0) == '/') {
					return relevantPath.substring(1);
				}
				else {
					return relevantPath;
				}
			}
			return resourcePath;
		}

		public Resource getResource() {
			if (resource instanceof IFolder) {
				return new EclipseResource(resource);
			}
			else if (resource instanceof IFile) {
				return new FileResource((IFile) resource);
			}
			else {
				return null;
			}

		}

	}

	public static class StorageHolder implements RelevantLocationAwareResourceHolder {

		private final ZipEntryStorage resource;

		public StorageHolder(ZipEntryStorage resource) {
			this.resource = resource;
		}

		public String getRelevantPath() {
			String relevantPath = resource.getEntryName();
			if (relevantPath.length() > 1 && relevantPath.charAt(0) == '/') {
				return relevantPath.substring(1);
			}
			else {
				return relevantPath;
			}
		}

		public Resource getResource() {
			return new StorageResource(resource);
		}

	}

	public static final Object[] NO_CHILDREN = new Object[0];

	private static Object[] concatenate(Object[] a1, Object[] a2) {
		int a1Len = a1.length;
		int a2Len = a2.length;
		Object[] res = new Object[a1Len + a2Len];
		System.arraycopy(a1, 0, res, 0, a1Len);
		System.arraycopy(a2, 0, res, a1Len, a2Len);
		return res;
	}

	private static boolean exists(Object element) {
		if (element == null) {
			return false;
		}
		if (element instanceof IResource) {
			return ((IResource) element).exists();
		}
		if (element instanceof IJavaElement) {
			return ((IJavaElement) element).exists();
		}
		return true;
	}

	public static Set<Resource> getAllResources(String path, IProject project) {
		Set<IProject> projects = new LinkedHashSet<IProject>(3);
		Set<Resource> result = new LinkedHashSet<Resource>(6);

		getResource(path, project, result, projects, true);
		return result;
	}

	private static Object[] getChildren(Object element) {
		if (!exists(element)) {
			return NO_CHILDREN;
		}
		try {
			if (element instanceof IProject) {
				IProject project = (IProject) element;
				if (JdtUtils.isJavaProject(project)) {
					element = JavaCore.create(project);
				}
			}
			if (element instanceof IJavaProject) {
				return getPackageFragmentRoots((IJavaProject) element);
			}
			if (element instanceof IProject) {
				return getResources((IProject) element);
			}
			if (element instanceof IPackageFragmentRoot) {
				return getPackageFragments((IPackageFragmentRoot) element);
			}
			if (element instanceof IPackageFragment) {
				return getPackageContents((IPackageFragment) element);
			}
			if (element instanceof IFolder) {
				return getResources((IFolder) element);
			}
		}
		catch (JavaModelException e) {
			return NO_CHILDREN;
		}
		return NO_CHILDREN;
	}

	private static Object[] getNonJavaResources(IPackageFragment fragment)
			throws JavaModelException {
		Object[] nonJavaResources = fragment.getNonJavaResources();
		IPackageFragmentRoot root = (IPackageFragmentRoot) fragment
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (root != null && root.getKind() == IPackageFragmentRoot.K_BINARY) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IFile) {
					nonJavaResources[i] = resource;
				}
				else if (resource instanceof IStorage && root.getResource() instanceof IFile) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage((IFile) root.getResource(), storage
							.getFullPath().toString());
				}
			}
		}
		return nonJavaResources;
	}

	private static Object[] getNonJavaResources(IPackageFragmentRoot root)
			throws JavaModelException {
		Object[] nonJavaResources = root.getNonJavaResources();

		// Replace JAR entries with our own wrapper
		if (root.getKind() == IPackageFragmentRoot.K_BINARY && root.getResource() instanceof IFile) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IStorage) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage((IFile) root.getResource(), storage
							.getFullPath().toString());
				}
			}
		}
		return nonJavaResources;
	}

	private static Object[] getPackageContents(IPackageFragment fragment) throws JavaModelException {
		return getNonJavaResources(fragment);
	}

	private static Object[] getPackageFragmentRoots(IJavaProject project) throws JavaModelException {
		if (!project.getProject().isOpen()) {
			return NO_CHILDREN;
		}

		// Filter out JARs not contained in the project itself and replace
		// package fragment roots that correspond to projects with the package
		// fragments directly
		List<IJavaElement> list = new ArrayList<IJavaElement>();
		for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
			// if (isInternalLibrary(project, root)) {
			if (isProjectPackageFragmentRoot(root)) {
				for (IJavaElement element : root.getChildren()) {
					list.add(element);
				}
			}
			else if (hasChildren(root)) {
				list.add(root);
			}
			// }
		}
		return concatenate(list.toArray(), project.getNonJavaResources());
	}

	private static Object[] getPackageFragments(IPackageFragmentRoot root)
			throws JavaModelException {
		Object[] fragments = root.getChildren();
		Object[] nonJavaResources = getNonJavaResources(root);
		if (nonJavaResources == null) {
			return fragments;
		}
		return concatenate(fragments, nonJavaResources);
	}

	public static Resource getResource(String path, IProject project) {
		Set<IProject> projects = new LinkedHashSet<IProject>(3);
		Set<Resource> result = new LinkedHashSet<Resource>(6);

		getResource(path, project, result, projects, false);
		Iterator<Resource> iterator = result.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		else {
			return null;
		}
	}

	private static Set<Resource> getResource(String path, IProject project,
			Set<Resource> resources, Set<IProject> projects, boolean lookForAll) {

		if (projects.contains(project)) {
			return resources;
		}
		else {
			projects.add(project);
		}

		try {
			getResourceForProject(path, project, resources);
			if (resources.size() > 0 && !lookForAll) {
				return resources;
			}
			if (JdtUtils.isJavaProject(project)) {
				for (IJavaProject jp : JdtUtils.getAllDependingJavaProjects(JdtUtils
						.getJavaProject(project))) {
					getResource(path, jp.getProject(), resources, projects, lookForAll);
					if (resources.size() > 0 && !lookForAll) {
						return resources;
					}
				}
			}
			else {
				for (IProject p : project.getReferencingProjects()) {
					getResource(path, p, resources, projects, lookForAll);
					if (resources.size() > 0 && !lookForAll) {
						return resources;
					}
				}
			}
		}
		catch (JavaModelException e) {
			// Don't do anything
		}
		return resources;
	}

	private static void getResourceForProject(String path, IProject project, Set<Resource> resources)
			throws JavaModelException {
		List<RelevantLocationAwareResourceHolder> holders = new ArrayList<RelevantLocationAwareResourceHolder>();

		for (Object child : ResourceUtils.getChildren(project)) {
			if (child instanceof IPackageFragmentRoot
					&& ((IPackageFragmentRoot) child).getResource() != null) {
				String parentPath = ((IPackageFragmentRoot) child).getResource().getRawLocation()
						.toString();
				Object[] packages = ResourceUtils.getChildren(child);
				boolean isJar = ((IPackageFragmentRoot) child).getKind() == IPackageFragmentRoot.K_BINARY;
				for (Object p : packages) {
					if (p instanceof IPackageFragment) {

						IResource resource = ((IPackageFragment) p).getResource();
						if (!isJar) {
							holders.add(new ResourceHolder(parentPath, resource));
						}
						else {
							holders.add(new ResourceHolder(parentPath, new StorageFolder(resource,
									(IPackageFragment) p)));
						}

						Object[] packageChildren = ResourceUtils.getChildren(p);
						for (Object packageChild : packageChildren) {
							if (packageChild instanceof IResource) {
								holders.add(new ResourceHolder(parentPath, (IResource) packageChild));
							}
							else if (packageChild instanceof ZipEntryStorage) {
								holders.add(new StorageHolder((ZipEntryStorage) packageChild));
							}
						}

					}
					else if (p instanceof IFolder) {
						deepAddChildren((IFolder) p, holders, parentPath);
					}
					else if (p instanceof IResource) {
						holders.add(new ResourceHolder(parentPath, (IResource) p));
					}
				}
			}
			else if (child instanceof JarPackageFragmentRoot) {
				addJarEntryResources(project, holders, child);
			}
			else if (child instanceof IResource) {
				String parentPath = ((IResource) child).getProject().getLocation().toString();
				holders.add(new ResourceHolder(parentPath, (IResource) child));
			}
		}

		for (RelevantLocationAwareResourceHolder holder : holders) {
			String holderPath = holder.getRelevantPath();
			if (path.endsWith("/")) {
				holderPath = holderPath.concat("/");
			}
			if (holderPath.equals(path)) {
				resources.add(holder.getResource());
			}
		}
	}

	private static void addJarEntryResources(IProject project,
			List<RelevantLocationAwareResourceHolder> holders, Object child)
			throws JavaModelException {

		IPath classpathEntry = ((JarPackageFragmentRoot) child).getRawClasspathEntry().getPath();

		if (!JVM_CLASSPATH_CONTAINER.equals(classpathEntry)) {
			IPath jarPath = ((JarPackageFragmentRoot) child).getPath();
			try {
				File file = jarPath.toFile();
				JarFile jarFile = new JarFile(file);
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry jarEntry = entries.nextElement();
					String entryPath = jarEntry.getName();
					if (entryPath.charAt(0) == '/' && entryPath.length() > 1) {
						entryPath = entryPath.substring(1);
					}
					
					if (!jarEntry.isDirectory()) {
						holders.add(new ResourceHolder(jarPath.toString()
								+ ZipEntryStorage.DELIMITER, new ExternalFile(file, '/' + jarEntry
								.getName(), project)));
					}
				}
			}
			catch (IOException e) {
			}
		}

	}

	private static void deepAddChildren(IFolder obj,
			List<RelevantLocationAwareResourceHolder> holders, String parentPath) {
		holders.add(new ResourceHolder(parentPath, obj));
		Object[] children = ResourceUtils.getChildren(obj);
		for (Object child : children) {
			if (child instanceof IFolder) {
				deepAddChildren((IFolder) child, holders, parentPath);
			}
			else if (child instanceof IResource) {
				holders.add(new ResourceHolder(parentPath, (IResource) child));
			}
		}
	}

	private static Object[] getResources(IContainer container) {
		try {
			IResource[] members = container.members();
			IJavaProject javaProject = JavaCore.create(container.getProject());
			if (javaProject == null || !javaProject.exists()) {
				return members;
			}
			boolean isFolderOnClasspath = javaProject.isOnClasspath(container);
			List<IResource> nonJavaResources = new ArrayList<IResource>();

			// Can be on classpath but as a member of non-java resource folder
			for (IResource member : members) {

				// A resource can also be a java element in the case of
				// exclusion and inclusion filters. We therefore exclude Java
				// elements from the list of non-Java resources.
				if (isFolderOnClasspath) {
					if (javaProject.findPackageFragmentRoot(member.getFullPath()) == null) {
						nonJavaResources.add(member);
					}
				}
				else if (!javaProject.isOnClasspath(member)) {
					nonJavaResources.add(member);
				}
			}
			return nonJavaResources.toArray();
		}
		catch (CoreException e) {
			return NO_CHILDREN;
		}
	}

	private static boolean hasChildren(Object element) {
		if (element instanceof IProject) {
			IProject project = (IProject) element;
			if (!project.isOpen()) {
				return false;
			}
		}
		if (element instanceof IJavaProject) {
			IJavaProject javaProject = (IJavaProject) element;
			if (!javaProject.getProject().isOpen()) {
				return false;
			}
		}
		if (element instanceof IParent) {
			try {
				// when we have Java children return true, else we fetch all
				// the children
				if (((IParent) element).hasChildren()) {
					return true;
				}
			}
			catch (JavaModelException e) {
				return true;
			}
		}
		Object[] children = getChildren(element);
		return (children != null) && children.length > 0;
	}

	private static boolean isProjectPackageFragmentRoot(IPackageFragmentRoot root) {
		IResource resource = root.getResource();
		return (resource instanceof IProject);
	}

}
