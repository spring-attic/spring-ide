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
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.cft.server.standalone.core.internal.application.CloudFoundryArchiverRegistry;
import org.eclipse.cft.server.standalone.core.internal.application.ICloudFoundryArchiver;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.springframework.ide.eclipse.cft.ProjectUtils;

public class LegacyCFTJavaArchiver extends AbstractApplicationArchiver {

	@Override
	protected CFApplicationArchive generateArchive(CloudFoundryApplicationModule appModule,
			CloudFoundryServer cloudServer, IProject project, IProgressMonitor monitor) throws CoreException {
		printToConsole(appModule, cloudServer, "Using CFT framework standalone application project archiver");
		ICloudFoundryArchiver archiver = CloudFoundryArchiverRegistry.INSTANCE.createArchiver(appModule, cloudServer);
		return archiver.getApplicationArchive(monitor);
	}

	@Override
	public boolean supports(IModule module) {
		// Supports any Java project
		return ProjectUtils.isJavaProject(module);
	}

	@Override
	public boolean shouldSetDefaultUrl(IModule module) {
		// Only if it is Spring Boot. Note: Legacy CFT archiving may also handle
		// Spring Boot apps
		return ProjectUtils.isSpringBootProject(module);
	}

}
