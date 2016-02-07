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

public class ApplicationDeploymentOperations {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace the existing application - {0} - with project: {1}?";

	private final CloudFoundryBootDashModel model;

	public ApplicationDeploymentOperations(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public Operation<?> restartAndPush(CloudAppDashElement element, DebugSupport debugSupport,
			RunState runningOrDebugging, UserInteractions ui) throws Exception {
		String opName = "Starting application '" + element.getName() + "' in "
				+ (runningOrDebugging == RunState.DEBUGGING ? "DEBUG" : "RUN") + " mode";

		Operation<?> restartExistingOp = new RestartExistingApplicationOperation(opName, model, element.getName(),
				debugSupport, runningOrDebugging, this, ui);

		if (runningOrDebugging == RunState.DEBUGGING) {

			if (debugSupport != null && debugSupport.isSupported(element)) {
				Operation<?> debugOp = debugSupport.createOperation(element, opName, ui);

				CloudFoundryBootDashModel cloudModel = element.getCloudModel();
				return new CompositeApplicationOperation(opName, cloudModel, element.getName(),
						Arrays.asList(new Operation<?>[] { restartExistingOp, debugOp }), RunState.STARTING);
			} else {
				String title = "Debugging is not supported for '" + element.getName() + "'";
				String msg = debugSupport.getNotSupportedMessage(element);
				if (msg == null) {
					msg = title;
				}
				ui.errorPopup(title, msg);
				throw org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil.coreException(msg);
			}
		} else {
			return restartExistingOp;
		}
	}

	public CloudApplicationOperation restartOnly(IProject project, String appName, RunState preferredState) {
		return new ApplicationRestartOnlyOp(appName, this.model, preferredState);
	}

	public CloudApplicationOperation createRestartPush(IProject project,
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

}
