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

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Operation for (re)starting existing CF app with associated project
 *
 * @author Alex Boyko
 *
 */
public class FullApplicationRestartOperation extends CloudApplicationOperation {

	final private UserInteractions ui;
	private DebugSupport debugSupport;
	private boolean isDebugging;

	public FullApplicationRestartOperation(String opName, CloudFoundryBootDashModel model, String appName,
			RunState runOrDebug, DebugSupport debugSupport, UserInteractions ui) {
		super(opName, model, appName);
		this.debugSupport = debugSupport;
		this.isDebugging = runOrDebug==RunState.DEBUGGING;
		this.ui = ui;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		CloudApplication application = requests.getApplication(appName);
		if (application == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
					"No Cloud Application found for '" + appName + "'"));
		}
		CloudDashElement cde = model.getElement(appName);
		if (cde == null || cde.getProject() == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
					"Local project not associated to CF app '" + appName + "'"));
		}
		IProject project = cde.getProject();

		List<CloudDomain> domains = model.getCloudTarget().getDomains(requests, monitor);

		CloudApplicationDeploymentProperties properties = null;
		ApplicationManifestHandler manifestHandler = new ApplicationManifestHandler(project, domains);

		if (manifestHandler.hasManifest()) {
			List<CloudApplicationDeploymentProperties> props = manifestHandler.load(monitor);
			properties = props != null && !props.isEmpty() ? props.get(0) : null;
		}

		if (properties == null) {
			properties = CloudApplicationDeploymentProperties.getFor(application, project);
		}

		DevtoolsUtil.setupEnvVarsForRemoteClient(properties.getEnvironmentVariables(), DevtoolsUtil.getSecret(project));
		if (debugSupport!=null) {
			if (isDebugging) {
				debugSupport.setupEnvVars(properties.getEnvironmentVariables());
			} else {
				debugSupport.clearEnvVars(properties.getEnvironmentVariables());
			}
		}

		CloudApplicationOperation op = new DeploymentOperationFactory(model, project, ui)
				.getRestartAndDeploy(properties);

		op.run(monitor);
	}

}
