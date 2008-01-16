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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * Eclipse specific {@link ResourceLoader} implementation that understands the
 * same rules applied by {@link PathMatchingResourcePatternResolver} of Spring.
 * <p>
 * See the later for a comprehensive description of the supported patterns and
 * semantics.
 * @author Christian Dupuis
 * @since 2.0.3
 * @see PathMatchingResourcePatternResolver
 * @see AntPathMatcher
 */
public class EclipsePathMatchingResourcePatternResolver implements
		ResourcePatternResolver {

	private final ResourceLoader resourceLoader;

	private PathMatcher pathMatcher = new AntPathMatcher();

	private final IProject project;

	public EclipsePathMatchingResourcePatternResolver(IProject project) {
		this.resourceLoader = new EclipseFileResourceLoader(project);
		this.project = project;
	}

	/**
	 * Return the ResourceLoader that this pattern resolver works with.
	 */
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Return the ClassLoader that this pattern resolver works with (never
	 * <code>null</code>).
	 */
	public ClassLoader getClassLoader() {
		return getResourceLoader().getClassLoader();
	}

	/**
	 * Set the PathMatcher implementation to use for this resource pattern
	 * resolver. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Return the PathMatcher that this resource pattern resolver uses.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}
	
	/**
	 * Return a single resource for the given location.
	 */
	public Resource getResource(String location) {
		return getResourceLoader().getResource(location);
	}

	/**
	 * Returns matching resources.
	 */
	public Resource[] getResources(String locationPattern) throws IOException {
		Assert.notNull(locationPattern, "Location pattern must not be null");
		if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
			// a class path resource (multiple resources for same name possible)
			if (getPathMatcher().isPattern(
					locationPattern
							.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
				// a class path resource pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// all class path resources with the given name
				return findAllClassPathResources(locationPattern
						.substring(CLASSPATH_ALL_URL_PREFIX.length()));
			}
		}
		else {
			// Only look for a pattern after a prefix here
			// (to not get fooled by a pattern symbol in a strange prefix).
			int prefixEnd = locationPattern.indexOf(":") + 1;
			if (getPathMatcher()
					.isPattern(locationPattern.substring(prefixEnd))) {
				// a file pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// a single resource with the given name
				return new Resource[] { getResourceLoader().getResource(
						locationPattern) };
			}
		}
	}

	protected Resource[] findAllClassPathResources(String location) {
		String path = location;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		Set<Resource> resources = org.springframework.ide.eclipse.core.io.ResourceUtils
				.getAllResources(path, project);
		return resources.toArray(new Resource[resources.size()]);
	}

	protected Resource[] findPathMatchingResources(String locationPattern)
			throws IOException {
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		Resource[] rootDirResources = getResources(rootDirPath);
		Set<Resource> result = new LinkedHashSet<Resource>(16);
		for (int i = 0; i < rootDirResources.length; i++) {
			Resource rootDirResource = rootDirResources[i];
			result.addAll(doFindPathMatchingFileResources(rootDirResource,
					subPattern));
		}
		return result.toArray(new Resource[result.size()]);
	}

	protected String determineRootDir(String location) {
		int prefixEnd = location.indexOf(":") + 1;
		int rootDirEnd = location.length();
		while (rootDirEnd > prefixEnd
				&& getPathMatcher().isPattern(
						location.substring(prefixEnd, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		if (rootDirEnd == 0) {
			rootDirEnd = prefixEnd;
		}
		return location.substring(0, rootDirEnd);
	}

	protected Set<Resource> doFindPathMatchingFileResources(
			Resource rootDirResource, String subPattern) throws IOException {
		IFolder rootDir = null;
		if (rootDirResource instanceof EclipseResource) {
			if (((EclipseResource) rootDirResource).getRawResource() instanceof IFolder) {
				rootDir = (IFolder) ((EclipseResource) rootDirResource)
						.getRawResource();
			}
		}
		else if (rootDirResource instanceof IAdaptable) {
			if (((IAdaptable) rootDirResource).getAdapter(IResource.class) instanceof IFolder) {
				rootDir = (IFolder) ((IAdaptable) rootDirResource)
						.getAdapter(IResource.class);
			}
		}

		if (rootDir == null) {
			return Collections.emptySet();
		}
		return doFindMatchingFileSystemResources(rootDir, subPattern);
	}

	protected Set<Resource> doFindMatchingFileSystemResources(IFolder rootDir,
			String subPattern) throws IOException {
		Set<IResource> matchingFiles = retrieveMatchingFiles(rootDir,
				subPattern);
		Set<Resource> result = new LinkedHashSet<Resource>(matchingFiles.size());
		for (IResource file : matchingFiles) {

			if (file instanceof IFile) {
				result.add(new FileResource((IFile) file));
			}
			else if (file instanceof StorageFile) {
				result.add(new StorageResource(((StorageFile) file)
						.getStorage()));
			}
		}
		return result;
	}

	protected Set<IResource> retrieveMatchingFiles(IFolder rootDir,
			String pattern) throws IOException {
		String fullPattern = StringUtils.replace(rootDir.getRawLocation()
				.toString(), File.separator, "/");
		if (!pattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern
				+ StringUtils.replace(pattern, File.separator, "/");
		Set<IResource> result = new LinkedHashSet<IResource>(8);
		doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	protected void doRetrieveMatchingFiles(String fullPattern, IFolder dir,
			Set<IResource> result) throws IOException {
		IResource[] dirContents = null;
		try {
			dirContents = dir.members();
		}
		catch (CoreException e) {
			throw new IOException("Could not retrieve contents of directory ["
					+ dir.getRawLocation() + "]");
		}
		if (dirContents == null) {
			throw new IOException("Could not retrieve contents of directory ["
					+ dir.getRawLocation() + "]");
		}
		for (int i = 0; i < dirContents.length; i++) {
			IResource content = dirContents[i];
			String currPath = StringUtils.replace(content.getRawLocation()
					.toString(), File.separator, "/");
			if (content instanceof IFolder
					&& getPathMatcher().matchStart(fullPattern, currPath + "/")) {
				doRetrieveMatchingFiles(fullPattern, (IFolder) content, result);
			}
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(content);
			}
		}
	}

}
