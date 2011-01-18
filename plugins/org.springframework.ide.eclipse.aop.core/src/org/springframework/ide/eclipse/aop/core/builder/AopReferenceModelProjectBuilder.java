/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AopReferenceModelBuilderJob;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorStateAware;

/**
 * {@link IProjectBuilder} that triggers creation of Spring IDE's internal AOP reference model.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AopReferenceModelProjectBuilder implements IProjectBuilder, IProjectContributorStateAware {

	private IProjectContributorState context = null;

	/**
	 * Returns a {@link Set} of {@link IResource} instances that need to be rebuild in the context of the current
	 * <code>resource</code> and <code>kind</code>
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {
			resources.addAll(AopReferenceModelUtils.getAffectedFiles(kind, deltaKind, resource, context));
		}
		return resources;
	}

	/**
	 * Starts creation of AOP reference model by passing the Set of affectedResources on to a new instance of
	 * {@link AopReferenceModelBuilderJob}.
	 */
	public void build(Set<IResource> affectedResources, int kind, IProgressMonitor monitor) throws CoreException {
		monitor.subTask(Activator.getFormattedMessage("AopReferenceModelProjectBuilder.buildingAopReferenceModel"));
		if (affectedResources.size() > 0) {
			IProject project = context.get(IProject.class);
			if (kind == IncrementalProjectBuilder.CLEAN_BUILD || kind == IncrementalProjectBuilder.FULL_BUILD) {
				AopReferenceModelMarkerUtils.deleteProblemMarkers(project);
				((AopReferenceModel) Activator.getModel()).removeProject(JdtUtils.getJavaProject(project));
			}
			Job job = new AopReferenceModelBuilderJob(project, AopReferenceModelUtils
					.getAffectedFilesFromBeansConfig(affectedResources), affectedResources);
			job.schedule();
		}
		monitor.done();
	}

	/**
	 * Removes the AOP reference model markers
	 */
	public void cleanup(IResource resource, IProgressMonitor monitor) {
		try {
			monitor.subTask(Activator.getFormattedMessage("AopReferenceModelProjectBuilder.deletedProblemMarkers",
					resource.getFullPath()));
			AopReferenceModelMarkerUtils.deleteProblemMarkers(resource);

			// delete existing AOP references in case a build is disabled for
			// a certain project.
			IProject project = resource.getProject();
			if (JdtUtils.isJavaProject(project)) {
				Activator.getModel().removeProject(JdtUtils.getJavaProject(project));
			}
		}
		finally {
			monitor.done();
		}
	}

	public void setProjectContributorState(IProjectContributorState context) {
		this.context = context;
	}

}
