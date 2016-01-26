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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;

/**
 * Operation for (re) starting Remote DevTools client for CF app with associated
 * project.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class ApplicationStartWithRemoteClientOperation extends CloudApplicationOperation {

	final private RunState runOrDebug;
	final private DebugSupport debugSupport;
	final private UserInteractions ui;
	private CloudAppDashElement app;
	private ApplicationDeploymentOperations operations;

	public ApplicationStartWithRemoteClientOperation(String opName, CloudAppDashElement app, RunState runOrDebug,
			ApplicationDeploymentOperations operations, UserInteractions ui) {
		super(opName, app.getCloudModel(), app.getName());
		this.app = app;
		this.runOrDebug = runOrDebug;
		this.ui = ui;
		this.debugSupport = app.getDebugSupport();
		this.operations = operations;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		List<CloudApplicationOperation> ops = new ArrayList<CloudApplicationOperation>();

		CloudAppInstances instances = getCachedApplicationInstances();
		Map<String, String> envVars = instances.getApplication().getEnvAsMap();

		CloudAppDashElement cde = model.getElement(appName);
		if (cde == null || cde.getProject() == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
					"Local project not associated to CF app '" + appName + "'"));
		}

		String opName = "Restarting application '" + cde.getName() + "' in "
				+ (runOrDebug == RunState.DEBUGGING ? "DEBUG" : "RUN") + " mode";

		ops.add(new SetHealthCheckOperation(app, HealthCheckSupport.HC_NONE, ui, /* confirmChange */true));
		if (!DevtoolsUtil.isEnvVarSetupForRemoteClient(envVars, DevtoolsUtil.getSecret(cde.getProject()))) {
			ops.add(operations.restartAndPush(opName, appName, debugSupport, runOrDebug, ui));
		} else if (cde.getRunState() == RunState.INACTIVE) {
			ops.add(operations.restartOnly(cde.getProject(), appName, runOrDebug));
		}

		ops.add(new RemoteDevClientStartOperation(model, appName, runOrDebug));

		new CompositeApplicationOperation(getName(), model, appName, ops).run(monitor);
	}

}
