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
package org.springframework.ide.eclipse.maven;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.maven.internal.core.MavenClasspathUpdateJob;


/**
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 */
public class MavenCorePlugin extends AbstractUIPlugin {
	
	private static final String M2ECLIPSE_CLASS = "org.eclipse.m2e.core.MavenPlugin";
	private static final String M2ECLIPSE_LEGACY_CLASS = "org.maven.ide.eclipse.MavenPlugin";

	public static final boolean IS_M2ECLIPSE_PRESENT = isPresent(M2ECLIPSE_CLASS);
	public static final boolean IS_LEGACY_M2ECLIPSE_PRESENT = isPresent(M2ECLIPSE_LEGACY_CLASS);

	private static boolean isPresent(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.maven"; //$NON-NLS-1$

	public static final String AUTOMATICALLY_UPDATE_DEPENDENCIES_KEY = "maven.automatically.update";

	public static final boolean AUTOMATICALLY_UPDATE_DEPENDENCIES_DEFAULT = false;

	public static final String M2ECLIPSE_NATURE = "org.eclipse.m2e.core.maven2Nature";

    public static final String M2ECLIPSE_LEGACY_NATURE = "org.maven.ide.eclipse.maven2Nature";

	private static MavenCorePlugin plugin;

	private IResourceChangeListener resourceChangeListener;
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (IS_M2ECLIPSE_PRESENT || IS_LEGACY_M2ECLIPSE_PRESENT) {
			resourceChangeListener = new PomResourceChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		if (resourceChangeListener != null && (IS_M2ECLIPSE_PRESENT || IS_LEGACY_M2ECLIPSE_PRESENT)) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
	}

	public static MavenCorePlugin getDefault() {
		return plugin;
	}

	public void scheduleClasspathUpdateJob(IProject project) {
		if (IS_M2ECLIPSE_PRESENT) {
			MavenClasspathUpdateJob.scheduleClasspathContainerUpdateJob(project);
		}
	}

	/**
	 * {@link IResourceChangeListener} that listens to changes to the pom.xml resources.
	 */
	class PomResourceChangeListener implements IResourceChangeListener {

		/**
		 * Internal resource delta visitor.
		 */
		protected class PomResourceVisitor implements IResourceDeltaVisitor {

			protected int eventType;

			public PomResourceVisitor(int eventType) {
				this.eventType = eventType;
			}

			public final boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					return resourceAdded(resource);

				case IResourceDelta.OPEN:
					return resourceOpened(resource);

				case IResourceDelta.CHANGED:
					return resourceChanged(resource, delta.getFlags());

				case IResourceDelta.REMOVED:
					return resourceRemoved(resource);
				}
				return true;
			}

			protected boolean resourceAdded(IResource resource) {
				if (resource instanceof IFile) {
					updateDependenciesIfPom(resource);
					return false;
				}
				return true;
			}

			protected boolean resourceChanged(IResource resource, int flags) {
				if (resource instanceof IFile) {
					if ((flags & IResourceDelta.CONTENT) != 0) {
						updateDependenciesIfPom(resource);
					}
					return false;
				}
				return true;
			}

			protected boolean resourceOpened(IResource resource) {
				return true;
			}

			protected boolean resourceRemoved(IResource resource) {
				return true;
			}

			private void updateDependenciesIfPom(IResource resource) {
				if (resource.getName().equals("pom.xml")
						&& SpringCorePreferences.getProjectPreferences(resource.getProject(), PLUGIN_ID).getBoolean(
								AUTOMATICALLY_UPDATE_DEPENDENCIES_KEY, AUTOMATICALLY_UPDATE_DEPENDENCIES_DEFAULT)
						&& !SpringCoreUtils.hasNature(resource, M2ECLIPSE_NATURE)) {
					MavenClasspathUpdateJob.scheduleClasspathContainerUpdateJob(resource.getProject());
				}
			}
		}

		public static final int LISTENER_FLAGS = IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.PRE_BUILD;

		private static final int VISITOR_FLAGS = IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED;

		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getSource() instanceof IWorkspace) {
				int eventType = event.getType();
				switch (eventType) {
				case IResourceChangeEvent.POST_CHANGE:
					IResourceDelta delta = event.getDelta();
					if (delta != null) {
						try {
							delta.accept(getVisitor(eventType), VISITOR_FLAGS);
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
							delta.accept(getVisitor(eventType), VISITOR_FLAGS);
						}
						catch (CoreException e) {
							SpringCore.log("Error while traversing resource change delta", e);
						}
					}
					break;
				}
			}

		}

		protected IResourceDeltaVisitor getVisitor(int eventType) {
			return new PomResourceVisitor(eventType);
		}
	}
	
	public static void createEclipseProjectFromExistingMavenProject(File pomFile, IProgressMonitor monitor) throws CoreException {

			Model model = MavenPlugin.getMavenModelManager().readMavenModel(pomFile);
			String derivedProjectName = model.getName();
			MavenProjectInfo parent = null;
			MavenProjectInfo projectInfo = new MavenProjectInfo(derivedProjectName, pomFile, model, parent);
			ArrayList<MavenProjectInfo> projectInfos = new ArrayList<MavenProjectInfo>();
			projectInfos.add(projectInfo);
			ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
			String activeProfiles = "pom.xml";
			resolverConfiguration.setActiveProfiles(activeProfiles);
			ProjectImportConfiguration configuration = new ProjectImportConfiguration(resolverConfiguration);

			MavenPlugin.getProjectConfigurationManager().importProjects(projectInfos, configuration, monitor);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(derivedProjectName);
			MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, monitor);
		
	}

}
