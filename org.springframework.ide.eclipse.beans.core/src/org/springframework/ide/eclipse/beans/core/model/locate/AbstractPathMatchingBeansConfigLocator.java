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
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Abstract {@link IBeansConfigLocator} implementation that uses ant file patterns to located
 * {@link IBeansConfig} candidates in the given {@link IProject}.
 * <p>
 * File patterns need to provided via the {@link #getAllowedFilePatterns()} method. To fine control
 * the matching process this implementation offers the {@link #canLocateInProject(IProject)} and
 * {@link #filterMatchingFiles(Set)}.
 * <p>
 * This implementation is only usable on {@link IJavaProject} as it iterates each source folder.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractPathMatchingBeansConfigLocator extends AbstractBeansConfigLocator {

	/** Internal path matcher that understands ant patterns */
	private PathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * Locates potential {@link IFile}s. Uses ant file name patterns to match all resources of a
	 * project recursively.
	 * @see #canLocateInProject(IProject)
	 * @see #filterMatchingFiles(Set)
	 * @see #getAllowedFilePatterns()
	 */
	public final Set<IFile> locateBeansConfigs(IProject project) {
		Set<IFile> files = new LinkedHashSet<IFile>();
		if (canLocateInProject(project)) {
			try {
				// first make sure the project is in sync with the file system
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
			catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
			IJavaProject javaProject = JdtUtils.getJavaProject(project);
			if (javaProject != null) {
				try {
					for (IClasspathEntry entry : javaProject.getResolvedClasspath(false)) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IFolder folder = (IFolder) javaProject.getProject().findMember(
									entry.getPath().removeFirstSegments(1));
							locateConfigsInFolder(files, javaProject, folder);
						}
					}
				}
				catch (JavaModelException e) {
					BeansCorePlugin.log(e);
				}
				catch (CoreException e) {
					BeansCorePlugin.log(e);
				}
			}
		}
		return filterMatchingFiles(files);
	}

	/**
	 * Locates matching files in the given <code>folder</code>. Walks down the file tree until no
	 * further sub-folder is found.
	 */
	private void locateConfigsInFolder(Set<IFile> files, IJavaProject javaProject, IFolder folder)
			throws CoreException {
		if (folder != null && folder.exists()) {
			for (IResource resource : folder.members()) {
				if (resource instanceof IFile) {
					IPath filePath = resource.getProjectRelativePath().removeFirstSegments(1);
					for (String pattern : getAllowedFilePatterns()) {
						if (pathMatcher.match(pattern, filePath.toString())) {
							files.add((IFile) resource);
						}
					}
				}
				else if (resource instanceof IFolder) {
					locateConfigsInFolder(files, javaProject, (IFolder) resource);
				}
			}
		}
	}

	/**
	 * Pre-check to make sure that this implementation works on the given project and its type.
	 * <p>
	 * Sub-classes may wish to override this method to do own pre-checks.
	 * <p>
	 * This default implementation just checks if the given <code>project</code> is a java
	 * project.
	 * @param project the project to check if this implementation can search of {@link IBeansConfig}
	 * files
	 * @return true if the given project should be scanned for files
	 */
	protected boolean canLocateInProject(IProject project) {
		return JdtUtils.isJavaProject(project);
	}

	/**
	 * Filter path matching files. This method provides a post-processing hook for filtering found
	 * files. Sub-classes might want to override this method to filter out any un-wanted files.
	 * <p>
	 * This default implementation just passes through the given set of files.
	 * @param files the files to filter
	 * @return the {@link Set} of filtered files
	 */
	protected Set<IFile> filterMatchingFiles(Set<IFile> files) {
		return files;
	}

	protected abstract String[] getAllowedFilePatterns();

}
