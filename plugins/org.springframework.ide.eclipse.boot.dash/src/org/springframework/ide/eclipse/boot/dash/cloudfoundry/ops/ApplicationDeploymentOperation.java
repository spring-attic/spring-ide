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

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ManifestParser;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ApplicationDeploymentOperation extends CloudApplicationOperation {

	private final IProject project;
	private BootDashElement element;

	private final boolean shouldAutoReplace;

	public ApplicationDeploymentOperation(CloudFoundryOperations client, IProject project, BootDashElement element,
			CloudFoundryBootDashModel model, UserInteractions ui, boolean shouldAutoReplace) {
		super("Deploying project " + project.getName(), client, element != null ? element.getName() : project.getName(),
				model, ui);

		this.element = element;
		this.project = project;
		this.shouldAutoReplace = shouldAutoReplace;
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor)
			throws Exception, OperationCanceledException {

		CloudApplicationDeploymentProperties properties = getDeploymentProperties(project, element, monitor);

		model.notifyApplicationChanged(properties.getAppName(), RunState.STARTING);

		CloudApplicationOperation createOp = new CreateAndUpdateCloudAppOp(client, properties, model, ui);
		CloudApplicationOperation uploadOp = new UploadApplicationOperation(client, properties, model, ui);
		CloudApplicationOperation restartOp = new ApplicationStartOperation(properties.getAppName(), model, client, ui);

		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
		deploymentOperations.add(createOp);
		deploymentOperations.add(uploadOp);
		deploymentOperations.add(restartOp);

		CloudApplicationOperation compositeOp = new CompositeAppOp(getName(), client,
				appName, model, ui, deploymentOperations);
		compositeOp.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));

		model.getCloudOpExecution().runOpAsynch(compositeOp);

		return null;
	}

	protected CloudApplicationDeploymentProperties getDeploymentProperties(IProject project, BootDashElement element,
			IProgressMonitor monitor) throws Exception, OperationCanceledException {

		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.setTaskName("Resolving deployment properties for project: " + project.getName());

		CloudApplicationDeploymentProperties deploymentProperties = null;

		List<CloudDomain> domains = client.getDomains();

		ManifestParser parser = new ManifestParser(project, domains);

		if (parser.hasManifest()) {
			deploymentProperties = parser.load(subMonitor.newChild(100));
		}

		// Mapping or replacing an exist cloud application:
		if (element instanceof CloudDashElement) {
			CloudDashElement cloudElement = (CloudDashElement) element;

			CloudApplicationDeploymentProperties existingProps = cloudElement.getDeploymentProperties();

			if (deploymentProperties == null) {
				deploymentProperties = existingProps;
			}

			// // TODO:Mapping case: Currently not supported as it needs more
			// work
			// else if (existingProps != null) {
			// // check if app names in the existing boot element match the
			// // parsed deployment properties;
			// if
			// (!deploymentProperties.getAppName().equals(existingProps.getAppName()))
			// {
			// throw BootDashActivator.asCoreException("The application name in
			// the existing target element - "
			// + existingProps.getAppName()
			// + " - does not match the application name in the project's
			// manifest.yml - "
			// + deploymentProperties.getAppName()
			// + ". At this time project-to-application mapping is supported
			// only if both names match.");
			// // // NOTE: to delete, do not delete through the element
			// // // API as it is done asynchronously
			// //
			// // // Delete from backend first
			// // CloudApplicationOperation op = new
			// // CloudApplicationDeleteOperation(client,
			// // existingProps.getAppName(), model, ui);
			// // op.run(subMonitor.newChild(100));
			// //
			// // // Delete locally from model
			// // model.deleteFromModelOnly(cloudElement, ui);
			// }
			// // Map the project
			//// deploymentProperties.setProject(existingProps.getProject());
			// }
		}

		IStatus status = Status.OK_STATUS;

		if (deploymentProperties == null) {
			status = BootDashActivator.createErrorStatus(null, "No deployment propreties found for " + project.getName()
					+ ". Please ensure that the project contains a valid manifest.yml.");
		} else {
			// Update the app name
			this.appName = deploymentProperties.getAppName();
			status = deploymentProperties.validate();
		}

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		deploymentProperties.setShoudAutoReplace(shouldAutoReplace);
		return deploymentProperties;
	}

}
