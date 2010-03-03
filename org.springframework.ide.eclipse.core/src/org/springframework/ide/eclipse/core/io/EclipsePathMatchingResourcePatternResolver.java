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
package org.springframework.ide.eclipse.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.ClassUtils;

/**
 * Eclipse specific {@link ResourceLoader} implementation that understands the same rules applied by
 * {@link PathMatchingResourcePatternResolver} of Spring.
 * <p>
 * See the later for a comprehensive description of the supported patterns and semantics.
 * @author Christian Dupuis
 * @since 2.0.3
 * @see PathMatchingResourcePatternResolver
 */
public class EclipsePathMatchingResourcePatternResolver implements ResourcePatternResolver {

	private final PathMatchingResourcePatternResolver patternResolver;

	private final IProject project;

	private final ResourceLoader resourceLoader;

	private final Map<String, Resource[]> resolvedResources = new ConcurrentHashMap<String, Resource[]>();

	public EclipsePathMatchingResourcePatternResolver(IProject project) {
		this(project, JdtUtils.getClassLoader(project, null));
	}

	public EclipsePathMatchingResourcePatternResolver(IProject project, ClassLoader classLoader) {
		this.resourceLoader = new EclipseFileResourceLoader(this, classLoader);
		this.project = project;
		this.patternResolver = new PathMatchingResourcePatternResolver(classLoader);
	}

	/**
	 * Return the ClassLoader that this pattern resolver works with (never <code>null</code>).
	 */
	public ClassLoader getClassLoader() {
		return getResourceLoader().getClassLoader();
	}

	public Resource getResource(String location) {
		// long start = System.currentTimeMillis();
		try {

			// Check cache first
			if (resolvedResources.containsKey(location)) {
				return resolvedResources.get(location)[0];
			}

			Resource resource = patternResolver.getResource(location);
			if (resource != null) {
				try {
					IJavaProject javaProject = JdtUtils.getJavaProject(project);
					IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
					Resource newResource = processRawResource(roots, resource);
					if (newResource != null) {
						resource = newResource;
					}
					else {
						System.out.println(String.format("!-- could not resolve '%s'", resource));
					}
				}
				catch (JavaModelException e) {
				}
				catch (IOException e) {
				}
			}

			resolvedResources.put(location, new Resource[] { resource });
			return resource;
		}
		finally {
			// System.out.println(String.format("--- resolving location '%s' took '%s'ms", location, (System
			// .currentTimeMillis() - start)));
		}
	}

	/**
	 * Return the ResourceLoader that this pattern resolver works with.
	 */
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		// long start = System.currentTimeMillis();
		try {

			// Check cache first
			if (resolvedResources.containsKey(locationPattern)) {
				return resolvedResources.get(locationPattern);
			}

			Resource[] resources = patternResolver.getResources(locationPattern);
			Set<Resource> foundResources = new HashSet<Resource>();

			try {
				IJavaProject javaProject = JdtUtils.getJavaProject(project);
				IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();

				for (Resource resource : resources) {
					Resource newResource = processRawResource(roots, resource);
					if (newResource != null) {
						foundResources.add(newResource);
					}
					else {
						System.out.println(String.format("!-- could not resolve '%s'", resource));
					}
				}
			}
			catch (JavaModelException e) {
				// The implementation is called too often to log
			}

			Resource[] result = foundResources.toArray(new Resource[foundResources.size()]);
			resolvedResources.put(locationPattern, result);

			return result;
		}
		finally {
			// System.out.println(String.format("--- resolving resource pattern '%s' took '%s'ms", locationPattern,
			// (System.currentTimeMillis() - start)));
		}
	}

	/**
	 * Verify if the <code>resources</code> array contains a file matching <code>fileName</code>.
	 */
	private IStorage contains(Object[] resources, String fileName) {
		for (Object resource : resources) {
			if (resource instanceof IResource) {
				if (((IResource) resource).getName().equals(fileName)) {
					return (IStorage) resource;
				}
			}
			else if (resource instanceof IJarEntryResource) {
				if (((IJarEntryResource) resource).getName().equals(fileName)) {
					return (IStorage) resource;
				}
			}
		}
		return null;
	}

	private Resource processClassResource(String path, String fileName, String typeName, IPackageFragmentRoot root,
			IJavaElement[] children) throws JavaModelException {
		for (IJavaElement je : children) {
			if (je instanceof ICompilationUnit) {
				for (IType type : ((ICompilationUnit) je).getAllTypes()) {
					if (type.getFullyQualifiedName('$').equals(typeName)) {
						if (root.getRawClasspathEntry().getEntryKind() == IClasspathEntry.CPE_SOURCE) {

							IPath outputLocation = root.getRawClasspathEntry().getOutputLocation();
							if (outputLocation == null) {
								outputLocation = root.getJavaProject().getOutputLocation();
							}
							IResource classResource = ResourcesPlugin.getWorkspace().getRoot().findMember(
									outputLocation.append(path));
							if (classResource != null) {
								return new FileResource((IFile) classResource);
							}
						}
					}
				}
			}
			else if (je instanceof IClassFile) {
				if (((IClassFile) je).getElementName().equals(fileName)) {
					// Workspace jar or resource
					if (root.getResource() != null) {
						return new StorageResource(new ZipEntryStorage((IFile) root.getResource(), path), project);
					}
					// Workspace external jar
					else {
						File jarFile = root.getPath().toFile();
						return new ExternalFile(jarFile, path, project);
					}
				}
			}
		}
		return null;
	}

	private Resource processRawResource(IPackageFragmentRoot[] roots, Resource resource) throws IOException,
			JavaModelException {
		if (resource instanceof FileSystemResource) {
			// This can only be something in the Eclipse workspace
			IResource[] allResourcesFor = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					resource.getURI());
			for (IResource res : allResourcesFor) {
				return new FileResource((IFile) res);
			}
		}
		else if (resource instanceof UrlResource) {
			URL url = resource.getURL();
			String path = url.getPath();
			int ix = path.indexOf('!');
			if (ix > 0) {
				String entryName = path.substring(ix + 1);
				path = path.substring(0, ix);

				try {
					return new ExternalFile(new File(new URI(path)), entryName, project);
				}
				catch (URISyntaxException e) {
				}
			}
			else {
				IResource[] allResourcesFor = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
						resource.getURI());
				for (IResource res : allResourcesFor) {
					return new FileResource((IFile) res);
				}
			}
		}
		else if (resource instanceof ClassPathResource) {
			String path = ((ClassPathResource) resource).getPath();
			String fileName = path;
			String packageName = "";
			int ix = path.lastIndexOf('/');
			if (ix > 0) {
				fileName = path.substring(ix + 1);
				packageName = path.substring(0, ix).replace('/', '.');
			}
			if (fileName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				String typeName = packageName + "." + fileName.substring(0, fileName.length() - 6);
				for (IPackageFragmentRoot root : roots) {
					Resource storage = null;

					if ("".equals(packageName) && root.exists()) {
						storage = processClassResource(path, fileName, typeName, root, root.getChildren());
					}

					IPackageFragment packageFragment = root.getPackageFragment(packageName);
					if (storage == null && packageFragment != null && packageFragment.exists()) {
						storage = processClassResource(path, fileName, typeName, root, packageFragment.getChildren());
					}

					if (storage != null) {
						return storage;
					}
				}
			}

			else {
				for (IPackageFragmentRoot root : roots) {
					IStorage storage = null;

					// Look in the root of the package fragment root
					if ("".equals(packageName) && root.exists()) {
						storage = contains(root.getNonJavaResources(), fileName);
					}

					// Check the package
					IPackageFragment packageFragment = root.getPackageFragment(packageName);
					if (storage == null && packageFragment != null && packageFragment.exists()) {
						storage = contains(packageFragment.getNonJavaResources(), fileName);
					}

					// Found the resource in the package fragment root? -> construct usable Resource
					if (storage != null) {
						if (storage instanceof IFile) {
							return new FileResource((IFile) storage);
						}
						else if (storage instanceof IJarEntryResource) {

							// Workspace jar or resource
							if (root.getResource() != null) {
								return new StorageResource(new ZipEntryStorage((IFile) root.getResource(),
										((IJarEntryResource) storage).getFullPath().toString()), project);
							}
							// Workspace external jar
							else {
								File jarFile = root.getPath().toFile();
								return new ExternalFile(jarFile,
										((IJarEntryResource) storage).getFullPath().toString(), project);
							}
						}
					}
				}
			}
		}
		return null;
	}

}
