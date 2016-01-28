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

import java.util.LinkedHashSet;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.LocalRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * Adds an element to the Cloud Foundry boot dash model for a give
 * {@link CloudApplication}. If the {@link CloudApplication} does not exist, it
 * will create the application first before adding the element to the model
 *
 */
public class AddElementOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;
	private RunState preferedRunState;
	private CloudApplication existingApplication;

	/**
	 *
	 * @param deploymentProperties
	 * @param model
	 *            model where Cloud Foundry element should be added
	 * @param existingApplication
	 *            Cloud Application if app alreay exists in CF, otherwise null.
	 * @param preferedRunState
	 *            the state that the app should be in when the element is created. For
	 *            example "STARTING". This overrides the actual runstate of the app in CF, if the app already
	 *            exists
	 */
	public AddElementOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, CloudApplication existingApplication, RunState preferedRunState) {
		super("Deploying application: " + deploymentProperties.getAppName(), model, deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
		this.existingApplication = existingApplication;
		this.preferedRunState = preferedRunState;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		CloudAppInstances existingInstances = null;
		if (existingApplication == null) {
			existingInstances = createApplication(monitor);
		} else {
			existingInstances = model.getRunTarget().getClient().getExistingAppInstances(existingApplication.getMeta().getGuid());
		}

		monitor.worked(20);

		if (existingInstances == null) {
			throw ExceptionUtil.coreException("Failed to create a Cloud application for : "
					+ deploymentProperties.getAppName() + ". Please try to redeploy again or check your connection.");
		}

		IProject project = deploymentProperties.getProject();

		if (preferedRunState == null) {
			preferedRunState = ApplicationRunningStateTracker.getRunState(existingInstances);
		}

		CloudAppDashElement cde = this.model.addElement(existingInstances, project, preferedRunState);
		if (project != null && cde != null) {
			BootDashElement localElement = findLocalBdeForProject(project);
			if (localElement != null) {
				copyTags(localElement, cde);
			}
			// Persist the manifest path when creating the bde
			cde.setDeploymentManifestFile(deploymentProperties.getManifestFile());
			String hc = cde.getTarget().getHealthCheckSupport().getHealthCheck(cde.getAppGuid());
			cde.setHealthCheck(hc);
		}
	}

	private static BootDashElement findLocalBdeForProject(IProject project) {
		BootDashModel localModel = BootDashActivator.getDefault().getModel()
				.getSectionByTargetId(LocalRunTarget.INSTANCE.getId());
		if (localModel != null) {
			for (BootDashElement bde : localModel.getElements().getValue()) {
				if (project.equals(bde.getProject())) {
					return bde;
				}
			}
		}
		return null;
	}

	private static void copyTags(BootDashElement sourceBde, BootDashElement targetBde) {
		LinkedHashSet<String> tagsToCopy = sourceBde.getTags();
		if (tagsToCopy != null && !tagsToCopy.isEmpty()) {
			LinkedHashSet<String> targetTags = targetBde.getTags();
			for (String tag : tagsToCopy) {
				targetTags.add(tag);
			}
			targetBde.setTags(targetTags);
		}
	}

	protected CloudAppInstances createApplication(IProgressMonitor monitor) throws Exception {

		monitor.beginTask("Creating application: " + deploymentProperties.getAppName(), 10);
		try {

			logAndUpdateMonitor("Creating application: " + deploymentProperties.getAppName(), monitor);
			model.getRunTarget().getClient().createApplication(deploymentProperties);
			monitor.worked(5);

		} catch (Exception e) {
			// Clean-up: If app creation failed, check if the app was created
			// anyway
			// and delete it to allow users to redeploy
			CloudApplication toCleanUp = model.getRunTarget().getClient().getApplication(deploymentProperties.getAppName());
			if (toCleanUp != null) {
				model.getRunTarget().getClient().deleteApplication(toCleanUp.getName());
			}
			throw e;
		}
		// Fetch the created Cloud Application
		logAndUpdateMonitor(
				"Verifying that the application was created successfully: " + deploymentProperties.getAppName(),
				monitor);
		CloudAppInstances instances = model.getRunTarget().getClient().getExistingAppInstances(deploymentProperties.getAppName());
		monitor.worked(5);

		return instances;
	}

}