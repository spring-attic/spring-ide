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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;

/**
 * Operation for (re)starting existing CF app with associated project
 *
 * @author Alex Boyko
 *
 */
public class RestartExistingApplicationOperation extends CloudApplicationOperation {

	final private UserInteractions ui;
	private DebugSupport debugSupport;
	private boolean isDebugging;
	private ApplicationDeploymentOperations operations;

	/**
	 * Optional parameter of this operation. If set then it will be used instead of trying to 'resolve' deployment
	 * properties.
	 */
	private CloudApplicationDeploymentProperties deploymentProperties;
	private CloudAppDashElement app;

	public RestartExistingApplicationOperation(String opName, CloudAppDashElement app,
			DebugSupport debugSupport, RunState runState, ApplicationDeploymentOperations operations,
			UserInteractions ui, CancelationToken cancelationToken) {
		super(
				opName == null ? "Re-deploying and re-starting app: " + app.getName() : opName,
				app.getCloudModel(),
				app.getName(),
				cancelationToken
		);
		this.app = app;
		this.debugSupport = debugSupport;
		this.isDebugging = runState == RunState.DEBUGGING;
		this.ui = ui;
		this.operations = operations;
	}

	public void setDeploymentProperties(CloudApplicationDeploymentProperties deploymentProperties) {
		this.deploymentProperties = deploymentProperties;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		app.startOperationStarting();
		try {
			//Refresh app data and check that the application (still) exists in Cloud Foundry
			// This also ensures that the 'diff change dialog' will pick up on the latest changes.
			//TODO: should this refresh be moved closer to the where we actually compute the diff?
			app = app.refresh();
			if (app==null) {
				throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
						"No Cloud Application found for '" + appName + "'"));
			}
			IProject project = app.getProject();
			if (project==null) {
				throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
						"Local project not associated to CF app '" + appName + "'"));
			}

			CloudApplicationDeploymentProperties properties = deploymentProperties==null
					?model.resolveDeploymentProperties(app, ui, monitor)
					:deploymentProperties;

			// Update JAVA_OPTS env variable with Remote DevTools Client secret
			DevtoolsUtil.setupEnvVarsForRemoteClient(properties.getEnvironmentVariables(), DevtoolsUtil.getSecret(project));
			if (debugSupport != null) {
				if (isDebugging) {
					debugSupport.setupEnvVars(properties.getEnvironmentVariables());
				} else {
					debugSupport.clearEnvVars(properties.getEnvironmentVariables());
				}
			}

			List<Operation<?>> deploymentOperations = new ArrayList<Operation<?>>();
			// Stop application first to avoid issues when updating or restarting
			// the app in case
			// the app is in a "failed" state in CF. This is a work around to handle
			// 503 errors that
			// may result when the underlying client indirectly fetches app instance
			// stats that may not
			// be available (and thus throw 503)
			CancelationToken cancelTok = getCancelationToken();
			deploymentOperations.add(new ApplicationStopOperation(app, false, cancelTok));
			deploymentOperations.add(new ApplicationPropertiesUpdateOperation(properties, model, cancelTok));
			deploymentOperations.add(new ApplicationPushOperation(properties, model, ui, cancelTok));
			deploymentOperations.add(this.operations.restartOnly(app, cancelTok));

			CloudApplicationOperation op = new CompositeApplicationOperation(opName, model, properties.getAppName(),
					deploymentOperations, cancelTok);

			op.run(monitor);
			app.startOperationEnded(null, getCancelationToken(), monitor);
		} catch (Throwable e) {
			app.startOperationEnded(e, getCancelationToken(), monitor);
		}
	}


}
