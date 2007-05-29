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
package org.springframework.ide.eclipse.core.internal.project;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;

/**
 * Incremental project builder which implements the Strategy GOF pattern. For
 * every modified {@link IResource} within a Spring project all implementations
 * of the interface {@link IProjectBuilder} provided via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> and the
 * interface {@link IValidator} provided via the extension point
 * <code>org.springframework.ide.eclipse.core.validators</code> are called.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringProjectContributionManager extends IncrementalProjectBuilder {

	protected final IProject[] build(final int kind, Map args,
			final IProgressMonitor monitor) throws CoreException {
		final IProject project = getProject();
		final IResourceDelta delta = getDelta(project);

		// At first run all builders
		for (final ProjectBuilderDefinition builderDefinition
				: ProjectBuilderDefinitionFactory
						.getProjectBuilderDefinitions()) {
			if (builderDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(
						builderDefinition.getProjectBuilder(), project, kind,
						delta);
				runBuilder(builderDefinition, affectedResources, kind, monitor);
			}
		}

		// Finally run all validators
		for (final ValidatorDefinition validatorDefinition
				: ValidatorDefinitionFactory.getValidatorDefinitions()) {
			if (validatorDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(
						validatorDefinition.getValidator(), project, kind,
						delta);
				runValidator(validatorDefinition, affectedResources, monitor);
			}
		}
		return null;
	}

	private void runBuilder(final ProjectBuilderDefinition builderDefinition,
			final Set<IResource> affectedResources, final int kind,
			final IProgressMonitor monitor) {
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						1);
				subMonitor = new SubProgressMonitor(monitor, 1);
				builderDefinition.getProjectBuilder().build(affectedResources,
						kind, subMonitor);
			}

			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}
		};
		SafeRunner.run(code);
	}

	private void runValidator(final ValidatorDefinition validatorDefinition,
			final Set<IResource> affectedResources,
			final IProgressMonitor monitor) {
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						1);
				subMonitor = new SubProgressMonitor(monitor, 1);
				validatorDefinition.getValidator().validate(affectedResources,
						subMonitor);
			}

			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}
		};
		SafeRunner.run(code);
	}

	private Set<IResource> getAffectedResources(IProjectContributor contributor,
			IProject project, int kind, IResourceDelta delta)
			throws CoreException {
		Set<IResource> affectedResources;
		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
			ResourceTreeVisitor visitor = new ResourceTreeVisitor(contributor);
			project.accept(visitor);
			affectedResources = visitor.getResources();
		}
		else {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(
					contributor);
			delta.accept(visitor);
			affectedResources = visitor.getResources();
		}
		return affectedResources;
	}

	/**
	 * Create a list of affected resources from a resource tree.
	 */
	private class ResourceTreeVisitor implements IResourceVisitor {

		private IProjectContributor contributor;
		private Set<IResource> resources;

		public ResourceTreeVisitor(IProjectContributor builder) {
			this.contributor = builder;
			this.resources = new LinkedHashSet<IResource>();
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				resources.addAll(contributor.getAffectedResources(resource, 0));
			}
			return true;
		}
	}

	/**
	 * Create a list of affected resources from a resource delat.
	 */
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private IProjectContributor contributor;
		private Set<IResource> resources;

		public ResourceDeltaVisitor(IProjectContributor builder) {
			this.contributor = builder;
			this.resources = new LinkedHashSet<IResource>();
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResourceDelta aDelta) throws CoreException {
			boolean visitChildren = false;

			IResource resource = aDelta.getResource();
			if (resource instanceof IProject) {

				// Only check projects with Spring beans nature
				visitChildren = SpringCoreUtils.isSpringProject(resource);
			}
			else if (resource instanceof IFolder) {
				resources.addAll(contributor.getAffectedResources(resource,
						aDelta.getKind()));
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					resources.addAll(contributor.getAffectedResources(resource,
							aDelta.getKind()));
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					resources.addAll(contributor.getAffectedResources(resource,
							aDelta.getKind()));
					break;
				}
			}
			return visitChildren;
		}
	}
}
