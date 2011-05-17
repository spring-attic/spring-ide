/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.ide.eclipse.uaa.UaaUtils;

/**
 * {@link IUsageMonitor} that records usage data for build systems based on common build files in the root of the
 * project.
 * <p>
 * Note: this implementation only checks for pom.xml, build.xml, build.gradle and ivy.xml in the root of the project.
 * @author Christian Dupuis
 * @since 2.6.0
 */
public class BuildSystemUsageMonitor implements IUsageMonitor {

	private IUaa manager;

	private IResourceChangeListener resourceChangeListener;

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;

		this.resourceChangeListener = new BuildFilesProjectResourceChangeListener();

		Job startup = new Job("Initializing build system-based usage monitoring") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Before we start get all projects and record build system usage
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.isOpen() && project.isAccessible()) {
						if (project.getFile("pom.xml").exists()) {
							projectChanged(project.getName(), BuildSystemType.MAVEN);
						}
						if (project.getFile("ivy.xml").exists()) {
							projectChanged(project.getName(), BuildSystemType.IVY);
						}
						if (project.getFile("build.gradle").exists()) {
							projectChanged(project.getName(), BuildSystemType.GRADLE);
						}
						if (project.getFile("build.xml").exists()) {
							projectChanged(project.getName(), BuildSystemType.ANT);
						}
						
						// Now we should also check the parent folder for the project location on the
						// file system to check for the same files there
						URI uri = UaaUtils.getResourceURI(project);
						if (uri != null) {
							File parent = new File(uri);
							if (parent != null && parent.exists()) {
								parent = parent.getParentFile();
								
								for (File file : parent.listFiles()) {
									if (file.isFile()) {
										if (file.getName().equals("pom.xml")) {
											projectChanged(parent.getName(), BuildSystemType.MAVEN);
										}
										if (file.getName().equals("ivy.xml")) {
											projectChanged(parent.getName(), BuildSystemType.IVY);
										}
										if (file.getName().equals("build.gradle")) {
											projectChanged(parent.getName(), BuildSystemType.GRADLE);
										}
										if (file.getName().equals("build.xml")) {
											projectChanged(parent.getName(), BuildSystemType.ANT);
										}
									}
								}
							}
						}
					}
				}

				ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
				return Status.OK_STATUS;
			}

		};
		startup.setSystem(true);
		startup.schedule(3000);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stopMonitoring() {
		if (resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
	}

	/**
	 * Record usage data to a given project.
	 */
	private void projectChanged(String projectName, BuildSystemType type) {
		if (type != null && projectName != null) {
			manager.registerProductUse(BuildSystemType.getName(type), null, projectName);
		}
	}

	/**
	 * {@link IResourceChangeListener} that listens to changes of a project's <code>pom.xml</code>.
	 */
	class BuildFilesProjectResourceChangeListener implements IResourceChangeListener {

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
							UaaPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, UaaPlugin.PLUGIN_ID, "Error while traversing resource change delta", e));
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
							UaaPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, UaaPlugin.PLUGIN_ID, "Error while traversing resource change delta", e));
						}
					}
					break;
				}
			}

		}

		protected IResourceDeltaVisitor getVisitor() {
			return new BuildFilesProjectResourceVisitor();
		}

		/**
		 * Internal resource delta visitor.
		 */
		protected class BuildFilesProjectResourceVisitor implements IResourceDeltaVisitor {

			public final boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					return resourceAddedOrChanged(resource);
				case IResourceDelta.CHANGED:
					return resourceAddedOrChanged(resource);
				}
				return true;
			}

			protected boolean resourceAddedOrChanged(IResource resource) {
				if (resource instanceof IFile && resource.isAccessible()) {
					if (resource.getName().equals("pom.xml")) {
						projectChanged(resource.getProject().getName(), BuildSystemType.MAVEN);
					}
					if (resource.getName().equals("ivy.xml")) {
						projectChanged(resource.getProject().getName(), BuildSystemType.IVY);
					}
					if (resource.getName().equals("build.gradle")) {
						projectChanged(resource.getProject().getName(), BuildSystemType.GRADLE);
					}
					if (resource.getName().equals("build.xml")) {
						projectChanged(resource.getProject().getName(), BuildSystemType.ANT);
					}
					return false;
				}
				else if (resource instanceof IFolder) {
					return false;
				}
				return true;
			}
		}
	}

	/**
	 * Internal enum to represent different build systems. 
	 */
	private enum BuildSystemType {

		MAVEN, ANT, IVY, GRADLE;

		public static String getName(BuildSystemType type) {
			switch (type) {
			case MAVEN:
				return "Maven";
			case ANT:
				return "Ant";
			case IVY:
				return "Ivy";
			case GRADLE:
				return "Gradle";
			default:
				return null;
			}
		}
	}

}
