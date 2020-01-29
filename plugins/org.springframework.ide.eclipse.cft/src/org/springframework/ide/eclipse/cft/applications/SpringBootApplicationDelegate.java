/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft.applications;

import org.eclipse.cft.server.core.CFApplicationArchive;
import org.eclipse.cft.server.core.internal.application.ModuleResourceApplicationDelegate;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.springframework.ide.eclipse.cft.CftUiInteractions;
import org.springframework.ide.eclipse.cft.ProjectUtils;

public class SpringBootApplicationDelegate extends ModuleResourceApplicationDelegate {

	@Override
	public boolean requiresURL() {
		// Standalone apps by default do not require URL.
		return false;
	}

	@Override
	public boolean suggestUrl(CloudFoundryApplicationModule appModule) {
		return ProjectUtils.isSpringBootProject(appModule);
	}

	@Override
	public CFApplicationArchive getApplicationArchive(IModule module, IServer server, IModuleResource[] moduleResources,
			IProgressMonitor monitor) throws CoreException {
		return new BootDashboardArchiver(getCftUiInteractions()).getApplicationArchive(module, server, moduleResources, monitor);
	}

	private CftUiInteractions getCftUiInteractions() {
		return new CftUiInteractions();
	}
}
