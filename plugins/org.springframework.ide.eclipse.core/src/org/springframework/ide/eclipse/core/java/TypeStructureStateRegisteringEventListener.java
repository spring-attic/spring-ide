/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.core.Ordered;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.project.SpringProjectContributionManager.ResourceDeltaVisitor;
import org.springframework.ide.eclipse.core.internal.project.SpringProjectContributionManager.ResourceTreeVisitor;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;
import org.springframework.util.ClassUtils;

/**
 * {@link IProjectContributionEventListener} implementation that manages the lifecycle of the {@link TypeStructureCache}.
 * @author Christian Dupuis
 * @since 2.2.0
 * @see #updateTypeStructures(int,IProject,IResourceDelta)
 */
public class TypeStructureStateRegisteringEventListener extends ProjectContributionEventListenerAdapter implements Ordered {

	/**
	 * {@inheritDoc}
	 */
	public void start(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {
		state.hold(new TypeStructureState());
	}

	/**
	 * {@inheritDoc}
	 */
	public void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {
		try {
			// Update type structures at the end of each build
			updateTypeStructures(kind, project, delta);
		}
		catch (CoreException e) {
		}
	}

	/**
	 * Updates the type structures for a given project.s
	 */
	private void updateTypeStructures(int kind, IProject project, IResourceDelta delta) throws CoreException {
		// Record type structures
		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD
				|| kind == IncrementalProjectBuilder.CLEAN_BUILD) {
			// 1. full build (does not matter if state (1a) or not (1b))
			recoredFullTypeStructures(project);
		}
		else {
			// 2. incremental build and no state
			if (!SpringCore.getTypeStructureCache().hasRecordedTypeStructures(project)) {
				recoredFullTypeStructures(project);
			}
			// 3. incremental build and state
			else {
				JavaResourceRecordingProjectContributor contributor = new JavaResourceRecordingProjectContributor();
				ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(contributor, kind);
				delta.accept(visitor);
				SpringCore.getTypeStructureCache().recordTypeStructures(project,
						visitor.getResources().toArray(new IResource[visitor.getResources().size()]));
			}
		}
	}

	/**
	 * Records a complete snapshot of the type structure of all java class files in the given project.
	 */
	private void recoredFullTypeStructures(IProject project) throws CoreException {
		// remove pre-existing state as we are doing a clean build
		SpringCore.getTypeStructureCache().clearStateForProject(project);

		// collect all java class files
		JavaResourceRecordingProjectContributor contributor = new JavaResourceRecordingProjectContributor();
		ResourceTreeVisitor visitor = new ResourceTreeVisitor(contributor);
		project.accept(visitor);

		// record type strcutures for all collected class files
		SpringCore.getTypeStructureCache().recordTypeStructures(project,
				visitor.getResources().toArray(new IResource[visitor.getResources().size()]));
	}

	/**
	 * Passes through any java class file.
	 */
	private class JavaResourceRecordingProjectContributor implements IProjectContributor {

		public void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException {
			// nothing to do
		}

		public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
			if (resource.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX) && JavaCore.create(resource) != null) {
				Set<IResource> resources = new HashSet<IResource>();
				resources.add(resource);
				return resources;
			}
			return Collections.emptySet();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
