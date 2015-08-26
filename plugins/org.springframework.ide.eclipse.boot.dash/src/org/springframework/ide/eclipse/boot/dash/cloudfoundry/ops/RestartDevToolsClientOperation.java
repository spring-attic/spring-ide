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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Operation restarting the Remote DevTools Client application for a CF app.
 * Updates the secret for the launch config and the app on CF
 *
 * @author Alex Boyko
 *
 */
public class RestartDevToolsClientOperation extends ApplicationStartOperation {

	public RestartDevToolsClientOperation(String appName, CloudFoundryBootDashModel model, RunState startMode) {
		super("Restarting Remote DevTools Client for '" + appName + "'", appName, model, startMode);
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		CloudDashElement cde = model.getElement(getAppName());
		if (DevtoolsUtil.isDevClientAttached(cde, null)) {
			DevtoolsUtil.disconnectDevtoolsClientsFor(cde);
		}
		String debugSecret = RandomStringUtils.randomAlphabetic(20);
		if (getStartMode() == RunState.DEBUGGING) {
			updateEnvVars(debugOpts(debugSecret));
			DevtoolsUtil.launchDevtools(cde, debugSecret, ILaunchManager.DEBUG_MODE, monitor);
		} else {
			updateEnvVars(runOpts(debugSecret));
			DevtoolsUtil.launchDevtools(cde, debugSecret, ILaunchManager.RUN_MODE, monitor);
		}
	}

	private Map<String, String> runOpts(String debugSecret) {
		Map<String, String> opts = new HashMap<String, String>();
		opts.put("JAVA_OPTS", "-Dspring.devtools.remote.secret="+debugSecret
				+" -Dspring.devtools.restart.enabled=false");
		return opts;
	}

}
