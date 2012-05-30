/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.internal.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class MavenClasspathUpdateJob extends WorkspaceJob {

	/** Internal cache of scheduled and <b>unfinished</b> update jobs */
	private static final Queue<IJavaProject> SCHEDULED_PROJECTS = new ConcurrentLinkedQueue<IJavaProject>();

	/** The {@link IJavaProject} this jobs should refresh the class path container for */
	private final IJavaProject javaProject;

	/**
	 * Private constructor to create an instance
	 * @param javaProject the {@link IJavaProject} the class path container should be updated for
	 * @param types the change types happened to the manifest
	 */
	private MavenClasspathUpdateJob(IJavaProject javaProject) {
		super("Updating Maven dependencies for project '" + javaProject.getElementName() + "'");
		this.javaProject = javaProject;
	}

	/**
	 * Returns the internal {@link IJavaProject}
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	/**
	 * Runs the job in the context of the workspace. Simply delegates refreshing of the class path container to
	 * {@link ClasspathUtils#updateClasspathContainer(IJavaProject, IProgressMonitor)}.
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		if (!javaProject.getProject().isOpen() || monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		try {
			IContainer basedir = findPomXmlBasedir(javaProject.getProject());
			if (basedir == null) {
				return Status.CANCEL_STATUS;
			}
			ILaunchConfiguration configuration = createLaunchConfiguration(basedir, "eclipse:clean eclipse:eclipse");
			if (configuration != null) {
				ILaunch launch = configuration.launch(ILaunchManager.RUN_MODE, new SubProgressMonitor(monitor, 75));
				DebugPlugin.getDefault().addDebugEventListener(
						new MavenProcessListener(launch.getProcesses()[0], javaProject.getProject()));
			}
		}
		catch (Exception e) {
			return Status.CANCEL_STATUS;
		}

		return new Status(IStatus.OK, MavenCorePlugin.PLUGIN_ID, "Updated Maven dependencies");
	}

	private IContainer findPomXmlBasedir(IContainer dir) {
		if (dir == null) {
			return null;
		}

		try {
			// loop upwards through the parents as long as we do not cross the project boundary
			while (dir.exists() && dir.getProject() != null && dir.getProject() != dir) {
				// see if pom.xml exists
				if (dir.getType() == IResource.FOLDER) {
					IFolder folder = (IFolder) dir;
					if (folder.findMember(IMavenConstants.POM_FILE_NAME) != null) {
						return folder;
					}
				}
				else if (dir.getType() == IResource.FILE) {
					if (((IFile) dir).getName().equals(IMavenConstants.POM_FILE_NAME)) {
						return dir.getParent();
					}
				}
				dir = dir.getParent();
			}
		}
		catch (Exception e) {
			return dir;
		}
		return dir;
	}

	private ILaunchConfiguration createLaunchConfiguration(IContainer basedir, String goal) {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager
					.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);

			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(basedir, //
					"Updating Maven dependencies for '" + basedir.getName() + "'");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, basedir.getLocation().toOSString());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, goal);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION, true);

			workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_SCOPE, "${project}");
			workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, true);

			String vmArguments = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			vmArguments += "-Declipse.workspace=\"${workspace_loc}\"";
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);

			IPath path = getJREContainerPath(basedir);
			if (path != null) {
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, path
						.toPortableString());
			}

			return workingCopy;
		}
		catch (CoreException ex) {
			MavenCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, MavenCorePlugin.PLUGIN_ID, "Error occured", ex));
		}
		return null;
	}

	private IPath getJREContainerPath(IContainer basedir) throws CoreException {
		IProject project = basedir.getProject();
		if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (JavaRuntime.JRE_CONTAINER.equals(entry.getPath().segment(0))) {
					return entry.getPath();
				}
			}
		}
		return null;
	}

	/**
	 * Helper method to schedule a new {@link MavenClasspathUpdateJob}.
	 * @param javaProject the {@link IJavaProject} the class path container should be updated for
	 * @param types the change types of the manifest
	 */
	public static void scheduleClasspathContainerUpdateJob(IJavaProject javaProject) {
		if (javaProject != null && !SCHEDULED_PROJECTS.contains(javaProject)) {
			newClasspathContainerUpdateJob(javaProject);
		}
	}

	public static void scheduleClasspathContainerUpdateJob(IProject oroject) {
		scheduleClasspathContainerUpdateJob(JavaCore.create(oroject));
	}

	/**
	 * Creates a new instance of {@link MavenClasspathUpdateJob} and configures required properties and schedules it to
	 * the workbench.
	 */
	private static MavenClasspathUpdateJob newClasspathContainerUpdateJob(IJavaProject javaProject) {
		MavenClasspathUpdateJob job = new MavenClasspathUpdateJob(javaProject);
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setPriority(Job.BUILD);
		job.addJobChangeListener(new DuplicateJobListener());
		job.schedule();
		return job;
	}

	/**
	 * Internal {@link IJobChangeListener} to detect duplicates in the scheduled list of {@link MavenClasspathUpdateJob
	 * Jobs}.
	 */
	private static class DuplicateJobListener extends JobChangeAdapter implements IJobChangeListener {

		@Override
		public void done(IJobChangeEvent event) {
			SCHEDULED_PROJECTS.remove(((MavenClasspathUpdateJob) event.getJob()).getJavaProject());
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			SCHEDULED_PROJECTS.add(((MavenClasspathUpdateJob) event.getJob()).getJavaProject());
		}
	}

	private class MavenProcessListener implements IDebugEventSetListener {

		private final IProject project;

		private final IProcess newProcess;

		public MavenProcessListener(IProcess process, IProject project) {
			this.project = project;
			this.newProcess = process;
		}

		public void handleDebugEvents(DebugEvent[] events) {
			if (events != null && project != null) {
				int size = events.length;
				for (int i = 0; i < size; i++) {
					if (newProcess != null && newProcess.equals(events[i].getSource())
							&& events[i].getKind() == DebugEvent.TERMINATE) {

						DebugPlugin.getDefault().removeDebugEventListener(this);

						Job job = new Job("refresh project") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
								}
								catch (CoreException e) {
								}
								return Status.OK_STATUS;
							}

						};
						job.setSystem(true);
						job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
						job.setPriority(Job.INTERACTIVE);
						job.schedule();
					}
				}
			}
		}
	}

}
