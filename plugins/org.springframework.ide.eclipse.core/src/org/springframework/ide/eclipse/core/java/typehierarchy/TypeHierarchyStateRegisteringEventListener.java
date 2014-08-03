/*******************************************************************************
 * Copyright (c) 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.springframework.core.Ordered;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;

/**
 * {@link IProjectContributionEventListener} implementation that manages the build-related lifecycle of the {@link TypeHierarchyEngine}.
 * @author Martin Lippert
 * @since 3.6.1
 */
public class TypeHierarchyStateRegisteringEventListener extends ProjectContributionEventListenerAdapter implements Ordered {

	/**
	 * {@inheritDoc}
	 */
	public void start(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {

		System.out.println("type hierarchy engine created");

		TypeHierarchyEngine engine = new TypeHierarchyEngine(false);
		engine.setClassReaderFactory(new BytecodeTypeHierarchyClassReaderFactory());
		engine.setTypeHierarchyElementCacheFactory(new DirectTypeHierarchyElementCacheFactory());
		state.hold(engine);
	}

	/**
	 * {@inheritDoc}
	 */
	public void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {
		TypeHierarchyEngine engine = state.get(TypeHierarchyEngine.class);
		engine.cleanup();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
