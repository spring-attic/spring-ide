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

import java.util.ArrayList;

import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.base.Objects;

/**
 * Updates deployment properties (e.g. memory, URL, instances) of an existing
 * Cloud Application.
 * <p/>
 * This does NOT upload application sources (i.e the archive). This is meant to
 * be a separate operation so that users can scale their application or change
 * URLs mappings without also pushing the application archive which is a slow
 * process.
 */
public class ApplicationPropertiesUpdateOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;

	public ApplicationPropertiesUpdateOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, CancelationToken cancelationToken) {
		super("Updating application deployment properties - " + deploymentProperties.getAppName(), model,
				deploymentProperties.getAppName(), cancelationToken);
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (deploymentProperties == null) {
			throw ExceptionUtil.coreException("No deployment properties for application - " + appName
					+ " found. Unable to update the application");
		}

		if (updateExistingApplicationInCloud(deploymentProperties, monitor)) {
			CloudAppDashElement element = getDashElement();
			CloudAppInstances existingAppInstances = model.getRunTarget().getClient().getExistingAppInstances(appName);
			element.setInstanceData(existingAppInstances);
			checkTerminationRequested(monitor);
		}
	}

	protected boolean updateExistingApplicationInCloud(CloudApplicationDeploymentProperties properties,
			IProgressMonitor monitor) throws Exception {

		CFApplication app = model.getRunTarget().getClient().getApplication(appName);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		boolean updated = false;

		if (app != null) {
			String appName = properties.getAppName();
			if (properties.getEnvironmentVariables() != null
					&& !properties.getEnvironmentVariables().equals(app.getEnvAsMap())) {
				subMonitor.setTaskName("Updating " + appName + " environment variables.");

				model.getRunTarget().getClient().updateApplicationEnvironment(appName, properties.getEnvironmentVariables());
				updated = true;

				subMonitor.worked(1);
			}

			if (!Objects.equal(properties.getBuildpack(), app.getBuildpackUrl())
					|| !Objects.equal(properties.getTimeout(), app.getTimeout())
					|| !Objects.equal(properties.getCommand(), app.getCommand())
					|| !Objects.equal(properties.getStack(), app.getStack())) {
				subMonitor.setTaskName("Updating " + appName + " staging data.");

				model.getRunTarget().getClient().updateApplicationStaging(appName, new Staging(properties.getCommand(),
						properties.getBuildpack(), properties.getStack(), properties.getTimeout()));
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getServices() != null && !properties.getServices().equals(app.getServices())) {
				subMonitor.setTaskName("Updating " + appName + " bound services.");

				model.getRunTarget().getClient().updateApplicationServices(appName, properties.getServices());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getMemory() > 0 && properties.getMemory() != app.getMemory()) {
				subMonitor.setTaskName("Updating " + appName + " memory.");

				model.getRunTarget().getClient().updateApplicationMemory(appName, properties.getMemory());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getDiskQuota() > 0 && properties.getDiskQuota() != app.getDiskQuota()) {
				subMonitor.setTaskName("Updating " + appName + " disk quota.");

				model.getRunTarget().getClient().updateApplicationDiskQuota(appName, properties.getDiskQuota());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getInstances() > 0 && properties.getInstances() != app.getInstances()) {
				subMonitor.setTaskName("Updating " + appName + " instances.");

				model.getRunTarget().getClient().updateApplicationInstances(appName, properties.getInstances());
				updated = true;

				subMonitor.worked(1);
			}

			if (properties.getUris() != null && !properties.getUris().equals(app.getUris())) {

				subMonitor.setTaskName("Updating " + appName + " mapped URLs.");

				model.getRunTarget().getClient().updateApplicationUris(appName, new ArrayList<>(properties.getUris()));
				updated = true;

				subMonitor.worked(1);
			}
		}
		return updated;
	}

}
