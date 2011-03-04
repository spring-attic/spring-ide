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
package org.springframework.ide.eclipse.beans.core.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ConcurrentModificationException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Christian Dupuis
 * @since 2.0.3
 */
public abstract class BeansCoreTestCase extends TestCase {

	protected void tearDown() throws Exception {
		super.tearDown();
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			deleteProject(project, false);
		}
		allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			deleteProject(project, true);
		}
	}

	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform
					.getBundle(
							"org.springframework.ide.eclipse.beans.core.tests").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
			return new File(FileLocator.toFileURL(platformURL).getFile())
					.getAbsolutePath();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() + java.io.File.separator + "workspace"; //$NON-NLS-1$
	}

	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}

	protected IProject createPredefinedProject(final String projectName)
			throws CoreException, IOException {
		IJavaProject jp = setUpJavaProject(projectName);
		getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		jp.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		return jp.getProject();
	}

	protected IJavaProject setUpJavaProject(final String projectName)
			throws CoreException, IOException {
		return setUpJavaProject(projectName, "1.4"); //$NON-NLS-1$
	}

	protected IJavaProject setUpJavaProject(final String projectName,
			String compliance) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile()
				.getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(
				targetWorkspacePath, projectName));

		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				try {
					project.open(null);
				}
				catch (ConcurrentModificationException e) {
					// wait and try again to work-around
					// ConcurrentModificationException (bug 280488)
					try {
						Thread.sleep(500);
						project.open(null);
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					}
					catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		getWorkspace().run(populate, null);
		IJavaProject javaProject = JavaCore.create(project);
		return javaProject;
	}

	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
						null);
				wasInterrupted = false;
			}
			catch (OperationCanceledException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	public static void waitForManualBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD,
						null);
				wasInterrupted = false;
			}
			catch (OperationCanceledException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	/**
	 * Copy the given source directory (and all its contents) to the given
	 * target directory.
	 */
	protected void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			File sourceChild = files[i];
			String name = sourceChild.getName();
			if (name.equals("CVS"))continue; //$NON-NLS-1$
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			}
			else {
				copy(sourceChild, targetChild);
			}
		}
	}

	/**
	 * Copy file from src (path to the original file) to dest (path to the
	 * destination file).
	 */
	public void copy(File src, File dest) throws IOException {
		// read source bytes
		byte[] srcBytes = this.read(src);
		// write bytes to dest
		FileOutputStream out = new FileOutputStream(dest);
		out.write(srcBytes);
		out.close();
	}

	public byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = new java.io.FileInputStream(file);
		int bytesRead = 0;
		int lastReadSize = 0;
		while ((lastReadSize != -1) && (bytesRead != fileLength)) {
			lastReadSize = stream.read(fileBytes, bytesRead, fileLength
					- bytesRead);
			bytesRead += lastReadSize;
		}
		stream.close();
		return fileBytes;
	}

	public static String convertToIndependantLineDelimiter(String source) {
		if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1)
			return source;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = source.length(); i < length; i++) {
			char car = source.charAt(i);
			if (car == '\r') {
				buffer.append('\n');
				if (i < length - 1 && source.charAt(i + 1) == '\n') {
					i++; // skip \n after \r
				}
			}
			else {
				buffer.append(car);
			}
		}
		return buffer.toString();
	}

	protected IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	protected void deleteProject(IProject project, boolean force)
			throws CoreException {
		if (project.exists() && !project.isOpen()) { // force opening so that
														// project can be
														// deleted without
														// logging (see bug
														// 23629)
			project.open(null);
		}
		deleteResource(project, force);
	}

	protected void deleteProject(String projectName) throws CoreException {
		deleteProject(this.getProject(projectName), true);
	}

	/**
	 * Delete this resource.
	 */
	public void deleteResource(IResource resource, boolean force)
			throws CoreException {
		waitForManualBuild();
		waitForAutoBuild();
		CoreException lastException = null;
		try {
			resource.delete(false, null);
		}
		catch (CoreException e) {
			lastException = e;
			// just print for info
			System.out
					.println("(CoreException): " + e.getMessage() + " Resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		}
		catch (IllegalArgumentException iae) {
			// just print for info
			System.out
					.println("(IllegalArgumentException): " + iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!force) {
			return;
		}
		int retryCount = 10; // wait 1 minute at most
		while (resource.isAccessible() && --retryCount >= 0) {
			waitForAutoBuild();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			try {
				resource.delete(true, null);
			}
			catch (CoreException e) {
				lastException = e;
				// just print for info
				System.out
						.println("(CoreException) Retry " + retryCount + ": " + e.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			catch (IllegalArgumentException iae) {
				// just print for info
				System.out
						.println("(IllegalArgumentException) Retry " + retryCount + ": " + iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		if (!resource.isAccessible())
			return;
		System.err.println("Failed to delete " + resource.getFullPath()); //$NON-NLS-1$
		if (lastException != null) {
			throw lastException;
		}
	}

	protected IResource createPredefinedProjectAndGetResource(
			String projectName, String resourcePath) throws CoreException,
			IOException {
		IProject project = createPredefinedProject(projectName);
		return project.findMember(resourcePath);
	}
}
