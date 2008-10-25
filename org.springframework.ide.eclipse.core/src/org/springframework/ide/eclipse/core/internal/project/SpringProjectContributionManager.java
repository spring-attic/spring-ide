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
package org.springframework.ide.eclipse.core.internal.project;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorStateAware;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerFactory;

/**
 * Incremental project builder which implements the Strategy GOF pattern. For every modified
 * {@link IResource} within a Spring project all implementations of the interface
 * {@link IProjectBuilder} provided via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> and the interface {@link IValidator}
 * provided via the extension point <code>org.springframework.ide.eclipse.core.validators</code> are
 * called.
 * <p>
 * This {@link IncrementalProjectBuilder} makes state in form of an instance of
 * {@link IProjectContributorState} for the {@link IProjectContributor} accessible. This state
 * should be used to store arbitrary state object in order to save calculation time for subsequent
 * {@link IValidator} or {@link IProjectBuilder}.
 * <p>
 * {@link IProjectBuilder} or {@link IValidator} implementations that want to access the state
 * should implement the {@link IProjectContributorStateAware} interface to a call back with the
 * current state.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 * @see IProjectContributor
 * @see IProjectBuilder
 * @see IValidator
 * @see IProjectContributorState
 */
public class SpringProjectContributionManager extends IncrementalProjectBuilder {

	/**
	 * {@inheritDoc}
	 */
	protected final IProject[] build(final int kind, Map args, final IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		IResourceDelta delta = getDelta(project);

		List<ProjectBuilderDefinition> builderDefinitions = ProjectBuilderDefinitionFactory
				.getProjectBuilderDefinitions();
		List<ValidatorDefinition> validatorDefinitions = ValidatorDefinitionFactory
				.getValidatorDefinitions();
		List<IProjectContributionEventListener> listeners = ProjectContributionEventListenerFactory
				.getProjectContributionEventListeners();

		// Set up the state object
		IProjectContributorState state = prepareState(project, builderDefinitions,
				validatorDefinitions);

		// Fire start event on listeners
		for (IProjectContributionEventListener listener : listeners) {
			listener.start(kind, delta, builderDefinitions, validatorDefinitions, state, project);
		}

		// At first run all builders
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(builderDefinition
						.getProjectBuilder(), project, kind, delta);
				runBuilder(builderDefinition, affectedResources, kind, monitor, listeners);
			}
		}

		// Finally run all validators
		for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
			if (validatorDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(validatorDefinition
						.getValidator(), project, kind, delta);
				runValidator(validatorDefinition, affectedResources, monitor, listeners);
			}
		}

		// Fire end event on listeners
		for (IProjectContributionEventListener listener : listeners) {
			listener.finish(kind, delta, builderDefinitions, validatorDefinitions, state, project);
		}

		return null;
	}

	/**
	 * Collects all affected resources from the given {@link IResourceDelta} and
	 * {@link IProjectContributor}.
	 */
	private Set<IResource> getAffectedResources(IProjectContributor contributor, IProject project,
			int kind, IResourceDelta delta) throws CoreException {
		Set<IResource> affectedResources;
		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
			ResourceTreeVisitor visitor = new ResourceTreeVisitor(contributor);
			project.accept(visitor);
			affectedResources = visitor.getResources();
		}
		else {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(contributor, kind);
			delta.accept(visitor);
			affectedResources = visitor.getResources();
		}
		return affectedResources;
	}

	/**
	 * Instantiate the {@link IProjectContributorState} object. The state object is then passed to
	 * any {@link IProjectBuilder} and {@link IValidator} that implements the
	 * {@link IProjectContributorStateAware} interface.
	 * <p>
	 * This implementation creates an instance of {@link DefaultProjectContributorState}.
	 */
	private IProjectContributorState prepareState(IProject project,
			List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions) {

		IProjectContributorState context = new DefaultProjectContributorState();

		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)
					&& builderDefinition.getProjectBuilder() instanceof IProjectContributorStateAware) {
				((IProjectContributorStateAware) builderDefinition.getProjectBuilder())
						.setProjectContributorState(context);
			}
		}

		for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
			if (validatorDefinition.isEnabled(project)
					&& validatorDefinition.getValidator() instanceof IProjectContributorStateAware) {
				((IProjectContributorStateAware) validatorDefinition.getValidator())
						.setProjectContributorState(context);
			}
		}

		return context;
	}

	/**
	 * Runs all given {@link IProjectBuilder} in the order as they are given in the set.
	 */
	private void runBuilder(final ProjectBuilderDefinition builderDefinition,
			final Set<IResource> affectedResources, final int kind, final IProgressMonitor monitor,
			final List<IProjectContributionEventListener> listeners) {

		for (IProjectContributionEventListener listener : listeners) {
			listener.startContributor(builderDefinition.getProjectBuilder(), affectedResources);
		}

		ISafeRunnable code = new ISafeRunnable() {
			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}

			public void run() throws Exception {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor = new SubProgressMonitor(monitor, 1, 0);
				builderDefinition.getProjectBuilder().build(affectedResources, kind, subMonitor);
			}
		};
		SafeRunner.run(code);
	}

	/**
	 * Runs all given {@link IValidator} in the order as they are given in the set.
	 */
	private void runValidator(final ValidatorDefinition validatorDefinition,
			final Set<IResource> affectedResources, final IProgressMonitor monitor,
			final List<IProjectContributionEventListener> listeners) {

		for (IProjectContributionEventListener listener : listeners) {
			listener.startContributor(validatorDefinition.getValidator(), affectedResources);
		}

		ISafeRunnable code = new ISafeRunnable() {
			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}

			public void run() throws Exception {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor = new SubProgressMonitor(monitor, 1, 0);
				validatorDefinition.getValidator().validate(affectedResources, subMonitor);
			}
		};
		SafeRunner.run(code);
	}

	/**
	 * Default implementation of the {@link IProjectContributorState} interface.
	 */
	private class DefaultProjectContributorState implements IProjectContributorState {

		private Map<Class, Object> managedObjects = new HashMap<Class, Object>();

		@SuppressWarnings("unchecked")
		public <T> T get(Class<T> clazz) {
			return (T) managedObjects.get(clazz);
		}

		public boolean hold(Object obj) {
			if (managedObjects.containsKey(obj.getClass())) {
				return false;
			}
			else {
				managedObjects.put(obj.getClass(), obj);
				return true;
			}
		}

	}

	/**
	 * Create a list of affected resources from a resource delta.
	 */
	public static class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private IProjectContributor contributor;

		private int kind = -1;

		private Set<IResource> resources;

		public ResourceDeltaVisitor(IProjectContributor builder, int kind) {
			this.contributor = builder;
			this.resources = new LinkedHashSet<IResource>();
			this.kind = kind;
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
				if (visitChildren) {
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta
							.getKind()));
				}
			}
			else if (resource instanceof IFolder) {
				resources
						.addAll(contributor.getAffectedResources(resource, kind, aDelta.getKind()));
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta
							.getKind()));
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta
							.getKind()));
					break;
				}
			}
			return visitChildren;
		}
	}

	/**
	 * Create a list of affected resources from a resource tree.
	 */
	public static class ResourceTreeVisitor implements IResourceVisitor {

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
				resources.addAll(contributor.getAffectedResources(resource,
						IncrementalProjectBuilder.FULL_BUILD, IResourceDelta.CHANGED));
			}
			else if (resource instanceof IProject) {
				resources.addAll(contributor.getAffectedResources(resource,
						IncrementalProjectBuilder.FULL_BUILD, IResourceDelta.CHANGED));
			}
			return true;
		}
	}
}
