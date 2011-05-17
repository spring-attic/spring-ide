/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.project.uaa;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.util.ClassUtils;

/**
 * {@link IProjectContributionEventListener} implementation that captures executions of {@link IProjectBuilder}s and
 * {@link IValidator}s.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class ProjectContributionUsageMonitor extends ProjectContributionEventListenerAdapter {
	
	private static final boolean UAA_AVAILABLE = ClassUtils.isPresent("org.springframework.ide.eclipse.uaa.UaaPlugin", 
			ProjectContributionEventListenerAdapter.class.getClassLoader());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishProjectBuilder(ProjectBuilderDefinition contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		if (UAA_AVAILABLE) UaaDependentProjectContributionUsageMonitor.finishProjectBuilder(contributor, affectedResources, monitor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishValidator(ValidatorDefinition contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		if (UAA_AVAILABLE) UaaDependentProjectContributionUsageMonitor.finishValidator(contributor, affectedResources, monitor);
	}
	
	private static class UaaDependentProjectContributionUsageMonitor {

		private static IUaa manager;

		public static void finishProjectBuilder(ProjectBuilderDefinition contributor, Set<IResource> affectedResources,
				IProgressMonitor monitor) {
			// Capture usage of a project builder only it really ran (affectResources > 0)
			if (affectedResources != null && affectedResources.size() > 0) {
				getUaa().registerFeatureUse(contributor.getNamespaceUri(),
						Collections.singletonMap("name", contributor.getName()));
			}
		}

		public static void finishValidator(ValidatorDefinition contributor, Set<IResource> affectedResources,
				IProgressMonitor monitor) {
			// Capture usage of a validator only it really ran (affectResources > 0)
			if (affectedResources != null && affectedResources.size() > 0) {
				getUaa().registerFeatureUse(contributor.getNamespaceUri(),
						Collections.singletonMap("name", contributor.getName()));
			}
		}

		private static synchronized IUaa getUaa() {
			if (manager == null) {
				manager = UaaPlugin.getUAA();
			}
			return manager;
		}
	}
}
