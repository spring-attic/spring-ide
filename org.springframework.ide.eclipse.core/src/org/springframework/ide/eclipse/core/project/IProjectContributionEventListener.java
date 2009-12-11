/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;

/**
 * Listener interface that can be implemented by clients who want to receive callbacks on certain events fired by the
 * project contribution infrastructure.
 * <p>
 * An instance of this interface can be contributed using the listeners extension point:
 * <p>
 * 
 * <pre>
 *   &lt;extension
 *          point=&quot;org.springframework.ide.eclipse.core.listeners&quot;&gt;
 *       &lt;listener
 *             class=&quot;org.springframework.ide.eclipse.core.java.TypeStructureStateRegisteringEventListener&quot;&gt;
 *       &lt;/listener&gt;
 *    &lt;/extension&gt;
 * </pre>
 * @author Christian Dupuis
 * @since 2.2.0
 */
public interface IProjectContributionEventListener {
	
	/**
	 * Signals that {@link IProjectContributor}s are about to be executed.
	 * @param kind one of {@link IncrementalProjectBuilder#AUTO_BUILD}, {@link IncrementalProjectBuilder#CLEAN_BUILD},
	 * {@link IncrementalProjectBuilder#FULL_BUILD} or {@link IncrementalProjectBuilder#INCREMENTAL_BUILD}
	 * @param delta the changed resources that triggered the build
	 * @param builderDefinitions the contributed project builder extensions
	 * @param validatorDefinitions the contributed validator extensions
	 * @param state the state object
	 * @param project the current project
	 * @param monitor the monitor to report progress against
	 */
	void start(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project,
			IProgressMonitor monitor);

	/**
	 * Signals that the given {@link IProjectContributor} is about to be executed
	 * @param contributor the contributor to start
	 * @param affectedResources the resources that are affected
	 * @param monitor the monitor to report progress against
	 */
	void startContributor(IProjectContributor contributor, Set<IResource> affectedResources, IProgressMonitor monitor);

	/**
	 * Signals that {@link IProjectContributor}s are done executing.
	 * @param kind one of {@link IncrementalProjectBuilder#AUTO_BUILD}, {@link IncrementalProjectBuilder#CLEAN_BUILD},
	 * {@link IncrementalProjectBuilder#FULL_BUILD} or {@link IncrementalProjectBuilder#INCREMENTAL_BUILD}
	 * @param delta the changed resources that triggered the build
	 * @param builderDefinitions the contributed project builder extensions
	 * @param validatorDefinitions the contributed validator extensions
	 * @param state the state object
	 * @param project the current project
	 * @param monitor the monitor to report progress against
	 */
	void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project,
			IProgressMonitor monitor);

	/**
	 * Signals that the given {@link IProjectContributor} is done executing
	 * @param contributor the contributor has is done executing
	 * @param affectedResources the resources that are affected
	 * @param monitor the monitor to report progress against
	 */
	void finishContributor(IProjectContributor contributor, Set<IResource> affectedResources, IProgressMonitor monitor);

}
