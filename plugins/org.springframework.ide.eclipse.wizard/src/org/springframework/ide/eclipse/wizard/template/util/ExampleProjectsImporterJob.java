/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;

public class ExampleProjectsImporterJob extends WorkspaceJob implements IOverwriteQuery {

	private final String projectName;

	private final URI uri;

	private final Shell shell;

	public static final String URL_SUFFIX = "/zipball/master";

	public ExampleProjectsImporterJob(URI myUri, String myProjectName, Shell myShell) {
		super("Importing " + myProjectName);
		this.uri = myUri;
		this.projectName = myProjectName;
		this.shell = myShell;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {

		File archiveFile = getArchiveFile(projectName);
		File targetDirectory = new File(projectName);

		URL url;
		try {
			url = uri.toURL();
			if (url.getProtocol().startsWith("http")) {
				if (url.getHost().endsWith("github.com")) {
					if (!url.getPath().endsWith(URL_SUFFIX)) {
						String urlString = url.getProtocol() + "://" + url.getAuthority();
						if (url.getPort() != -1) {
							urlString += ":" + url.getPort();
						}
						urlString += url.getPath() + URL_SUFFIX;
						// Ignoring query part because github URLs that we are
						// looking for do not have query parts. If there
						// is a query part, something has changed.
						url = new URL(urlString);
					}
				}
				else {
					// We should never get here: non-github URLs should have
					// been filtered out by here
					System.err.println("Uh-oh, this is not a github URL");
				}

			}
		}
		catch (MalformedURLException e) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("The URL {0} is malformed", uri));
		}

		try {
			ZipFileUtil.unzip(url, targetDirectory, new SubProgressMonitor(monitor, 50));
		}

		catch (IOException e1) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Error downloading {0} to {1}", url,
					archiveFile.getAbsolutePath()));
		}
		catch (OperationCanceledException e) {
			System.err.println("Operation cancelled!");
			monitor.done();
			return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, NLS.bind("Cancelled download of {0} to {1}.", url,
					archiveFile.getAbsolutePath()));
		}

		if (!targetDirectory.exists()) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Download of {0} failed",
					archiveFile.getAbsolutePath()));
		}

		File[] subdirs = targetDirectory.listFiles();
		if (subdirs.length <= 0) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
					"The zip file downloaded from {0} was empty", url));
		}

		File projectFile = null;
		File pomFile = null;

		// This allows for multiple directories, where we believe there will
		// only be one...
		for (File subdir : subdirs) {
			if (subdir.isDirectory()) {
				boolean hasClasspath = false;

				File[] projectFiles = subdir.listFiles();
				for (File aFile : projectFiles) {
					if (aFile.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
						projectFile = aFile;
					}
					if (aFile.getName().equals(".classpath")) {
						hasClasspath = true;
					}
					if (aFile.getName().equals("pom.xml")) {
						pomFile = aFile;
					}
				}

				IStatus creationStatus;
				if (hasClasspath && (projectFile != null)) {
					creationStatus = createExistingEclipseProject(projectFile, monitor);
					deleteRecursive(targetDirectory);
				}
				else if (pomFile != null) {
					creationStatus = createExistingMavenProject(pomFile, monitor);
					if (!hasMaven()) {
						String message = NLS
								.bind("You do not appear to have Maven installed.  This project requires Maven to import and build.",
										null);
						creationStatus = new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message);
					}
				}
				else {
					creationStatus = new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
							"Project {0} does not have either pom.xml or .project/.classpath", projectName));
				}
				monitor.done();
				return creationStatus;
			}
			else {
				monitor.done();
				return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, NLS.bind("Download of {0} successful",
						projectName));

			}
		}
		monitor.done();
		return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Unforeseen error downloading {0} from {1}",
				projectName, url));
	}

	private IStatus createExistingEclipseProject(File projectFile, IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		sub.setTaskName("Loading Eclipse project");

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);

		URI locationURI = projectFile.toURI();
		IProjectDescription newDesc;
		IProjectDescription downloadedDescription;
		try {

			downloadedDescription = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(
					new Path(projectFile.getPath()));
			downloadedDescription.setLocation(null);
		}
		catch (CoreException e) {
			return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, NLS.bind("Could parse{0}, maybe it is corrupted?",
					projectFile.getAbsolutePath()));
		}

		if (locationURI != null) {
			// validate the location of the project being copied
			IStatus result = ResourcesPlugin.getWorkspace().validateProjectLocationURI(project, locationURI);
			if (!result.isOK()) {
				return result;
			}

			newDesc = workspace.newProjectDescription(projectName);
			newDesc.setBuildSpec(downloadedDescription.getBuildSpec());
			newDesc.setComment(downloadedDescription.getComment());
			newDesc.setDynamicReferences(downloadedDescription.getDynamicReferences());
			newDesc.setNatureIds(downloadedDescription.getNatureIds());
			newDesc.setReferencedProjects(downloadedDescription.getReferencedProjects());

			try {
				monitor.beginTask(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask, 100);
				if (!project.exists()) {
					project.create(newDesc, new SubProgressMonitor(monitor, 30));
					project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 70));
					sub.worked(30);
				}
				else {
					sub.worked(30);
					return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Project {0} already exists.",
							project.getName()));
				}
			}
			catch (CoreException e) {
				return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Could not create project from {0}",
						projectFile));
			}

			File importSource = new File(locationURI).getParentFile();
			List filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(importSource);
			ImportOperation operation = new ImportOperation(project.getFullPath(), importSource,
					FileSystemStructureProvider.INSTANCE, this, filesToImport);
			operation.setContext(shell);
			operation.setOverwriteResources(true); // need to overwrite
			operation.setCreateContainerStructure(false);
			try {
				operation.run(monitor);
			}
			catch (InvocationTargetException e) {
				return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
						"Error: Could not create project from {0}", projectFile));
			}
			catch (InterruptedException e) {
				return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID,
						NLS.bind("Import of {0} interrupted", projectName));
			}
			sub.worked(70);

			// clean up after self
			deleteRecursive(importSource.getParentFile());
		}

		return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, NLS.bind("Import of {0} successful", projectName));

	}

	public IStatus createExistingMavenProject(File pomFile, IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		sub.setTaskName("Loading Maven project");

		IPath projectPath = Platform.getLocation().append(projectName);
		File downloadDirectory = pomFile.getParentFile();

		File projectWorkspaceDirectory = projectPath.toFile();
		if (!projectWorkspaceDirectory.exists() && !projectWorkspaceDirectory.mkdir()) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Error: Could not make directory {0}",
					projectWorkspaceDirectory));
		}

		try {
			FileUtil.copyDirectory(downloadDirectory.getAbsoluteFile(), projectWorkspaceDirectory.getAbsoluteFile(),
					monitor);
		}
		catch (CoreException e) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
					"Error: Could not copy project files from {0} to {1}", downloadDirectory.getAbsoluteFile(),
					projectWorkspaceDirectory.getAbsoluteFile()));
		}
		sub.worked(50);

		File newPomFile = projectPath.append("pom.xml").toFile();
		if (!newPomFile.exists()) {
			monitor.done();
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind("Error: Pom file not found at",
					newPomFile.getAbsolutePath()));
		}

		IStatus creationStatus;
		if (hasMaven()) {
			creationStatus = createEclipseProjectFromExistingMavenProject(newPomFile, monitor);
		}
		else {
			// FIXME: do what we can to extract something from the archive, even
			// though we can't make a maven project from it
			creationStatus = new Status(IStatus.WARNING, WizardPlugin.PLUGIN_ID, NLS.bind(
					"Error: Maven is not installed, cannot create a project from {0}", pomFile));

		}
		sub.worked(50);
		// clean up after self
		deleteRecursive(pomFile.getParentFile().getParentFile());
		return creationStatus;
	}

	public static IStatus createEclipseProjectFromExistingMavenProject(File pomFile, IProgressMonitor monitor) {
		try {
			MavenCorePlugin.createEclipseProjectFromExistingMavenProject(pomFile, monitor);
		}
		catch (CoreException e) {
			monitor.done();
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
					"Error: Could not create project from {0}", pomFile));
		}

		monitor.done();
		return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, NLS.bind("Import of {0} successful", pomFile));
	}

	// For now, always overwrite
	public String queryOverwrite(String pathString) {
		return IOverwriteQuery.YES;
	}

	private File getArchiveFile(String projectName) {
		// TODO strip dangerous characters from the file name?
		return new File(projectName + ".zip");
	}

	private static boolean hasMaven() {
		return (MavenCorePlugin.IS_M2ECLIPSE_PRESENT || MavenCorePlugin.IS_LEGACY_M2ECLIPSE_PRESENT);
	}

	private void deleteRecursive(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File subFile : files) {
				deleteRecursive(subFile);
			}
		}
		file.delete();
	}
}
