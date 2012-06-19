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
package org.springframework.ide.eclipse.osgi.runtime.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * Spring IDE {@link IProjectBuilder} implementation that detects changes in
 * code belonging to Spring and PDE projects and triggers a bundle update or
 * refresh on the running OSGi Framework.
 * <p>
 * If a change in an exported package is detected a 'refresh' is being
 * triggered; otherwise just a update.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
@SuppressWarnings("restriction")
public class OsgiBundleUpdateBuilder implements IProjectBuilder {

	/** The possible commands */
	public enum Command {
		REFRESH
	}

	/*
	 * @see org.springframework.ide.eclipse.core.project.IProjectBuilder#build(java.util.Set,
	 * int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException {

		Map<IProject, Set<IResource>> projects = groupResourcesByProject(affectedResources);

		// For each IProject the PDE model and schedule the update job
		for (Map.Entry<IProject, Set<IResource>> project : projects.entrySet()) {
			IPluginModelBase model = PDECore.getDefault().getModelManager()
					.findModel(project.getKey());
			BundleDescription description = model.getBundleDescription();
			String symbolicName = description.getSymbolicName();

			// Schedule the job
			OsgiUpdateJob.schedule(symbolicName, Command.REFRESH);
		}
	}

	/**
	 * Nothing to clean up.
	 */
	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		// nothing to clean up
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.core.project.IProjectContributor#getAffectedResources(org.eclipse.core.resources.IResource,
	 * int)
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind)
			throws CoreException {
		// Check if project is a PDE project and Java Project; if not don't
		// update
		if (resource instanceof IFile    
				&& SpringCoreUtils.hasNature(resource, PDE.PLUGIN_NATURE)
				&& JdtUtils.isJavaProject(resource)) {

			IPluginModelBase model = PDECore.getDefault().getModelManager()
					.findModel(resource.getProject());

			Set<IResource> resources = new HashSet<IResource>();
			if (model != null) {
				resources.add(resource);
			}
			return resources;

		}
		return Collections.emptySet();
	}

	private Map<IProject, Set<IResource>> groupResourcesByProject(
			Set<IResource> affectedResources) {
		Map<IProject, Set<IResource>> projects = new HashMap<IProject, Set<IResource>>();
		for (IResource resource : affectedResources) {

			if (projects.containsKey(resource.getProject())) {
				projects.get(resource.getProject()).add(resource);
			}
			else {
				Set<IResource> resources = new HashSet<IResource>();
				resources.add(resource);
				projects.put(resource.getProject(), resources);
			}
		}
		return projects;
	}

}
