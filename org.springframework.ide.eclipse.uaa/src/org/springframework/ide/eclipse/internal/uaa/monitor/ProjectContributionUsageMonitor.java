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

/**
 * {@link IProjectContributionEventListener} implementation that captures executions of {@link IProjectBuilder}s and
 * {@link IValidator}s.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class ProjectContributionUsageMonitor extends ProjectContributionEventListenerAdapter {

	private IUaa manager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishProjectBuilder(ProjectBuilderDefinition contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		// Capture usage of a project builder only it really ran (affectResources > 0)
		if (affectedResources != null && affectedResources.size() > 0) {
			getUaa().registerFeatureUse(contributor.getNamespaceUri());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishValidator(ValidatorDefinition contributor, Set<IResource> affectedResources,
			IProgressMonitor monitor) {
		// Capture usage of a validator only it really ran (affectResources > 0)
		if (affectedResources != null && affectedResources.size() > 0) {
			getUaa().registerFeatureUse(contributor.getNamespaceUri());
		}
	}
	
	private synchronized IUaa getUaa() {
		if (manager == null) {
			manager = UaaPlugin.getUAA();
		}
		return manager;
	}
}
