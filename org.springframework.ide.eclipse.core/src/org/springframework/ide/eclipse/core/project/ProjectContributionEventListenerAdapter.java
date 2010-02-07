/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;

/**
 * Adapter implementation of {@link IProjectContributionEventListener} that allows sub classes only
 * override method that they are interested in.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class ProjectContributionEventListenerAdapter implements IProjectContributionEventListener {
	
	/**
	 * {@inheritDoc}
	 */
	public void finish(int kind, IResourceDelta delta,
			List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state,
			IProject project) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project,
			IProgressMonitor monitor) {
		finish(kind, delta, builderDefinitions, validatorDefinitions, state, project);
	}

	/**
	 * {@inheritDoc}
	 */
	public void finishContributor(IProjectContributor contributor, Set<IResource> affectedResources) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void finishContributor(IProjectContributor contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		finishContributor(contributor, affectedResources);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void start(int kind, IResourceDelta delta,
			List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state,
			IProject project) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void start(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project,
			IProgressMonitor monitor) {
		start(kind, delta, builderDefinitions, validatorDefinitions, state, project);
	}

	/**
	 * {@inheritDoc}
	 */
	public void startContributor(IProjectContributor contributor, Set<IResource> affectedResources) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void startContributor(IProjectContributor contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		startContributor(contributor, affectedResources);
	}

}
