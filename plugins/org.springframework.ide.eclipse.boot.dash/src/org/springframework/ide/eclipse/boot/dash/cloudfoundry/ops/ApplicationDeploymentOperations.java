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

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class ApplicationDeploymentOperations {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace the existing application - {0} - with project: {1}?";

	private final CloudFoundryBootDashModel model;

	public ApplicationDeploymentOperations(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	private Operation<?> startAndPush(CloudAppDashElement cde, DebugSupport debugSupport,
			RunState runningOrDebugging, UserInteractions ui, CloudApplicationDeploymentProperties deploymentProperties, CancelationToken cancelationToken) throws Exception {
		String opName = "Starting application '" + cde.getName() + "' in "
				+ (runningOrDebugging == RunState.DEBUGGING ? "DEBUG" : "RUN") + " mode";

		RestartExistingApplicationOperation restartExistingOp = new RestartExistingApplicationOperation(opName, cde,
				debugSupport, runningOrDebugging, this, ui, cancelationToken);
		if (deploymentProperties!=null) {
			restartExistingOp.setDeploymentProperties(deploymentProperties);
		}

		if (runningOrDebugging == RunState.DEBUGGING) {

			if (debugSupport != null && debugSupport.isSupported(cde)) {
				Operation<?> debugOp = debugSupport.createOperation(cde, opName, ui, cancelationToken);

				CloudFoundryBootDashModel cloudModel = cde.getCloudModel();
				return new CompositeApplicationOperation(opName, cloudModel, cde.getName(),
						Arrays.asList(new Operation<?>[] { restartExistingOp, debugOp }),
						cancelationToken
				);
			} else {
				String title = "Debugging is not supported for '" + cde.getName() + "'";
				String msg = debugSupport.getNotSupportedMessage(cde);
				if (msg == null) {
					msg = title;
				}
				ui.errorPopup(title, msg);
				throw ExceptionUtil.coreException(msg);
			}
		} else {
			return restartExistingOp;
		}
	}

	public Operation<?> firstStartAndPush(CloudAppDashElement cde, CloudApplicationDeploymentProperties deploymentProperties,
			DebugSupport debugSupport, RunState runningOrDebugging, UserInteractions ui, CancelationToken cancelationToken) throws Exception {
		return startAndPush(cde, debugSupport, runningOrDebugging, ui, deploymentProperties, cancelationToken);
	}

	public CloudApplicationOperation restartOnly(CloudAppDashElement app, CancelationToken cancelationToken) {
		return new ApplicationRestartOnlyOp(app, cancelationToken);
	}

	public CloudApplicationOperation createAddElement(IProject project,
			CloudApplicationDeploymentProperties properties, DebugSupport debugSupport, RunState runOrDebug,
			UserInteractions ui, IProgressMonitor monitor) throws Exception {

		CFApplication existingApp = model.getRunTarget().getClient().getApplication(properties.getAppName());

		if (existingApp != null && !ui.confirmOperation(APP_FOUND_TITLE,
				NLS.bind(APP_FOUND_MESSAGE, properties.getAppName(), properties.getProject().getName()))) {
			throw new OperationCanceledException();
		}

		RunState initialRunstate = RunState.STARTING;

		return new AddElementOperation(properties, model, existingApp, initialRunstate, this, debugSupport, runOrDebug,
				ui);
	}

	public Operation<?> restartAndPush(CloudAppDashElement cde, DebugSupport debugSupport, RunState runOrDebug, UserInteractions ui, CancelationToken cancelationToken) throws Exception {
		return startAndPush(cde, debugSupport, runOrDebug, ui, null, cancelationToken);
	}


}
