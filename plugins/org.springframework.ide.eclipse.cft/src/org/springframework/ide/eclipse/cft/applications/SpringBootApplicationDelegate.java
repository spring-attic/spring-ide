/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft.applications;

import org.eclipse.cft.server.core.CFApplicationArchive;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.application.ModuleResourceApplicationDelegate;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;

public class SpringBootApplicationDelegate extends ModuleResourceApplicationDelegate {

	// Add the archivers in the order that they should be invoked
	private IntegrationApplicationArchiver[] orderedArchivers = new IntegrationApplicationArchiver[] {
			new BootDashboardArchiver(),

			new LegacyCFTJavaArchiver() };

	@Override
	public boolean requiresURL() {
		// Standalone apps by default do not require URL.
		return false;
	}

	@Override
	public boolean shouldSetDefaultUrl(CloudFoundryApplicationModule appModule) {
		for (IntegrationApplicationArchiver archiver : orderedArchivers) {
			if (archiver.supports(appModule)) {
				return archiver.shouldSetDefaultUrl(appModule);
			}
		}
		return false;
	}

	@Override
	public CFApplicationArchive getApplicationArchive(IModule module, IServer server, IModuleResource[] moduleResources,
			IProgressMonitor monitor) throws CoreException {
		CloudFoundryApplicationModule appModule = getCloudFoundryApplicationModule(module, server);
		CloudFoundryServer cloudServer = getCloudServer(server);

		for (IntegrationApplicationArchiver archiver : orderedArchivers) {
			if (archiver.supports(appModule)) {
				// If archive is not generated, find the next archiver that
				// supports this module
				CFApplicationArchive archive = archiver.getApplicationArchive(appModule, cloudServer, moduleResources,
						monitor);
				if (archive != null) {
					return archive;
				}
			}
		}

		return null;
	}
}
