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
import java.util.Map.Entry;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationStartOperation extends CloudApplicationOperation {

	private final String appName;
	private final RunState startMode;

	public ApplicationStartOperation(String appName, CloudFoundryBootDashModel model, RunState startMode) {
		this("Starting application: " + appName, appName, model, startMode);
	}

	public ApplicationStartOperation(String opName, String appName, CloudFoundryBootDashModel model, RunState startMode) {
		super("Starting application: " + appName, model, appName);
		this.appName = appName;
		this.startMode = startMode;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		// Use the cached Cloud app instead of fetching a new one to avoid
		// unnecessary
		// network I/O. An updated Cloud application will be fetched when the op
		// completes
		logAndUpdateMonitor("Starting application: " + appName, monitor);

		getAppUpdateListener().applicationStarting(getCachedApplication());

		String debugSecret = null;
		boolean isDebugging = startMode==RunState.DEBUGGING;
		if (isDebugging) {
			debugSecret = RandomStringUtils.randomAlphabetic(20);
			updateEnvVars(debugOpts(debugSecret));
		}

		requests.restartApplication(appName);

		RunState runState = new ApplicationRunningStateTracker(appName, requests, model,
				ApplicationRunningStateTracker.TIMEOUT).startTracking(monitor);

		model.updateApplication(getCloudApplicationInstances(), runState);

		if (isDebugging) {
			CloudDashElement cde = model.getElement(appName);
			DevtoolsUtil.launchDevtools(cde, debugSecret, ILaunchManager.DEBUG_MODE, monitor);
		}
	}

	protected Map<String, String> debugOpts(String debugSecret) {
		Map<String, String> opts = new HashMap<String, String>();
		opts.put("JAVA_OPTS", "-Dspring.devtools.remote.secret="+debugSecret
				+" -Dspring.devtools.restart.enabled=false -Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n");
		return opts;
	}

	protected void updateEnvVars(Map<String, String> toAdd) throws Exception {
		CloudAppInstances instances = getCachedApplication();
		Map<String, Object> existingVars = requests
				.getApplicationEnvironment(instances.getApplication().getMeta().getGuid());

		Map<String, String> varsToUpdate = new HashMap<String, String>();
		if (existingVars != null) {
			for (Entry<String, Object> var : existingVars.entrySet()) {
				varsToUpdate.put(var.getKey(), var.getValue().toString());
			}
		}

		varsToUpdate.putAll(toAdd);

		requests.updateApplicationEnvironment(appName, varsToUpdate);
	}

	public ISchedulingRule getSchedulingRule() {
		return new CloudApplicationSchedulingRule(model.getRunTarget(), appName);
	}

	public String getAppName() {
		return appName;
	}

	public RunState getStartMode() {
		return startMode;
	}
}
