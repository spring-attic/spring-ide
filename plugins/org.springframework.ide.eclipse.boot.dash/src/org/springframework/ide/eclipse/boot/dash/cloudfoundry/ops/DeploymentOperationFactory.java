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

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public class DeploymentOperationFactory {

	private final CloudFoundryBootDashModel model;
	private final CloudApplicationDeploymentProperties properties;
	private final List<CloudDomain> domains;
	private final IProject project;

	public DeploymentOperationFactory(CloudFoundryBootDashModel model, CloudApplicationDeploymentProperties properties,
			IProject project, List<CloudDomain> domains) {
		this.model = model;
		this.project = project;
		this.properties = properties;
		this.domains = domains;
	}

	public CloudApplicationOperation getDeploymentOperationExistingApp() {
		List<CloudApplicationOperation> uploadAndRestartOps = uploadUpdateRestartOps();

		CloudApplicationOperation op = new CompositeApplicationOperation("Re-deploying and re-starting project: " + project.getName(),
				model, properties.getAppName(), uploadAndRestartOps, true);
		op.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));
		return op;
	}

	public CloudApplicationOperation getDeploymentOperation() {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();

		CloudApplicationOperation createOp = new ApplicationCreateOperation(properties, model);
		deploymentOperations.add(createOp);

		List<CloudApplicationOperation> uploadAndRestartOps = uploadUpdateRestartOps();
		deploymentOperations.addAll(uploadAndRestartOps);

		CloudApplicationOperation op = new CompositeApplicationOperation("Deploying and starting project: " + project.getName(),
				model, properties.getAppName(), deploymentOperations, true);
		op.addApplicationUpdateListener(new FullAppDeploymentListener(properties.getAppName(), model));
		return op;
	}

	protected List<CloudApplicationOperation> uploadUpdateRestartOps() {
		List<CloudApplicationOperation> deploymentOperations = new ArrayList<CloudApplicationOperation>();
		CloudApplicationOperation uploadOp = new ApplicationUploadOperation(properties, model);
		deploymentOperations.add(uploadOp);

		ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains);

		// For now only update the app properties if there is a manifest as of
		// RC3, this is
		// the only way to specify changes to an app through boot dash
		if (manifestHandler.hasManifest()) {
			CloudApplicationOperation updateOp = new ApplicationPropertiesUpdateOperation(properties, model);
			deploymentOperations.add(updateOp);
		}

		CloudApplicationOperation restartOp = new ApplicationStartOperation(properties.getAppName(), model);
		deploymentOperations.add(restartOp);

		return deploymentOperations;
	}

}
