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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.InstanceState;
import org.cloudfoundry.client.lib.domain.InstanceStats;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationRunningStateTracker {
	public static final long TIMEOUT = 1000 * 60 * 5;

	public static final long WAIT_TIME = 1000;

	private final ClientRequests requests;

	private final String appName;

	private final CloudFoundryBootDashModel model;

	private final long timeout;

	public ApplicationRunningStateTracker(String appName, ClientRequests requests, CloudFoundryBootDashModel model,
			long timeout) {
		this.requests = requests;
		this.appName = appName;
		this.model = model;
		this.timeout = timeout;
	}

	public RunState startTracking(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		// fetch an updated Cloud Application that reflects changes that
		// were
		// performed on it. Make sure the element app reference is updated
		// as
		// run state of the element depends on the app being up to date.
		// Wait for application to be started
		RunState runState = RunState.UNKNOWN;

		long currentTime = System.currentTimeMillis();
		long roughEstimateFetchStatsms = 5000;

		long totalTime = currentTime + timeout;
		String checkingMessage = "Checking if the application is running";

		int estimatedAttempts = (int) (timeout / (WAIT_TIME + roughEstimateFetchStatsms));
		
		monitor.beginTask(checkingMessage, estimatedAttempts);
		
		model.getElementConsoleManager().writeToConsole(appName, checkingMessage + ". Please wait...",
				LogType.LOCALSTDOUT);

		while (runState != RunState.RUNNING && runState != RunState.FLAPPING && currentTime < totalTime) {
			int timeLeft = (int) ((totalTime - currentTime) / 1000);

			// Don't log this. Only update the monitor
			monitor.setTaskName(checkingMessage + ". Time left before timeout: " + timeLeft + 's');

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			ApplicationStats stats = requests.getApplicationStats(appName);
			monitor.worked(1);
			runState = getRunState(stats);
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {

			}
			currentTime = System.currentTimeMillis();
		}

		if (runState != RunState.RUNNING) {
			String warning = "Timed out waiting for application - " + appName
					+ " to start. Please wait and manually refresh the target, or check if the application logs show any errors.";
			model.getElementConsoleManager().writeToConsole(appName, warning, LogType.LOCALSTDERROR);
			throw BootDashActivator.asCoreException(warning);

		} else {
			model.getElementConsoleManager().writeToConsole(appName, "Application appears to have started - " + appName,
					LogType.LOCALSTDOUT);
		}
		return runState;
	}

	public static RunState getRunState(InstanceState instanceState) {
		RunState runState = null;
		if (instanceState != null) {
			switch (instanceState) {
			case RUNNING:
				runState = RunState.RUNNING;
				break;
			case CRASHED:
				runState = RunState.CRASHED;
				break;
			case FLAPPING:
				runState = RunState.FLAPPING;
				break;
			case STARTING:
				runState = RunState.STARTING;
				break;
			case DOWN:
				runState = RunState.INACTIVE;
				break;
			default:
				runState = RunState.UNKNOWN;
				break;
			}
		}

		return runState;
	}

	public static RunState getRunState(CloudAppInstances instances) {

		RunState runState = RunState.UNKNOWN;
		ApplicationStats stats = instances.getStats();
		CloudApplication app = instances.getApplication();
		// if app desired state is "Stopped", return inactive
		if ((stats == null || stats.getRecords() == null || stats.getRecords().isEmpty())
				&& app.getState() == CloudApplication.AppState.STOPPED) {
			runState = RunState.INACTIVE;
		} else {
			runState = getRunState(stats);
		}
		return runState;
	}

	private static RunState getRunState(ApplicationStats stats) {
		RunState runState = RunState.UNKNOWN;

		if (stats != null && stats.getRecords() != null) {
			for (InstanceStats stat : stats.getRecords()) {

				RunState instanceState = getRunState(stat.getState());
				runState = instanceState != null ? runState.merge(instanceState) : null;

			}
		}
		return runState;
	}
}
