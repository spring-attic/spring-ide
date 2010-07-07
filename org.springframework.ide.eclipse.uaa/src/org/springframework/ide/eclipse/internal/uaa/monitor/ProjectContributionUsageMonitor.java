/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;
import org.springframework.ide.eclipse.internal.uaa.UaaPlugin;

/**
 * {@link IProjectContributionEventListener} implementation that captures executions of {@link IProjectBuilder}s and
 * {@link IValidator}s.
 * @author Christian Dupuis
 * @since 2.3.3
 */
public class ProjectContributionUsageMonitor extends ProjectContributionEventListenerAdapter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {
		// Report usage of IProjectBuilders; only for those that are enabled for the project
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				UaaPlugin.getDefault().registerFeatureUse(builderDefinition.getNamespaceUri());
			}
		}
		
		// Report usage of IValidators; only for those that are enabled for the project
		for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
			if (validatorDefinition.isEnabled(project)) {
				UaaPlugin.getDefault().registerFeatureUse(validatorDefinition.getNamespaceUri());
			}
		}
	}

}
