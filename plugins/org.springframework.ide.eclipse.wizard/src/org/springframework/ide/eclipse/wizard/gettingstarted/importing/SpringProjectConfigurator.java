/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.gradle.core.api.IProjectConfigurationRequest;
import org.springsource.ide.eclipse.gradle.core.api.IProjectConfigurator;

/**
 * Configures Spring project for Gradle integration
 *
 * @author Alex Boyko
 *
 */
public class SpringProjectConfigurator implements IProjectConfigurator {

	private static final String SPRING_NAME = "spring-core"; //$NON-NLS-1$
	private static final String SPRING_GROUP = "org.springframework"; //$NON-NLS-1$

	public void configure(IProjectConfigurationRequest request, IProgressMonitor monitor) throws Exception {
		for (ExternalDependency dependency : request.getGradleModel().getClasspath()) {
			GradleModuleVersion moduleVersion = dependency.getGradleModuleVersion();
			if (SPRING_NAME.equals(moduleVersion.getName()) && SPRING_GROUP.equals(moduleVersion.getGroup())) {
				SpringCoreUtils.addProjectNature(request.getProject(), SpringCore.NATURE_ID, monitor);
			}
		}
	}

}