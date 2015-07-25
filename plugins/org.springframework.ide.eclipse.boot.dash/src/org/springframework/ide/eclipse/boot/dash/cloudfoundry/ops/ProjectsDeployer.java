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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ManifestParser;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ProjectsDeployer extends Operation<Void> {

	private final CloudFoundryBootDashModel model;
	private final Map<IProject, BootDashElement> projectsToDeploy;
	private final CloudFoundryOperations client;
	private final UserInteractions ui;
	private final boolean shouldAutoReplaceApps;

	public ProjectsDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, Map<IProject, BootDashElement> projectsToDeploy, boolean shouldAutoReplaceApps) {

		super("Deploying Applications");

		this.model = cloudFoundryBootDashModel;
		this.projectsToDeploy = projectsToDeploy;
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
	}

	public ProjectsDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, List<IProject> projectsToDeploy, boolean shouldAutoReplaceApps) {

		super("Deploying Applications");

		this.projectsToDeploy = new LinkedHashMap<IProject, BootDashElement>();
		for (IProject project : projectsToDeploy) {
			this.projectsToDeploy.put(project, null);
		}

		this.model = cloudFoundryBootDashModel;
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
	}

	public Void runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		// First load all deployment properties

		List<CloudDeploymentProperties> toDeploy = prepareToDeploy(monitor);

		deploy(toDeploy, monitor);

		return null;
	}

	protected List<CloudDeploymentProperties> prepareToDeploy(IProgressMonitor monitor) throws Exception {
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
		return toDeploy;
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
			deploymentProperties = cloudElement.getDeploymentProperties();
		}

		IStatus status = Status.OK_STATUS;

		if (deploymentProperties == null) {
			status = BootDashActivator.createErrorStatus(null, "No deployment propreties found for " + project.getName()
					+ ". Please ensure that the project contains a valid manifest.yml or launch configuration.");
		} else {
			status = deploymentProperties.validate();
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}
		deploymentProperties.setShoudAutoReplace(shouldAutoReplaceApps);
		return deploymentProperties;
	}

	protected void deploy(List<CloudDeploymentProperties> toDeploy, IProgressMonitor monitor)
			throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor, toDeploy.size() * 10);
		for (CloudDeploymentProperties properties : toDeploy) {

			CloudApplicationOperation createOp = new CreateAndUpdateCloudAppOp(client, properties, model, ui);
			CloudApplicationOperation uploadOp = new UploadApplicationOperation(client, properties, model, ui);
			CloudApplicationOperation restartOp = new ApplicationStartOperation(properties.getAppName(), model, client,
					ui);

			List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
			deploymentOperations.add(createOp);
			deploymentOperations.add(uploadOp);
			deploymentOperations.add(restartOp);

			CloudApplicationOperation compositeOp = new CompositeCloudAppOp("Deploying " + properties.getAppName(),
					client, properties.getAppName(), model, ui, deploymentOperations);

			compositeOp.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));

			model.getCloudOpExecution().runOpSynch(compositeOp);

			subMonitor.worked(10);
		}
	}
}