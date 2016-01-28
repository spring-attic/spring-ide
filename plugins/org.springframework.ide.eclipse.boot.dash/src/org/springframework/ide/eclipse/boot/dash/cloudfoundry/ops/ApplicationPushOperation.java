/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudZipApplicationArchive;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Operation that pushes an application's archive. The application must have an
 * accessible workspace project.
 *
 */
public class ApplicationPushOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;
	private final UserInteractions ui;

	public ApplicationPushOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, UserInteractions ui) {
		super("Uploading application: " + deploymentProperties.getAppName(), model, deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
		this.ui = ui;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		monitor.beginTask("Generating application archiving and uploading to Cloud Foundry", 10);
		// Must perform this check otherwise if the app does not exist
		// and an upload is attempted, CF backend or client may throw an
		// unintelligible
		// error that does not indicate that the app is missing (e.g. it does
		// not indicate 404 error)
		CloudAppInstances appInstances = model.getRunTarget().getClient().getExistingAppInstances(appName);

		monitor.worked(3);

		if (appInstances == null) {
			throw BootDashActivator.asCoreException(
					"Unable to upload application archive. Application does not exist anymore in Cloud Foundry: "
							+ deploymentProperties.getAppName());
		}

		// Upload the application
		CloudZipApplicationArchive archive = null;
		String appName = deploymentProperties.getAppName();

		try {

			if (deploymentProperties.getProject() != null) {

				ICloudApplicationArchiver archiver = getArchiver(monitor);
				if (archiver != null) {
					logAndUpdateMonitor("Generating archive for application: " + appName, monitor);
					archive = new CloudZipApplicationArchive(new ZipFile(archiver.getApplicationArchive(monitor)));
					monitor.worked(2);
				}
			}

			if (archive != null) {

				logAndUpdateMonitor("Uploading archive to Cloud Foundry for application: " + appName, monitor);

				model.getRunTarget().getClient().uploadApplication(appName, archive);

				monitor.worked(5);

				logAndUpdateMonitor("Archive uploaded to Cloud Foundry for application: " + appName, monitor);

			} else {
				throw BootDashActivator.asCoreException("Failed to generate application archive for " + appName
						+ ". Make sure that the project has a main type, or has an accessible archive file in manifest.yml.");

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

	protected ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
		try {
			for (CloudApplicationArchiverStrategy s : getArchiverStrategies(mon)) {
				ICloudApplicationArchiver a = s.getArchiver(mon);
				if (a != null) {
					return a;
				}
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	protected CloudApplicationArchiverStrategy[] getArchiverStrategies(IProgressMonitor mon) throws Exception {
		IProject project = deploymentProperties.getProject();

		ApplicationManifestHandler parser = new ApplicationManifestHandler(project,
				this.model.getRunTarget().getDomains(mon));

		return new CloudApplicationArchiverStrategy[] {
				CloudApplicationArchiverStrategies.fromManifest(project, appName, parser),
				CloudApplicationArchiverStrategies.packageAsJar(project, ui) };

	}

	// Use the 'Legacy' archiver
	// protected CloudApplicationArchiverStrategy[]
	// getArchiverStrategies(IProgressMonitor monitor) throws Exception {
	// IProject project = deploymentProperties.getProject();
	// ApplicationManifestHandler parser = new
	// ApplicationManifestHandler(project,
	// this.model.getCloudTarget().getDomains(requests, monitor));
	// ICloudApplicationArchiver legacyArchiver = new CloudApplicationArchiver(
	// JavaCore.create(deploymentProperties.getProject()), appName, parser);
	//
	//
	// return new CloudApplicationArchiverStrategy[] {
	// CloudApplicationArchiverStrategies.justReturn(legacyArchiver)
	// };
	// }

}
