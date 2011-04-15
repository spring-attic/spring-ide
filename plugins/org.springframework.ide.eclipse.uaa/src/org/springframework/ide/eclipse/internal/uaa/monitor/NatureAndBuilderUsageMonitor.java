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

import java.util.Collections;
import java.util.HashMap;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;

/**
 * {@link IUsageMonitor} that records usage data for project nature and builder definitions.
 * @author Christian Dupuis
 * @since 2.6.0
 */
public class NatureAndBuilderUsageMonitor implements IUsageMonitor {

	private IUaa manager;

	private ExtensionIdToBundleMapper natureBundleMapper;

	private ExtensionIdToBundleMapper builderBundleMapper;

	private IResourceChangeListener resourceChangeListener;

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;
		this.natureBundleMapper = new ExtensionIdToBundleMapper(ResourcesPlugin.PI_RESOURCES + "."
				+ ResourcesPlugin.PT_NATURES) {

			protected synchronized void updateCommandToBundleMappings() {
				if (map != null) {
					return;
				}
				map = new HashMap<String, String>();
				IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES,
						ResourcesPlugin.PT_NATURES);
				for (IExtension extension : point.getExtensions()) {
					map.put(extension.getUniqueIdentifier(), extension.getNamespaceIdentifier());
				}
			};

		};

		this.builderBundleMapper = new ExtensionIdToBundleMapper(ResourcesPlugin.PI_RESOURCES + "."
				+ ResourcesPlugin.PT_BUILDERS) {

			protected synchronized void updateCommandToBundleMappings() {
				if (map != null) {
					return;
				}
				map = new HashMap<String, String>();
				IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES,
						ResourcesPlugin.PT_BUILDERS);
				for (IExtension extension : point.getExtensions()) {
					map.put(extension.getUniqueIdentifier(), extension.getNamespaceIdentifier());
				}
			};

		};

		this.resourceChangeListener = new DotProjectResourceChangeListener();

		Job startup = new Job("Initializing nature- and builder-based usage monitoring") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Before we start get all projects and record natures and builders
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.isOpen() && project.isAccessible()) {
						projectChanged(project);
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
		if (natureBundleMapper != null) {
			natureBundleMapper.dispose();
		}
		if (builderBundleMapper != null) {
			builderBundleMapper.dispose();
		}
		if (resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
	}

	/**
	 * Record usage data to a given project.
	 */
	private void projectChanged(IProject project) {
		if (project != null && project.isAccessible() && project.isOpen()) {
			try {
				for (String natureId : project.getDescription().getNatureIds()) {
					recordNatureEvent(natureId, natureBundleMapper.getBundleId(natureId), project.getName());
				}
				for (ICommand command : project.getDescription().getBuildSpec()) {
					recordBuilderEvent(command.getBuilderName(),
							builderBundleMapper.getBundleId(command.getBuilderName()), project.getName());
				}
			}
			catch (CoreException e) {
				// intentionally only onto the console
				e.printStackTrace();
			}
		}
	}

	/**
	 * Records the usage of a certain nature in a given project.
	 */
	private void recordNatureEvent(String natureId, String bundleId, String project) {
		IProjectNatureDescriptor desc = ResourcesPlugin.getWorkspace().getNatureDescriptor(natureId);
		if (desc != null && bundleId != null && project != null) {
			String label = desc.getLabel();
			// Do some cosmetic changes to the label
			if (label == null || label.startsWith("%")) {
				label = natureId.toLowerCase();
			}
			else if (label.toLowerCase().endsWith("nature")) {
				label = label.substring(0, label.length() - 6).trim();
			}
			if (label != null && label.length() > 0) {
				manager.registerProjectUsageForProduct(bundleId, project, Collections.singletonMap("nature", label));
			}
			else {
				manager.registerProjectUsageForProduct(bundleId, project, IUaa.EMPTY_DATA);
			}
		}
	}

	/**
	 * Records the usage of a certain builder in a given project.
	 */
	private void recordBuilderEvent(String builderId, String bundleId, String project) {
		IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES,
				ResourcesPlugin.PT_BUILDERS, builderId);
		if (builderId != null && bundleId != null) {
			String label = extension.getLabel();
			if (label != null && label.length() > 0) {
				manager.registerProjectUsageForProduct(bundleId, project, Collections.singletonMap("builder", label));
			}
			else {
				manager.registerProjectUsageForProduct(bundleId, project, IUaa.EMPTY_DATA);
			}
		}
	}

	/**
	 * {@link IResourceChangeListener} that listens to changes of a project's <code>.project</code> file.
	 */
	class DotProjectResourceChangeListener implements IResourceChangeListener {

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
			return new DotProjectResourceVisitor();
		}

		/**
		 * Internal resource delta visitor.
		 */
		protected class DotProjectResourceVisitor implements IResourceDeltaVisitor {

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
				if (resource instanceof IFile && resource.getName().equals(".project") && resource.isAccessible()) {
					projectChanged(resource.getProject());
					return false;
				}
				return true;
			}
		}
	}

}
