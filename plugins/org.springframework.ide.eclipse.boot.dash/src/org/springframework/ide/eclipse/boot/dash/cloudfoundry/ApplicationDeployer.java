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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class ApplicationDeployer {

	final CloudFoundryBootDashModel model;
	private final List<IJavaProject> projectsToDeploy;
	final CloudFoundryOperations client;
	final UserInteractions ui;

	ApplicationDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, List<IJavaProject> projectsToDeploy) {

		this.model = cloudFoundryBootDashModel;
		this.projectsToDeploy = projectsToDeploy;
		this.client = client;
		this.ui = ui;
	}

	public void deployAndStart(IProgressMonitor monitor) throws Exception {
		// First load all deployment properties
		SubMonitor subMonitor = SubMonitor.convert(monitor, projectsToDeploy.size() * 100);

		boolean proceedIfError = true;

		final List<CloudDeploymentProperties> toDeploy = new ArrayList<CloudDeploymentProperties>();

		List<CloudDomain> domains  = client.getDomains();

		for (Iterator<IJavaProject> it = projectsToDeploy.iterator(); proceedIfError && it.hasNext();) {
			IJavaProject javaProject = it.next();
			ManifestParser parser = new ManifestParser(javaProject.getProject(), domains);

			subMonitor.setTaskName("Loading deployment properties for project: " + javaProject.getProject().getName());

			CloudDeploymentProperties deploymentProperties = null;

			IStatus status = Status.OK_STATUS;

			try {
				deploymentProperties = parser.load(subMonitor.newChild(100));
				if (deploymentProperties == null) {
					status = BootDashActivator.createErrorStatus(null,
							"No deployment propreties found. Please ensure the project contains a valid manifest.yml or configurations are set correctly");

				} else {

					status = deploymentProperties.validate();
				}

			} catch (Exception e) {
				status = BootDashActivator.createErrorStatus(e);
			}

			if (!status.isOK()) {
				StringWriter errorMessage = new StringWriter();
				errorMessage.append("Failed to deploy project: ");
				errorMessage.append(javaProject.getProject().getName());
				errorMessage.append(" - ");
				if (projectsToDeploy.size() > 1) {
					errorMessage.append("Do you wish to continue deploying the other projects?");
				}
				errorMessage.append(" Error: ");
				errorMessage.append(status.getMessage());

				proceedIfError = ui.confirmOperation("Error Deploying Project", errorMessage.toString());
			} else {
				toDeploy.add(deploymentProperties);

			}

		}

		if (!proceedIfError) {
			return;
		}

		deployInParallel(toDeploy, subMonitor);

		// Add listener to start apps as they are deployed
		model.getElements().addListener(new StartAppListener(toDeploy));

	}

	protected void deployInParallel(List<CloudDeploymentProperties> toDeploy, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, toDeploy.size() * 10);
		for (CloudDeploymentProperties properties : toDeploy) {

			model.getCloudOpExecution().runOp(new DeployAppOperation(client, properties, model, ui));
			subMonitor.worked(10);
		}
	}

	class StartAppListener implements ValueListener<Set<BootDashElement>> {

		private final List<CloudDeploymentProperties> appsToStart;

		public StartAppListener(List<CloudDeploymentProperties> appsToStart) {
			// Make sure to work on a copy as it will be modified
			this.appsToStart = new ArrayList<CloudDeploymentProperties>(appsToStart);
		}

		@Override
		public synchronized void gotValue(LiveExpression<Set<BootDashElement>> exp, Set<BootDashElement> elements) {
			if (elements != null && !appsToStart.isEmpty()) {
				for (BootDashElement element : elements) {

					boolean found = false;
					for (CloudDeploymentProperties properties : appsToStart) {
						if (properties.getAppName().equals(element.getName())) {
							found = true;
							break;
						}
					}

					if (found) {
						try {
							element.restart(RunState.STARTING, ApplicationDeployer.this.ui);
						} catch (Exception e) {
							BootDashActivator.log(e);
						}
						appsToStart.remove(element.getName());
					}
				}
			}

			if (appsToStart.isEmpty()) {
				model.getElements().removeListener(this);
			}
		}
	}
}