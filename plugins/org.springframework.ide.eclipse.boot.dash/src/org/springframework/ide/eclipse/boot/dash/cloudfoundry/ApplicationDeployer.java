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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ApplicationDeployer extends Operation<Void> {

	private final CloudFoundryBootDashModel model;
	private final Map<IProject, BootDashElement> projectsToDeploy;
	private final CloudFoundryOperations client;
	private final UserInteractions ui;
	private final boolean shouldAutoReplaceApps;
	private final OperationsExecution opExecution;

	public ApplicationDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, Map<IProject, BootDashElement> projectsToDeploy, boolean shouldAutoReplaceApps,
			OperationsExecution opExecution) {

		super("Deploying Applications");

		this.model = cloudFoundryBootDashModel;
		this.projectsToDeploy = projectsToDeploy;
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
		this.opExecution = opExecution;
	}

	public ApplicationDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, List<IProject> projectsToDeploy, boolean shouldAutoReplaceApps,
			OperationsExecution opExecution) {

		super("Deploying Applications");

		this.model = cloudFoundryBootDashModel;

		this.projectsToDeploy = new LinkedHashMap<IProject, BootDashElement>();
		for (IProject project : projectsToDeploy) {
			this.projectsToDeploy.put(project, null);
		}
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
		this.opExecution = opExecution;

	}

	public Void runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		// First load all deployment properties
		SubMonitor subMonitor = SubMonitor.convert(monitor, projectsToDeploy.size() * 100);

		final List<CloudDeploymentProperties> toDeploy = new ArrayList<CloudDeploymentProperties>();

		for (Iterator<Entry<IProject, BootDashElement>> it = projectsToDeploy.entrySet().iterator(); it.hasNext();) {
			Entry<IProject, BootDashElement> entry = it.next();

			CloudDeploymentProperties deploymentProperties = getDeploymentProperties(entry.getKey(), entry.getValue(),
					monitor);

			if (deploymentProperties == null) {
				StringWriter errorMessage = new StringWriter();
				errorMessage.append("Failed to deploy project: ");
				errorMessage.append(entry.getKey().getName());
				errorMessage.append(" - ");
				errorMessage.append(
						" No Cloud deployment properties resolved from either manifest.yml or launch configuration. Please either one exists and is valid. ");

				throw BootDashActivator.asCoreException(errorMessage.toString());
			}

			toDeploy.add(deploymentProperties);

		}

		deployInParallel(toDeploy, subMonitor);

		return null;
	}

	protected CloudDeploymentProperties getDeploymentProperties(IProject project, BootDashElement element,
			IProgressMonitor monitor) throws Exception {

		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		CloudDeploymentProperties deploymentProperties = null;

		List<CloudDomain> domains = client.getDomains();

		ManifestParser parser = new ManifestParser(project, domains);

		if (parser.hasManifest()) {
			deploymentProperties = parser.load(subMonitor.newChild(100));

		} else if (element instanceof CloudDashElement) {
			CloudDashElement cloudElement = (CloudDashElement) element;

			try {
				deploymentProperties = cloudElement.refreshDeploymentProperties(subMonitor);

			} catch (Throwable t) {
				// ignore...app may not exist anymore
			}

		}

		IStatus status = Status.OK_STATUS;

		if (deploymentProperties == null) {
			status = BootDashActivator.createErrorStatus(null,
					"No deployment propreties found. Please ensure the project contains a valid manifest.yml or launch configuration.");
		} else {
			status = deploymentProperties.validate();
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}
		deploymentProperties.setShoudAutoReplace(shouldAutoReplaceApps);
		return deploymentProperties;
	}

	protected void deployInParallel(List<CloudDeploymentProperties> toDeploy, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, toDeploy.size() * 10);
		for (CloudDeploymentProperties properties : toDeploy) {
			// Chain operations: 1. create/update app 2. upload archive 3.
			// restart app
			Operation<?> createOp = new CreateAndUpdateCloudAppOp(client, properties, model, ui);
			Operation<?> uploadOp = new UploadApplicationOperation(client, properties, model, ui);
			Operation<?> restartOp = new ApplicationStartOperation(properties.getAppName(), model, client, ui);

			opExecution.runAsOneOp(new Operation<?>[]{createOp, uploadOp, restartOp},
					"Deploying: " + properties.getAppName());
			subMonitor.worked(10);
		}
	}
}