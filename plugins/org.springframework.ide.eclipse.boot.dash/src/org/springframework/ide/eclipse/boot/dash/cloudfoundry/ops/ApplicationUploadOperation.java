/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudZipApplicationArchive;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ManifestParser;

/**
 * Operation that uploads an application's archive. The application must have an
 * accessible workspace project.
 *
 */
public class ApplicationUploadOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;

	public ApplicationUploadOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model) {
		super("Uploading application: " + deploymentProperties.getAppName(), model, deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		SubMonitor subMonitor = SubMonitor.convert(monitor);

		// Must perform this check otherwise if the app does not exist
		// and an upload is attempted, CF backend or client may throw an
		// unintelligible
		// error that does not indicate that the app is missing (e.g. it does
		// not indicate 404 error)
		CloudApplication app = getCloudApplication();
		if (app == null) {
			throw BootDashActivator.asCoreException(
					"Unable to upload application archive. Application does not exist anymore in Cloud Foundry: "
							+ deploymentProperties.getAppName());
		}

		// Upload the application
		CloudZipApplicationArchive archive = null;
		String appName = deploymentProperties.getAppName();

		try {

			if (deploymentProperties.getProject() != null) {

				ManifestParser parser = new ManifestParser(deploymentProperties.getProject(),
						this.model.getCloudTarget().getDomains(subMonitor));

				CloudApplicationArchiver archiver = new CloudApplicationArchiver(
						JavaCore.create(deploymentProperties.getProject()), appName, parser);

				subMonitor.setTaskName("Generating archive for application: " + appName);

				archive = archiver.getApplicationArchive(subMonitor.newChild(50));
			}

			if (archive != null) {

				subMonitor.setTaskName("Uploading archive to Cloud Foundry for application: " + appName);

				getClient().uploadApplication(appName, archive);

				subMonitor.setTaskName("Archive uploaded to Cloud Foundry for application: " + appName);

			} else {
				throw BootDashActivator.asCoreException("Failed to generate application archive for " + appName
						+ ". Make sure the project is accessible and it can be archived for deployment, or it points to an archive in a manifest.yml file.");

			}
		} finally {
			// IMPORTANT: MUST close the archive to avoid resource
			// leakage and
			// potential bug were the same archive file keeps being
			// pushes even if the
			// archive file changes
			if (archive != null) {
				archive.close();
			}
		}
	}

}
