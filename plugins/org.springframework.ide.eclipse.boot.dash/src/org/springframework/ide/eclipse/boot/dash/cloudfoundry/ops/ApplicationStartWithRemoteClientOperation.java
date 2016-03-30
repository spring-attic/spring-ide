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
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
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
			ApplicationDeploymentOperations operations, UserInteractions ui, CancelationToken cancelationToken) {
		super(opName, app.getCloudModel(), app.getName(), cancelationToken);
		this.app = app;
		this.runOrDebug = runOrDebug;
		this.ui = ui;
		this.debugSupport = app.getDebugSupport();
		this.operations = operations;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		CancelationToken cancelToken = getCancelationToken();
		List<Operation<?>> ops = new ArrayList<Operation<?>>();

		Map<String, String> envVars = app.getSummaryData().getEnvAsMap();

		CloudAppDashElement cde = model.getApplication(appName);
		if (cde == null || cde.getProject() == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
					"Local project not associated to CF app '" + appName + "'"));
		}

		boolean resetConsole = false;
		ops.add(new SetHealthCheckOperation(app, HealthCheckSupport.HC_NONE, ui, /* confirmChange */true, cancelToken));
		if (!DevtoolsUtil.isEnvVarSetupForRemoteClient(envVars, DevtoolsUtil.getSecret(cde.getProject()))) {
			ops.add(operations.restartAndPush(cde, debugSupport, runOrDebug, ui, cancelToken));
			/*
			 * Restart and push op resets console anyway, no need to reset it again
			 */
		} else if (cde.getRunState() == RunState.INACTIVE) {
			ops.add(operations.restartOnly(cde, cancelToken));
			resetConsole = true;
		}

		ops.add(new RemoteDevClientStartOperation(model, appName, runOrDebug, cancelToken));

		new CompositeApplicationOperation(getName(), model, appName, ops, resetConsole, cancelToken).run(monitor);
	}

}
