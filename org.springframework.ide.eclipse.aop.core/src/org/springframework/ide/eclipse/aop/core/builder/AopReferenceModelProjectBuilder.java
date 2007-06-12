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
package org.springframework.ide.eclipse.aop.core.builder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilder;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * {@link IProjectBuilder} that triggers creation of Spring IDE's internal AOP
 * reference model.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelProjectBuilder implements IProjectBuilder {

	/**
	 * Returns a {@link Set} of {@link IResource} instances that need to be
	 * rebuild in the context of the current <code>resource</code> and
	 * <code>kind</code>
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {
			resources.addAll(AopReferenceModelUtils.getAffectedFiles(kind,
					resource));
		}
		return resources;
	}

	/**
	 * Starts creation of AOP reference model by passing the Set of
	 * affectedResources on to a new instance of
	 * {@link AopReferenceModelBuilder}.
	 */
	public void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException {
		try {
			monitor
					.subTask(Activator
							.getFormattedMessage("AopReferenceModelProjectBuilder.buildingAopReferenceModel"));
			IWorkspaceRunnable referenceModelBuilder = new AopReferenceModelBuilder(
					affectedResources);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			ISchedulingRule rule = workspace.getRuleFactory().markerRule(
					workspace.getRoot());
			workspace.run(referenceModelBuilder, rule, IWorkspace.AVOID_UPDATE,
					monitor);
		}
		catch (CoreException e) {
			Activator.log(e);
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * Removes the AOP reference model markers
	 */
	public void cleanup(IResource resource, IProgressMonitor monitor) {
		try {
			monitor.subTask(Activator.getFormattedMessage(
					"AopReferenceModelProjectBuilder.deletedProblemMarkers",
					resource.getFullPath()));
			AopReferenceModelMarkerUtils.deleteProblemMarkers(resource);

			IProject project = resource.getProject();
			// reinit aop reference model
			if (JdtUtils.isJavaProject(project)) {
				Activator.getModel().removeProject(
						JdtUtils.getJavaProject(project));
			}
		}
		finally {
			monitor.done();
		}
	}
}
