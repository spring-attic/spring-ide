/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class SpringCoreUtils {

	/**
	 * Returns the specified adapter for the given object or <code>null</code>
	 * if adapter is not supported.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(Object object, Class<T> adapter) {
		if (object != null && adapter != null) {
			if (adapter.isAssignableFrom(object.getClass())) {
				return (T) object;
			}
			if (object instanceof IAdaptable) {
				return (T) ((IAdaptable) object).getAdapter(adapter);
			}
		}
		return null;
	}

	/**
	 * Returns a list of all projects with the Spring project nature.
	 */
	public static Set<IProject> getSpringProjects() {
		Set<IProject> projects = new LinkedHashSet<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {
			if (isSpringProject(project)) {
				projects.add(project);
			}
		}
		return projects;
	}

	/**
	 * Creates specified simple project.
	 */
	public static IProject createProject(String projectName,
			IProjectDescription description, IProgressMonitor monitor)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists()) {
			if (description == null) {
				project.create(monitor);
			}
			else {
				project.create(description, monitor);
			}
		}
		else {
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
	 * Creates given folder and (if necessary) all of it's parents.
	 */
	public static void createFolder(IFolder folder, IProgressMonitor monitor)
			throws CoreException {
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
	public static void addProjectNature(IProject project, String nature,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && nature != null) {
			if (!project.hasNature(nature)) {
				IProjectDescription desc = project.getDescription();
				String[] oldNatures = desc.getNatureIds();
				String[] newNatures = new String[oldNatures.length + 1];
				newNatures[0] = nature;
				if (oldNatures.length > 0) {
					System.arraycopy(oldNatures, 0, newNatures, 1,
							oldNatures.length);
				}
				desc.setNatureIds(newNatures);
				project.setDescription(desc, monitor);
			}
		}
	}

	/**
	 * Removes given nature from specified project.
	 */
	public static void removeProjectNature(IProject project, String nature,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && nature != null) {
			if (project.exists() && project.hasNature(nature)) {

				// first remove all problem markers (including the
				// inherited ones) from Spring beans project
				if (nature.equals(SpringCore.NATURE_ID)) {
					project.deleteMarkers(SpringCore.MARKER_ID, true,
							IResource.DEPTH_INFINITE);
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
	 * Adds given builder to specified project.
	 */
	public static void addProjectBuilder(IProject project, String builderID,
			IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand builderCommand = getProjectBuilderCommand(desc, builderID);
		if (builderCommand == null) {

			// Add a new build spec
			ICommand command = desc.newCommand();
			command.setBuilderName(builderID);

			// Commit the spec change into the project
			addProjectBuilderCommand(desc, command);
			project.setDescription(desc, monitor);
		}
	}

	/**
	 * Removes given builder from specified project.
	 */
	public static void removeProjectBuilder(IProject project, String builderID,
			IProgressMonitor monitor) throws CoreException {
		if (project != null && builderID != null) {
			IProjectDescription desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();
			for (int i = commands.length - 1; i >= 0; i--) {
				if (commands[i].getBuilderName().equals(builderID)) {
					ICommand[] newCommands = new ICommand[commands.length - 1];
					System.arraycopy(commands, 0, newCommands, 0, i);
					System.arraycopy(commands, i + 1, newCommands, i,
							commands.length - i - 1);
					// Commit the spec change into the project
					desc.setBuildSpec(newCommands);
					project.setDescription(desc, monitor);
					break;
				}
			}
		}
	}

	/**
	 * Adds (or updates) a builder in given project description.
	 */
	public static void addProjectBuilderCommand(IProjectDescription description,
			 ICommand command) throws CoreException {
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldBuilderCommand = getProjectBuilderCommand(description,
				command.getBuilderName());
		ICommand[] newCommands;
		if (oldBuilderCommand == null) {

			// Add given builder to the end of the builder list
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 0,
							 oldCommands.length);
			newCommands[oldCommands.length] = command;
		} else {

			// Replace old builder with given new one
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i] == oldBuilderCommand) {
					oldCommands[i] = command;
					break;
				}
			}
			newCommands = oldCommands;
		}
		description.setBuildSpec(newCommands);
	}

	/**
	 * Returns specified builder from given project description.
	 */
	public static ICommand getProjectBuilderCommand(
			IProjectDescription description, String builderID)
			throws CoreException {
		ICommand[] commands = description.getBuildSpec();
		for (int i = commands.length - 1; i >= 0; i--) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}

	/**
	 * Returns true if given resource's project is a Spring project.
	 */
	public static boolean isSpringProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			IProject project = resource.getProject();
			if (project != null) {
				try {
					return project.hasNature(SpringCore.NATURE_ID);
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if Eclipse's runtime bundle has the same or a newer than
	 * given version.
	 */
	public static boolean isEclipseSameOrNewer(int majorVersion,
			int minorVersion) {
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
			}
			catch (NoSuchElementException e) {
				// ignore
			}
			catch (NumberFormatException e) {
				// ignore
			}
		}
		return false;
	}

	public static IPath getProjectLocation(IProject project) {
		return (project.getRawLocation() != null ? project.getRawLocation()
				: project.getLocation());
	}

	/**
	 * Triggers a build of the given {@link IProject} instance.
	 * @param project the project to build
	 */
	public static void buildProject(IProject project) {
		if (ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			scheduleBuildInBackground(project, ResourcesPlugin.getWorkspace()
					.getRuleFactory().buildRule(),
					new Object[] { ResourcesPlugin.FAMILY_MANUAL_BUILD });
		}
	}

	private static void scheduleBuildInBackground(final IProject project,
			ISchedulingRule rule, final Object[] jobFamilies) {
		Job job = new WorkspaceJob("Build workspace") {

			@Override
			public boolean belongsTo(Object family) {
				if (jobFamilies == null || family == null) {
					return false;
				}
				for (int i = 0; i < jobFamilies.length; i++) {
					if (family.equals(jobFamilies[i])) {
						return true;
					}
				}
				return false;
			}

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				try {
					project
							.build(IncrementalProjectBuilder.FULL_BUILD,
									monitor);
					return Status.OK_STATUS;
				}
				catch (CoreException e) {
					return new MultiStatus(SpringCore.PLUGIN_ID, 1,
							"Error during build of project ["
									+ project.getName() + "]", e);
				}
			}
		};
		if (rule != null) {
			job.setRule(rule);
		}
		job.setUser(true);
		job.schedule();
	}
}
